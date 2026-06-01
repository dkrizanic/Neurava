import type { EntityId } from '../../shared/lib/ids';

export type CalendarSyncState = 'NOT_SYNCED' | 'SYNCED' | 'FAILED';

export type Reminder = {
  calendarEventId: string | null;
  calendarSyncEnabled: boolean;
  calendarSyncState: CalendarSyncState;
  completedAt: string | null;
  createdAt: string;
  details: string;
  dueAt: string;
  id: EntityId;
  ownerAccountId: EntityId;
  relatedContext: string;
  title: string;
  updatedAt: string;
  workspaceContextId: EntityId;
};

export type UpsertReminderInput = {
  calendarSyncEnabled: boolean;
  details: string;
  dueAt: string;
  relatedContext: string;
  title: string;
};

