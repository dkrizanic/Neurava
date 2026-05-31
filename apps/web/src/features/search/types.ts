import type { EntityId } from '../../shared/lib/ids';

export type MemorySearchMatch = {
  score: number;
  snippet: string;
  sourceId: EntityId;
  sourceType: 'note';
  sourceUpdatedAt: string;
  title: string;
};
