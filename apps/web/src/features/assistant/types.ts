import type { EntityId } from '../../shared/lib/ids';
import type { MemorySearchMatch } from '../search/types';
import type { Note } from '../notes/types';
import type { Plan } from '../plans/types';
import type { Reminder } from '../reminders/types';

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

export type SummarySections = {
  decisions: string[];
  keyEvents: string[];
  nextActions: string[];
  unresolvedItems: string[];
};

export type HistorySummary = {
  enoughSourceContext: boolean;
  sections: SummarySections;
  sources: SourceReference[];
};

export type AssistantActionName = 'answer_question' | 'search_memory' | 'summarize_history';

export type AssistantActionResultByName = {
  answer_question: SourceAwareAnswer;
  search_memory: MemorySearchMatch[];
  summarize_history: HistorySummary;
};

export type AssistantActionResponse<TAction extends AssistantActionName> = {
  action: TAction;
  result: AssistantActionResultByName[TAction];
};

export type AssistantPreviewActionName = 'create_note' | 'create_plan' | 'create_reminder' | 'fix_note_grammar';

export type NoteChangePreview = {
  body: string;
  linkedResources: string;
  tags: string;
  title: string;
};

export type ReminderChangePreview = {
  calendarSyncEnabled: boolean;
  details: string;
  dueAt: string;
  relatedContext: string;
  title: string;
};

export type PlanChangePreview = {
  goal: string;
  items: string;
  linkedResources: string;
  title: string;
};

export type AssistantActionPreviewResponse = {
  action: AssistantPreviewActionName;
  changeType: 'create' | 'update';
  entityType: 'note' | 'plan' | 'reminder';
  preview: NoteChangePreview | PlanChangePreview | ReminderChangePreview;
  summary: string;
};

export type AssistantActionApplicationResponse = {
  action: AssistantPreviewActionName;
  changeType: 'create' | 'update';
  entity: Note | Plan | Reminder;
  entityType: 'note' | 'plan' | 'reminder';
  summary: string;
};

export type AiActionHistorySummary = {
  action: AssistantPreviewActionName;
  changeType: 'create' | 'update';
  createdAt: string;
  currentState: string;
  entityId: EntityId;
  entityType: 'note' | 'plan' | 'reminder';
  id: EntityId;
  ownerAccountId: EntityId;
  previousState: string | null;
  revertedAt: string | null;
  revertSummary: string | null;
  summary: string;
  workspaceContextId: EntityId;
};

export type AssistantMessage =
  | {
      id: string;
      role: 'user';
      text: string;
    }
  | {
      answer: SourceAwareAnswer;
      id: string;
      role: 'assistant';
    }
  | {
      id: string;
      preview: AssistantActionPreviewResponse;
      role: 'assistant';
    }
  | {
      id: string;
      role: 'assistant';
      text: string;
      type: 'clarification' | 'error' | 'status';
    };
