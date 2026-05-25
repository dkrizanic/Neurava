import type { EntityId } from '../../shared/lib/ids';

export type SourceReference = {
  id: EntityId;
  type: 'note' | 'project' | 'calendarEvent' | 'gmailThread' | 'reminder';
  title: string;
};
