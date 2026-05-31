import { Bot, RotateCcw, Send } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion, summarizeHistory } from '../api/assistantApi';
import type {
  AssistantMessage,
  AssistantMode,
  SourceReference,
  SummarySections,
} from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [mode, setMode] = useState<AssistantMode>('answer');
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [lastRequest, setLastRequest] = useState<{ mode: AssistantMode; text: string } | null>(null);
  const latestMessage = messages.at(-1);
  const canRetry = Boolean(
    lastRequest
    && latestMessage?.role === 'assistant'
    && 'type' in latestMessage
    && latestMessage.type === 'error',
  );

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await sendPrompt(mode, question);
  }

  async function sendPrompt(requestMode: AssistantMode, rawPrompt: string) {
    const trimmedPrompt = rawPrompt.trim();
    if (!trimmedPrompt) {
      setError('Question is required.');
      return;
    }

    setError(null);

    if (isAmbiguous(trimmedPrompt)) {
      appendMessages([
        userMessage(requestMode, trimmedPrompt),
        assistantTextMessage(requestMode, 'clarification', 'Tell me a little more so I can search the right notebook context. Try a topic, decision, person, or remembered phrase.'),
      ]);
      setQuestion(trimmedPrompt);
      return;
    }

    setLastRequest({ mode: requestMode, text: trimmedPrompt });
    appendMessages([userMessage(requestMode, trimmedPrompt)]);
    setQuestion('');
    setIsLoading(true);

    try {
      if (requestMode === 'answer') {
        const answer = await answerQuestion(trimmedPrompt);
        appendMessages([{ answer, id: messageId(), mode: 'answer', role: 'assistant' }]);
      } else {
        const summary = await summarizeHistory(trimmedPrompt);
        appendMessages([{ id: messageId(), mode: 'summary', role: 'assistant', summary }]);
      }
    } catch {
      appendMessages([assistantTextMessage(requestMode, 'error', `${requestMode === 'answer' ? 'Assistant answer' : 'History summary'} could not be loaded.`)]);
    } finally {
      setIsLoading(false);
    }
  }

  function changeMode(nextMode: AssistantMode) {
    setMode(nextMode);
    setError(null);
  }

  async function retryLastRequest() {
    if (!lastRequest || isLoading) {
      return;
    }
    await sendPrompt(lastRequest.mode, lastRequest.text);
  }

  function appendMessages(nextMessages: AssistantMessage[]) {
    setMessages((current) => [...current, ...nextMessages]);
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">AI workspace</p>
          <h2>Assistant</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Assistant</h2>
        <p>Ask questions, summarize history, and inspect source-backed responses in this session.</p>
      </header>

      <div className="assistant-mode" role="group" aria-label="Assistant mode">
        <Button onClick={() => changeMode('answer')} type="button" variant={mode === 'answer' ? 'primary' : 'secondary'}>
          Answer
        </Button>
        <Button onClick={() => changeMode('summary')} type="button" variant={mode === 'summary' ? 'primary' : 'secondary'}>
          Summary
        </Button>
      </div>

      <section className="assistant-thread" aria-label="Assistant conversation">
        {messages.length === 0 ? (
          <div className="assistant-thread__empty">
            <Bot aria-hidden="true" size={24} />
            <p>Start with a specific question or topic from the active workspace.</p>
          </div>
        ) : null}
        {messages.map((message) => (
          <AssistantMessageItem key={message.id} message={message} />
        ))}
        {isLoading ? <LoadingState label={mode === 'answer' ? 'Preparing source-aware answer' : 'Preparing history summary'} /> : null}
      </section>

      {canRetry ? (
        <Button
          icon={<RotateCcw aria-hidden="true" size={16} />}
          onClick={() => void retryLastRequest()}
          type="button"
          variant="secondary"
        >
          Retry last request
        </Button>
      ) : null}

      {error && error !== 'Question is required.' ? (
        <p className="session-warning" role="alert">
          {error}
        </p>
      ) : null}

      <form className="assistant-question" onSubmit={(event) => void submitQuestion(event)}>
        <Field
          error={error === 'Question is required.' ? error : undefined}
          label={mode === 'answer' ? 'Question' : 'Summary topic'}
          maxLength={500}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder={mode === 'answer' ? 'What did we decide about the API problem?' : 'Summarize API planning'}
          value={question}
        />
        <Button disabled={isLoading} icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          {mode === 'answer' ? 'Ask assistant' : 'Summarize history'}
        </Button>
      </form>
    </div>
  );
}

function AssistantMessageItem({ message }: { message: AssistantMessage }) {
  if (message.role === 'user') {
    return (
      <article className="assistant-message assistant-message--user">
        <p className="eyebrow">{message.mode === 'answer' ? 'Question' : 'Summary topic'}</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if ('type' in message) {
    return (
      <article className={`assistant-message assistant-message--${message.type}`}>
        <p className="eyebrow">{message.type === 'error' ? 'Error' : 'Clarifying question'}</p>
        <p>{message.text}</p>
      </article>
    );
  }

  if (message.mode === 'answer') {
    return (
      <article className="assistant-message assistant-message--assistant" aria-label="Source-aware answer">
        {message.answer.enoughSourceContext ? (
          <>
            <p className="eyebrow">Answer</p>
            <p>{message.answer.answer}</p>
            <SourceReferences sources={message.answer.sources} />
          </>
        ) : (
          <>
            <p className="eyebrow">Not enough source context</p>
            <p>{message.answer.answer}</p>
          </>
        )}
      </article>
    );
  }

  return (
    <article className="assistant-message assistant-message--assistant" aria-label="History summary">
      {message.summary.enoughSourceContext ? (
        <>
          <p className="eyebrow">Summary</p>
          <SummarySectionList sections={message.summary.sections} />
          <SourceReferences sources={message.summary.sources} />
        </>
      ) : (
        <>
          <p className="eyebrow">Not enough source context</p>
          <SummarySectionList sections={message.summary.sections} />
        </>
      )}
    </article>
  );
}

function SummarySectionList({ sections }: { sections: SummarySections }) {
  return (
    <div className="summary-sections">
      <SummarySection items={sections.keyEvents} title="Key Events" />
      <SummarySection items={sections.decisions} title="Decisions" />
      <SummarySection items={sections.unresolvedItems} title="Unresolved Items" />
      <SummarySection items={sections.nextActions} title="Next Actions" />
    </div>
  );
}

function SummarySection({ items, title }: { items: string[]; title: string }) {
  return (
    <section>
      <h3>{title}</h3>
      <ul>
        {items.map((item) => (
          <li key={item}>{item}</li>
        ))}
      </ul>
    </section>
  );
}

function SourceReferences({ sources }: { sources: SourceReference[] }) {
  return (
    <section className="assistant-sources" aria-label="Source references">
      <h3>Source References</h3>
      {sources.map((source) => (
        <article className="assistant-source" key={source.id}>
          <div>
            <p className="eyebrow">{source.type} source</p>
            <h4>{source.title}</h4>
          </div>
          <p>{source.snippet}</p>
          <div className="memory-result__meta">
            <time dateTime={source.sourceUpdatedAt}>{formatIsoDateTime(source.sourceUpdatedAt)}</time>
            <span>{Math.round(source.score * 100)}% match</span>
          </div>
        </article>
      ))}
    </section>
  );
}

function isAmbiguous(prompt: string) {
  return prompt.split(/\s+/).filter(Boolean).length < 2;
}

function userMessage(mode: AssistantMode, text: string): AssistantMessage {
  return { id: messageId(), mode, role: 'user', text };
}

function assistantTextMessage(mode: AssistantMode, type: 'clarification' | 'error', text: string): AssistantMessage {
  return { id: messageId(), mode, role: 'assistant', text, type };
}

function messageId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
}
