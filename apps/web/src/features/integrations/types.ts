import type { EntityId } from '../../shared/lib/ids';

export type IntegrationProvider = 'CALENDAR' | 'GMAIL';

export type IntegrationConnection = {
  connectedAt: string | null;
  disconnectedAt: string | null;
  enabled: boolean;
  id: EntityId;
  permissionSummary: string;
  provider: IntegrationProvider;
  workspaceContextId: EntityId;
};

export type CalendarEventSummary = {
  endsAt: string;
  id: string;
  source: string;
  startsAt: string;
  title: string;
};

