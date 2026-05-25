import type { EntityId } from '../../shared/lib/ids';

export type Note = {
  id: EntityId;
  workspaceId: EntityId;
  title: string;
  body: string;
  createdAt: string;
  updatedAt: string;
};
