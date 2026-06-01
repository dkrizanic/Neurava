import type { EntityId } from '../../shared/lib/ids';
import type { Note } from '../notes/types';

export type ProjectStatus = 'ACTIVE' | 'PAUSED' | 'COMPLETED' | 'ARCHIVED';

export type Project = {
  createdAt: string;
  description: string;
  id: EntityId;
  name: string;
  ownerAccountId: EntityId;
  status: ProjectStatus;
  updatedAt: string;
  workspaceContextId: EntityId;
};

export type UpsertProjectInput = {
  description: string;
  name: string;
  status: ProjectStatus;
};

export type ProjectHistorySummary = {
  decisions: string[];
  nextActions: string[];
  project: Project;
  sources: Note[];
  timeline: string[];
  unresolvedItems: string[];
};

