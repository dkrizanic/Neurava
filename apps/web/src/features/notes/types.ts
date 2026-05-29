import type { EntityId } from '../../shared/lib/ids';

export type Note = {
  id: EntityId;
  ownerAccountId: EntityId;
  workspaceContextId: EntityId;
  title: string;
  body: string;
  createdAt: string;
  updatedAt: string;
};
