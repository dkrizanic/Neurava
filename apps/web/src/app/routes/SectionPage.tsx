import { Bell, Bot, CalendarRange, FileText, FolderKanban, Plug, Search } from 'lucide-react';
import { useMemo } from 'react';
import { useParams } from 'react-router';
import { Button, EmptyState } from '../../shared/ui';
import { sections } from '../sections';

const icons = {
  assistant: Bot,
  integrations: Plug,
  notes: FileText,
  plans: CalendarRange,
  projects: FolderKanban,
  reminders: Bell,
  search: Search,
};

export function SectionPage() {
  const { sectionId } = useParams();
  const section = useMemo(
    () => sections.find((item) => item.id === sectionId),
    [sectionId],
  );

  if (!section) {
    throw new Response('Section not found', { status: 404, statusText: 'Not Found' });
  }

  const Icon = icons[section.id];

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{section.status}</p>
        <h2>{section.label}</h2>
        <p>{section.summary}</p>
      </header>

      <EmptyState
        action={<Button variant="secondary">Prepare section</Button>}
        description={section.placeholder}
        icon={<Icon aria-hidden="true" size={24} />}
        title={`${section.label} is ready for its feature story`}
      />
    </div>
  );
}
