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

export type AssistantMode = 'answer' | 'summary';

export type AssistantMessage =
  | {
      id: string;
      mode: AssistantMode;
      role: 'user';
      text: string;
    }
  | {
      answer: SourceAwareAnswer;
      id: string;
      mode: 'answer';
      role: 'assistant';
    }
  | {
      id: string;
      mode: 'summary';
      role: 'assistant';
      summary: HistorySummary;
    }
  | {
      id: string;
      mode: AssistantMode;
      role: 'assistant';
      text: string;
      type: 'clarification' | 'error';
    };
