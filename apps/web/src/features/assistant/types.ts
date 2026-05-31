import type { EntityId } from '../../shared/lib/ids';

export type SourceReference = {
  id: EntityId;
  type: 'note' | 'project' | 'calendarEvent' | 'gmailThread' | 'reminder';
  snippet: string;
  sourceUpdatedAt: string;
  score: number;
  title: string;
};

export type SourceAwareAnswer = {
  answer: string;
  enoughSourceContext: boolean;
  sources: SourceReference[];
};
