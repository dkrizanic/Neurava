import { Bot, Send } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion, summarizeHistory } from '../api/assistantApi';
import type { HistorySummary, SourceAwareAnswer, SourceReference, SummarySections } from '../types';

type AssistantMode = 'answer' | 'summary';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [mode, setMode] = useState<AssistantMode>('answer');
  const [question, setQuestion] = useState('');
  const [submittedQuestion, setSubmittedQuestion] = useState('');
  const [answer, setAnswer] = useState<SourceAwareAnswer | null>(null);
  const [summary, setSummary] = useState<HistorySummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!authenticated || !submittedQuestion) {
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    const request = mode === 'answer'
      ? answerQuestion(submittedQuestion, controller.signal).then((value) => {
        setAnswer(value);
        setSummary(null);
      })
      : summarizeHistory(submittedQuestion, controller.signal).then((value) => {
        setSummary(value);
        setAnswer(null);
      });

    request
      .catch((answerError) => {
        if ((answerError as Error).name !== 'AbortError') {
          setError(mode === 'answer' ? 'Assistant answer could not be loaded.' : 'History summary could not be loaded.');
          setAnswer(null);
          setSummary(null);
        }
      })
      .finally(() => setIsLoading(false));

    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id, mode, submittedQuestion]);

  function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) {
      setError('Question is required.');
      return;
    }

    setSubmittedQuestion(trimmedQuestion);
  }

  function changeMode(nextMode: AssistantMode) {
    setMode(nextMode);
    setAnswer(null);
    setSummary(null);
    setSubmittedQuestion('');
    setError(null);
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
        <p>Ask a question or summarize history, then inspect the notebook sources behind the result.</p>
      </header>

      <div className="assistant-mode" role="group" aria-label="Assistant mode">
        <Button onClick={() => changeMode('answer')} type="button" variant={mode === 'answer' ? 'primary' : 'secondary'}>
          Answer
        </Button>
        <Button onClick={() => changeMode('summary')} type="button" variant={mode === 'summary' ? 'primary' : 'secondary'}>
          Summary
        </Button>
      </div>

      <form className="assistant-question" onSubmit={submitQuestion}>
        <Field
          error={error === 'Question is required.' ? error : undefined}
          label={mode === 'answer' ? 'Question' : 'Summary topic'}
          maxLength={500}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder={mode === 'answer' ? 'What did we decide about the API problem?' : 'Summarize API planning'}
          value={question}
        />
        <Button icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          {mode === 'answer' ? 'Ask assistant' : 'Summarize history'}
        </Button>
      </form>

      {error && error !== 'Question is required.' ? (
        <p className="session-warning" role="alert">
          {error} Try again when the service is available.
        </p>
      ) : null}

      {isLoading ? <LoadingState label={mode === 'answer' ? 'Preparing source-aware answer' : 'Preparing history summary'} /> : null}

      {!isLoading && answer && !answer.enoughSourceContext ? (
        <EmptyState
          description="Try asking with a related phrase, note title, or a more specific remembered detail."
          icon={<Bot aria-hidden="true" size={24} />}
          title="Not enough source context"
        />
      ) : null}

      {!isLoading && summary && !summary.enoughSourceContext ? (
        <EmptyState
          description="Try a related topic, note title, or a more specific remembered detail."
          icon={<Bot aria-hidden="true" size={24} />}
          title="Not enough source context"
        />
      ) : null}

      {!isLoading && answer?.enoughSourceContext ? (
        <section className="assistant-answer" aria-label="Source-aware answer">
          <article className="assistant-answer__text">
            <p className="eyebrow">Answer</p>
            <p>{answer.answer}</p>
          </article>

          <SourceReferences sources={answer.sources} />
        </section>
      ) : null}

      {!isLoading && summary?.enoughSourceContext ? (
        <section className="assistant-answer" aria-label="History summary">
          <article className="assistant-answer__text">
            <p className="eyebrow">Summary</p>
            <SummarySectionList sections={summary.sections} />
          </article>

          <SourceReferences sources={summary.sources} />
        </section>
      ) : null}
    </div>
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
