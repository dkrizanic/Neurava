import { apiBaseUrl } from '../../../shared/api/httpClient';
import type { CalendarEventSummary, IntegrationConnection, IntegrationProvider } from '../types';

export async function fetchIntegrations(signal?: AbortSignal): Promise<IntegrationConnection[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/integrations`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to load integrations');
  }
  return response.json() as Promise<IntegrationConnection[]>;
}

export async function connectIntegration(provider: IntegrationProvider): Promise<IntegrationConnection> {
  const response = await fetch(`${apiBaseUrl}/api/v1/integrations/${provider}/connect`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error('Unable to connect integration');
  }
  return response.json() as Promise<IntegrationConnection>;
}

export async function disconnectIntegration(provider: IntegrationProvider): Promise<IntegrationConnection> {
  const response = await fetch(`${apiBaseUrl}/api/v1/integrations/${provider}/disconnect`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    method: 'POST',
  });
  if (!response.ok) {
    throw new Error('Unable to disconnect integration');
  }
  return response.json() as Promise<IntegrationConnection>;
}

export async function fetchCalendarEvents(signal?: AbortSignal): Promise<CalendarEventSummary[]> {
  const response = await fetch(`${apiBaseUrl}/api/v1/integrations/calendar/events`, {
    credentials: 'include',
    headers: { Accept: 'application/json' },
    signal,
  });
  if (!response.ok) {
    throw new Error('Unable to load calendar events');
  }
  return response.json() as Promise<CalendarEventSummary[]>;
}

