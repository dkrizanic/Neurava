import { Bot, Send } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { answerQuestion } from '../api/assistantApi';
import type { SourceAwareAnswer } from '../types';

export function AssistantPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [question, setQuestion] = useState('');
  const [submittedQuestion, setSubmittedQuestion] = useState('');
  const [answer, setAnswer] = useState<SourceAwareAnswer | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!authenticated || !submittedQuestion) {
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    answerQuestion(submittedQuestion, controller.signal)
      .then(setAnswer)
      .catch((answerError) => {
        if ((answerError as Error).name !== 'AbortError') {
          setError('Assistant answer could not be loaded.');
          setAnswer(null);
        }
      })
      .finally(() => setIsLoading(false));

    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id, submittedQuestion]);

  function submitQuestion(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedQuestion = question.trim();
    if (!trimmedQuestion) {
      setError('Question is required.');
      return;
    }

    setSubmittedQuestion(trimmedQuestion);
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
        <p>Ask a question and inspect the notebook sources behind the answer.</p>
      </header>

      <form className="assistant-question" onSubmit={submitQuestion}>
        <Field
          error={error === 'Question is required.' ? error : undefined}
          label="Question"
          maxLength={500}
          name="assistant-question"
          onChange={(event) => setQuestion(event.target.value)}
          placeholder="What did we decide about the API problem?"
          value={question}
        />
        <Button icon={<Send aria-hidden="true" size={18} />} type="submit" variant="primary">
          Ask assistant
        </Button>
      </form>

      {error && error !== 'Question is required.' ? (
        <p className="session-warning" role="alert">
          {error} Try again when the service is available.
        </p>
      ) : null}

      {isLoading ? <LoadingState label="Preparing source-aware answer" /> : null}

      {!isLoading && answer && !answer.enoughSourceContext ? (
        <EmptyState
          description="Try asking with a related phrase, note title, or a more specific remembered detail."
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

          <section className="assistant-sources" aria-label="Source references">
            <h3>Source References</h3>
            {answer.sources.map((source) => (
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
        </section>
      ) : null}
    </div>
  );
}
