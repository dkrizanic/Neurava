export type SectionId =
  | 'assistant'
  | 'integrations'
  | 'notes'
  | 'plans'
  | 'projects'
  | 'reminders'
  | 'search';

type AppSection = {
  id: SectionId;
  label: string;
  path: string;
  placeholder: string;
  status: string;
  summary: string;
};

export const sections: AppSection[] = [
  {
    id: 'notes',
    label: 'Notes',
    path: '/notes',
    placeholder: 'Note capture, editing, tagging, and source references will build on this route.',
    status: 'Core workspace',
    summary: 'Capture and organize working memory.',
  },
  {
    id: 'assistant',
    label: 'Assistant',
    path: '/assistant',
    placeholder: 'Assistant conversations, citations, and action previews will live here.',
    status: 'AI workspace',
    summary: 'Ask questions and turn notebook context into action.',
  },
  {
    id: 'search',
    label: 'Search',
    path: '/search',
    placeholder: 'Semantic and keyword search results will share this route.',
    status: 'Discovery',
    summary: 'Find remembered details across notes and connected context.',
  },
  {
    id: 'reminders',
    label: 'Reminders',
    path: '/reminders',
    placeholder: 'Reminder lists, synced due dates, and follow-up queues will appear here.',
    status: 'Planning',
    summary: 'Keep deadlines and follow-ups visible.',
  },
  {
    id: 'plans',
    label: 'Plans',
    path: '/plans',
    placeholder: 'Meeting, interview, project, and AI-assisted planning workflows will build from this view.',
    status: 'Calendar context',
    summary: 'Turn notes, calendar context, and project history into useful plans.',
  },
  {
    id: 'projects',
    label: 'Projects',
    path: '/projects',
    placeholder: 'Project folders, related notes, and decisions will be organized here.',
    status: 'Work structure',
    summary: 'Group notes, actions, and context by project.',
  },
  {
    id: 'integrations',
    label: 'Integrations',
    path: '/integrations',
    placeholder: 'Google Calendar, Gmail, and future source controls will be configured here.',
    status: 'Connected sources',
    summary: 'Manage the services that can enrich notebook context.',
  },
];
