import type { EntityId } from '../../shared/lib/ids';

export type PlanStatus = 'ACTIVE' | 'COMPLETED' | 'ARCHIVED';

export type Plan = {
  createdAt: string;
  goal: string;
  id: EntityId;
  items: string;
  linkedResources: string;
  ownerAccountId: EntityId;
  status: PlanStatus;
  title: string;
  updatedAt: string;
  workspaceContextId: EntityId;
};

export type UpsertPlanInput = {
  goal: string;
  items: string;
  linkedResources: string;
  title: string;
};

