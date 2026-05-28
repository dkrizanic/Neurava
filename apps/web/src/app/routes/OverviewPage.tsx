import { CalendarCheck, FileText, PlugZap, Search, Sparkles, Target } from 'lucide-react';
import { Link } from 'react-router';
import { Button, Field } from '../../shared/ui';
import { sections } from '../sections';

const highlights = [
  ['Active context', 'Personal workspace is the default home for future notes and AI actions.'],
  ['API contract', '/api/v1 is ready with problem-details error responses.'],
  ['Next foundation', 'Google sign-in and secure session are next in Epic 1.'],
];

const previewIcons = [FileText, Sparkles, Search, CalendarCheck, Target, PlugZap];

export function OverviewPage() {
  return (
    <div className="route-stack">
      <section className="overview-hero" aria-labelledby="overview-title">
        <div>
          <p className="eyebrow">Notebook</p>
          <h2 id="overview-title">A calm command center for notes, context, and AI-assisted work.</h2>
          <p>
            The shell is ready for the product routes that follow: notes, assistant and search,
            reminders, plans, projects, and integrations.
          </p>
        </div>
        <form className="quick-capture" aria-label="Quick capture preview">
          <Field
            description="Preview only. Capture behavior arrives in a later notes story."
            label="Quick capture"
            name="quick-capture"
            placeholder="Save an idea, meeting note, or follow-up"
          />
          <Button type="submit" variant="primary">
            Capture
          </Button>
        </form>
      </section>

      <section className="section-grid" aria-label="Top-level sections">
        {sections.map((section, index) => {
          const Icon = previewIcons[index] ?? FileText;

          return (
            <article className="section-card" key={section.path}>
              <Icon aria-hidden="true" size={22} />
              <h3>{section.label}</h3>
              <p>{section.summary}</p>
              <Link to={section.path}>Open {section.label.toLowerCase()}</Link>
            </article>
          );
        })}
      </section>

      <section className="status-grid" aria-label="Foundation status">
        {highlights.map(([label, value]) => (
          <article className="status-card" key={label}>
            <p>{label}</p>
            <strong>{value}</strong>
          </article>
        ))}
      </section>
    </div>
  );
}
