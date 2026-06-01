import { Bot, RotateCcw, Send } from 'lucide-react';
import { useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion } from '../api/assistantApi';
import type {
  AssistantMessage,
  SourceReference,
} from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [question, setQuestion] = useState('');
  const [messages, setMessages] = useState<AssistantMessage[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [lastRequest, setLastRequest] = useState<string | null>(null);
  const latestMessage = messages.at(-1);
  const canRetry = Boolean(
    lastRequest
    && latestMessage?.role === 'assistant'
    && 'type' in latestMessage
    && latestMessage.type === 'error',
  );

  async function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    await sendPrompt(question);
  }

  async function sendPrompt(rawPrompt: string) {
    const trimmedPrompt = rawPrompt.trim();
    if (!trimmedPrompt) {
      setError('Question is required.');
      return;
    }

    setError(null);

    if (isAmbiguous(trimmedPrompt)) {
      appendMessages([
        userMessage(trimmedPrompt),
        assistantTextMessage('clarification', 'Tell me a little more so I can search the right notebook context. Try a topic, decision, person, or remembered phrase.'),
      ]);
      setQuestion(trimmedPrompt);
      return;
    }

    setLastRequest(trimmedPrompt);
    appendMessages([userMessage(trimmedPrompt)]);
    setQuestion('');
    setIsLoading(true);

    try {
      const answer = await answerQuestion(trimmedPrompt);
      appendMessages([{ answer, id: messageId(), role: 'assistant' }]);
    } catch {
      appendMessages([assistantTextMessage('error', 'Assistant response could not be loaded.')]);
    } finally {
      setIsLoading(false);
    }
  }

  async function retryLastRequest() {
    if (!lastRequest || isLoading) {
      return;
    }
    await sendPrompt(lastRequest);
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
        <p>Ask anything about your workspace. The assistant will use your notes as source context when it can.</p>
      </header>

      <section className="assistant-thread" aria-label="Assistant conversation">
        {messages.length === 0 ? (
          <div className="assistant-thread__empty">
            <Bot aria-hidden="true" size={24} />
            <p>Start with a specific question, search request, or summary request from the active workspace.</p>
          </div>
        ) : null}
        {messages.map((message) => (
          <AssistantMessageItem key={message.id} message={message} />
        ))}
        {isLoading ? <LoadingState label="Thinking with your workspace context" /> : null}
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
          label="Message"
          maxLength={1000}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="Ask, search, or summarize your notes"
          value={question}
        />
        <Button disabled={isLoading} icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          Send
        </Button>
      </form>
    </div>
  );
}

function AssistantMessageItem({ message }: { message: AssistantMessage }) {
  if (message.role === 'user') {
    return (
      <article className="assistant-message assistant-message--user">
        <p className="eyebrow">You</p>
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

  return (
    <article className="assistant-message assistant-message--assistant" aria-label="Assistant answer">
      {message.answer.enoughSourceContext ? (
        <>
          <p className="eyebrow">Assistant</p>
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

function SourceReferences({ sources }: { sources: SourceReference[] }) {
  if (sources.length === 0) {
    return null;
  }

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

function userMessage(text: string): AssistantMessage {
  return { id: messageId(), role: 'user', text };
}

function assistantTextMessage(type: 'clarification' | 'error', text: string): AssistantMessage {
  return { id: messageId(), role: 'assistant', text, type };
}

function messageId() {
  return globalThis.crypto?.randomUUID?.() ?? `${Date.now()}-${Math.random()}`;
}
