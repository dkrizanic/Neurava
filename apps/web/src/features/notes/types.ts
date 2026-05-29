import type { EntityId } from '../../shared/lib/ids';

export type Note = {
  archivedAt: string | null;
  id: EntityId;
  ownerAccountId: EntityId;
  workspaceContextId: EntityId;
  title: string;
  body: string;
  createdAt: string;
  editorMode: 'RICH_TEXT' | 'MARKDOWN' | 'JOURNAL';
  favorite: boolean;
  linkedResources: string;
  pinned: boolean;
  tags: string;
  updatedAt: string;
};
