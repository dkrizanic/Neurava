import { Search } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { formatIsoDateTime } from '../../../shared/lib/dates';
import { searchMemory } from '../api/searchApi';
import type { MemorySearchMatch } from '../types';

export function SearchPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [query, setQuery] = useState('');
  const [submittedQuery, setSubmittedQuery] = useState('');
  const [matches, setMatches] = useState<MemorySearchMatch[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!authenticated || !submittedQuery) {
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);

    searchMemory(submittedQuery, controller.signal)
      .then(setMatches)
      .catch((searchError) => {
        if ((searchError as Error).name !== 'AbortError') {
          setError('AI search could not be loaded.');
          setMatches([]);
        }
      })
      .finally(() => setIsLoading(false));

    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id, submittedQuery]);

  function submitSearch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const trimmedQuery = query.trim();
    if (!trimmedQuery) {
      setError('Search query is required.');
      return;
    }

    setSubmittedQuery(trimmedQuery);
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Discovery</p>
          <h2>Search</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Search</h2>
        <p>Search remembered fragments across indexed notes in this workspace.</p>
      </header>

      <form className="memory-search" onSubmit={submitSearch}>
        <Field
          error={error === 'Search query is required.' ? error : undefined}
          label="Weak-fragment search"
          maxLength={240}
          name="memory-search-query"
          onChange={(event) => setQuery(event.target.value)}
          placeholder="that API issue from the planning note"
          value={query}
        />
        <Button icon={<Search aria-hidden="true" size={18} />} type="submit" variant="primary">
          Search memory
        </Button>
      </form>

      {error && error !== 'Search query is required.' ? (
        <p className="session-warning" role="alert">
          {error} Try again when the service is available.
        </p>
      ) : null}

      {isLoading ? <LoadingState label="Searching memory" /> : null}

      {!isLoading && submittedQuery && matches.length === 0 && !error ? (
        <EmptyState
          description="Try a related phrase, a person, or a more specific detail from the note."
          icon={<Search aria-hidden="true" size={24} />}
          title="No memory matches yet"
        />
      ) : null}

      {!isLoading && matches.length > 0 ? (
        <section className="memory-results" aria-label="AI search results">
          {matches.map((match) => (
            <article className="memory-result" key={match.sourceId}>
              <div>
                <p className="eyebrow">{match.sourceType} source</p>
                <h3>{match.title}</h3>
              </div>
              <p>{match.snippet}</p>
              <div className="memory-result__meta">
                <time dateTime={match.sourceUpdatedAt}>{formatIsoDateTime(match.sourceUpdatedAt)}</time>
                <span>{Math.round(match.score * 100)}% match</span>
                <Link className="ui-link-button" to={`/notes/${match.sourceId}`}>
                  Open details and edit
                </Link>
              </div>
            </article>
          ))}
        </section>
      ) : null}
    </div>
  );
}
