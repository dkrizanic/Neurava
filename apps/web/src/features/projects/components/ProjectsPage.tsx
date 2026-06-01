import { FolderKanban, Sparkles } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { createProject, fetchProjects, summarizeProject } from '../api/projectsApi';
import type { Project, ProjectHistorySummary } from '../types';

export function ProjectsPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [projects, setProjects] = useState<Project[]>([]);
  const [form, setForm] = useState({ description: '', name: '', status: 'ACTIVE' as const });
  const [summary, setSummary] = useState<ProjectHistorySummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [summarizingId, setSummarizingId] = useState<string | null>(null);

  useEffect(() => {
    if (!authenticated) {
      setProjects([]);
      return;
    }
    const controller = new AbortController();
    setIsLoading(true);
    setError(null);
    fetchProjects(controller.signal)
      .then(setProjects)
      .catch((loadError: unknown) => {
        if (loadError instanceof DOMException && loadError.name === 'AbortError') {
          return;
        }
        setError('Projects could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });
    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id]);

  async function submitProject(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.name.trim()) {
      setError('Project name is required.');
      return;
    }
    try {
      const saved = await createProject({ ...form, name: form.name.trim() });
      setProjects((current) => [saved, ...current]);
      setForm({ description: '', name: '', status: 'ACTIVE' });
      setError(null);
    } catch {
      setError('Project could not be saved.');
    }
  }

  async function summarize(project: Project) {
    setSummarizingId(project.id);
    setError(null);
    try {
      setSummary(await summarizeProject(project.id));
    } catch {
      setError('Project summary could not be created.');
    } finally {
      setSummarizingId(null);
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Work structure</p>
          <h2>Projects</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Projects</h2>
        <p>Group business-context notes and summarize project history from linked source notes.</p>
      </header>

      {error ? <p className="session-warning">{error}</p> : null}

      <form className="note-composer" onSubmit={submitProject}>
        <Field label="Project name" name="project-name" onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} value={form.name} />
        <label className="field" htmlFor="project-description">
          <span className="field__label">Description</span>
          <textarea id="project-description" onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} value={form.description} />
        </label>
        <Button icon={<FolderKanban aria-hidden="true" size={18} />} type="submit" variant="primary">Create project</Button>
      </form>

      {summary ? (
        <section className="assistant-panel" aria-label="Project history summary">
          <div>
            <p className="eyebrow">Project summary</p>
            <h3>{summary.project.name}</h3>
          </div>
          <SummaryList title="Timeline" items={summary.timeline} />
          <SummaryList title="Decisions" items={summary.decisions} />
          <SummaryList title="Unresolved" items={summary.unresolvedItems} />
          <SummaryList title="Next actions" items={summary.nextActions} />
          <p className="muted-text">{summary.sources.length} source notes</p>
        </section>
      ) : null}

      {isLoading ? <LoadingState label="Loading projects" /> : null}
      {!isLoading && projects.length === 0 ? (
        <EmptyState description="Create a project in the active workspace." icon={<FolderKanban aria-hidden="true" size={24} />} title="No projects yet" />
      ) : null}
      <section className="notes-list notes-list--compact" aria-label="Project list">
        {projects.map((project) => (
          <article className="note-card note-card--compact" key={project.id}>
            <div className="note-card__heading">
              <h3>{project.name}</h3>
              <span className="note-badge">{project.status}</span>
            </div>
            <p>{project.description || 'No description yet'}</p>
            <div className="note-card__meta">
              <time dateTime={project.updatedAt}>{new Date(project.updatedAt).toLocaleString()}</time>
              <Button disabled={summarizingId === project.id} icon={<Sparkles aria-hidden="true" size={16} />} onClick={() => void summarize(project)} variant="secondary">
                {summarizingId === project.id ? 'Summarizing' : 'Summarize'}
              </Button>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

function SummaryList({ items, title }: { items: string[]; title: string }) {
  return (
    <div>
      <h4>{title}</h4>
      {items.length === 0 ? <p className="muted-text">None yet</p> : null}
      <ul>
        {items.map((item) => <li key={item}>{item}</li>)}
      </ul>
    </div>
  );
}

