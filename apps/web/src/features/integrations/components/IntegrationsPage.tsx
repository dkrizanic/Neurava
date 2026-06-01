import { CalendarDays, Mail, PlugZap } from 'lucide-react';
import { useEffect, useMemo, useState } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, LoadingState } from '../../../shared/ui';
import {
  connectIntegration,
  disconnectIntegration,
  fetchCalendarEvents,
  fetchIntegrations,
} from '../api/integrationsApi';
import type { CalendarEventSummary, IntegrationConnection, IntegrationProvider } from '../types';

const providers: Array<{ description: string; icon: typeof CalendarDays; provider: IntegrationProvider; title: string }> = [
  {
    description: 'Use upcoming events as read-only planning context and sync reminders when enabled.',
    icon: CalendarDays,
    provider: 'CALENDAR',
    title: 'Google Calendar',
  },
  {
    description: 'Opt in before AI can search Gmail context and cite message sources.',
    icon: Mail,
    provider: 'GMAIL',
    title: 'Smart Gmail',
  },
];

export function IntegrationsPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [connections, setConnections] = useState<IntegrationConnection[]>([]);
  const [events, setEvents] = useState<CalendarEventSummary[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const calendarEnabled = useMemo(
    () => connections.some((connection) => connection.provider === 'CALENDAR' && connection.enabled),
    [connections],
  );

  useEffect(() => {
    if (!authenticated) {
      setConnections([]);
      setEvents([]);
      return;
    }

    const controller = new AbortController();
    setIsLoading(true);
    setError(null);
    Promise.all([fetchIntegrations(controller.signal), fetchCalendarEvents(controller.signal)])
      .then(([loadedConnections, loadedEvents]) => {
        setConnections(loadedConnections);
        setEvents(loadedEvents);
      })
      .catch((loadError: unknown) => {
        if (loadError instanceof DOMException && loadError.name === 'AbortError') {
          return;
        }
        setError('Integrations could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });

    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id]);

  async function toggle(provider: IntegrationProvider, enabled: boolean) {
    setError(null);
    try {
      const saved = enabled ? await disconnectIntegration(provider) : await connectIntegration(provider);
      setConnections((current) => {
        const next = current.filter((connection) => connection.provider !== provider);
        return [...next, saved];
      });
      if (provider === 'CALENDAR') {
        setEvents(saved.enabled ? await fetchCalendarEvents() : []);
      }
    } catch {
      setError('Integration could not be updated.');
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Connected sources</p>
          <h2>Integrations</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Integrations</h2>
        <p>Manage optional Calendar and Gmail permissions for planning and AI context.</p>
      </header>

      {error ? <p className="session-warning">{error}</p> : null}
      {isLoading ? <LoadingState label="Loading integrations" /> : null}

      <section className="section-grid" aria-label="Integration connections">
        {providers.map((item) => {
          const Icon = item.icon;
          const connection = connections.find((candidate) => candidate.provider === item.provider);
          const enabled = Boolean(connection?.enabled);
          return (
            <article className="section-card" key={item.provider}>
              <div className="feature-card__title">
                <Icon aria-hidden="true" size={22} />
                <h3>{item.title}</h3>
              </div>
              <p>{connection?.permissionSummary ?? item.description}</p>
              <Button onClick={() => void toggle(item.provider, enabled)} variant={enabled ? 'secondary' : 'primary'}>
                {enabled ? 'Disconnect' : 'Connect'}
              </Button>
            </article>
          );
        })}
      </section>

      {calendarEnabled && events.length > 0 ? (
        <section className="notes-list notes-list--compact" aria-label="Upcoming calendar context">
          {events.map((event) => (
            <article className="note-card note-card--compact" key={event.id}>
              <div className="note-card__heading">
                <h3>{event.title}</h3>
                <span className="note-badge">{event.source}</span>
              </div>
              <p>{new Date(event.startsAt).toLocaleString()} - {new Date(event.endsAt).toLocaleTimeString()}</p>
            </article>
          ))}
        </section>
      ) : (
        <EmptyState
          description="Connect Calendar to show upcoming planning context here."
          icon={<PlugZap aria-hidden="true" size={24} />}
          title="No calendar context"
        />
      )}
    </div>
  );
}

