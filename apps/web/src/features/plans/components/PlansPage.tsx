import { CalendarRange, CheckCircle2, Sparkles } from 'lucide-react';
import { useEffect, useState, type FormEvent } from 'react';
import { SignedOutPrompt } from '../../auth/components/SignedOutPrompt';
import { useAuth } from '../../auth/hooks/useAuth';
import { Button, EmptyState, Field, LoadingState } from '../../../shared/ui';
import { applyPlanPreview, previewCreatePlan } from '../../assistant/api/assistantApi';
import { createPlan, fetchPlans, updatePlanStatus } from '../api/plansApi';
import type { Plan, UpsertPlanInput } from '../types';
import type { PlanChangePreview } from '../../assistant/types';

export function PlansPage() {
  const { activeWorkspace, authenticated } = useAuth();
  const [plans, setPlans] = useState<Plan[]>([]);
  const [form, setForm] = useState({ goal: '', items: '', linkedResources: '', title: '' });
  const [aiGoal, setAiGoal] = useState('');
  const [preview, setPreview] = useState<PlanChangePreview | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!authenticated) {
      setPlans([]);
      return;
    }
    const controller = new AbortController();
    setIsLoading(true);
    setError(null);
    fetchPlans(controller.signal)
      .then(setPlans)
      .catch((loadError: unknown) => {
        if (loadError instanceof DOMException && loadError.name === 'AbortError') {
          return;
        }
        setError('Plans could not be loaded.');
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setIsLoading(false);
        }
      });
    return () => controller.abort();
  }, [authenticated, activeWorkspace?.id]);

  async function submitPlan(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form.title.trim()) {
      setError('Title is required.');
      return;
    }
    const input: UpsertPlanInput = { ...form, title: form.title.trim() };
    try {
      const saved = await createPlan(input);
      setPlans((current) => [saved, ...current]);
      setForm({ goal: '', items: '', linkedResources: '', title: '' });
      setError(null);
    } catch {
      setError('Plan could not be saved.');
    }
  }

  async function previewAiPlan() {
    if (!aiGoal.trim()) {
      setError('AI plan goal is required.');
      return;
    }
    try {
      const response = await previewCreatePlan(aiGoal);
      setPreview(response.preview as PlanChangePreview);
      setError(null);
    } catch {
      setError('AI plan preview could not be created.');
    }
  }

  async function applyAiPlan() {
    if (!preview) {
      return;
    }
    try {
      const response = await applyPlanPreview(preview);
      setPlans((current) => [response.entity as Plan, ...current]);
      setPreview(null);
      setAiGoal('');
    } catch {
      setError('AI plan could not be applied.');
    }
  }

  async function completePlan(plan: Plan) {
    try {
      const saved = await updatePlanStatus(plan.id, plan.status === 'COMPLETED' ? 'ACTIVE' : 'COMPLETED');
      setPlans((current) => current.map((item) => (item.id === saved.id ? saved : item)));
    } catch {
      setError('Plan could not be updated.');
    }
  }

  if (!authenticated) {
    return (
      <div className="route-stack">
        <header className="route-heading">
          <p className="eyebrow">Calendar context</p>
          <h2>Plans</h2>
        </header>
        <SignedOutPrompt />
      </div>
    );
  }

  return (
    <div className="route-stack">
      <header className="route-heading">
        <p className="eyebrow">{activeWorkspace?.name ?? 'Active'} workspace</p>
        <h2>Plans</h2>
        <p>Create interview, meeting, job, or project plans with notes, tasks, reminders, and calendar context.</p>
      </header>

      {error ? <p className="session-warning">{error}</p> : null}

      <form className="note-composer" onSubmit={submitPlan}>
        <Field label="Title" name="plan-title" onChange={(event) => setForm((current) => ({ ...current, title: event.target.value }))} value={form.title} />
        <label className="field" htmlFor="plan-goal">
          <span className="field__label">Goal</span>
          <textarea id="plan-goal" onChange={(event) => setForm((current) => ({ ...current, goal: event.target.value }))} value={form.goal} />
        </label>
        <label className="field" htmlFor="plan-items">
          <span className="field__label">Items</span>
          <textarea id="plan-items" onChange={(event) => setForm((current) => ({ ...current, items: event.target.value }))} value={form.items} />
        </label>
        <Field label="Linked resources" name="plan-links" onChange={(event) => setForm((current) => ({ ...current, linkedResources: event.target.value }))} value={form.linkedResources} />
        <Button icon={<CalendarRange aria-hidden="true" size={18} />} type="submit" variant="primary">Create plan</Button>
      </form>

      <section className="assistant-panel" aria-label="AI plan creator">
        <label className="field" htmlFor="ai-plan">
          <span className="field__label">AI plan goal</span>
          <textarea id="ai-plan" onChange={(event) => setAiGoal(event.target.value)} value={aiGoal} />
        </label>
        <Button icon={<Sparkles aria-hidden="true" size={18} />} onClick={() => void previewAiPlan()} type="button" variant="secondary">Preview AI plan</Button>
        {preview ? (
          <div className="ai-preview">
            <h3>{preview.title}</h3>
            <p>{preview.goal}</p>
            <pre>{preview.items}</pre>
            <Button onClick={() => void applyAiPlan()} type="button" variant="primary">Apply plan</Button>
          </div>
        ) : null}
      </section>

      {isLoading ? <LoadingState label="Loading plans" /> : null}
      {!isLoading && plans.length === 0 ? (
        <EmptyState description="Create a plan manually or generate one from a goal." icon={<CalendarRange aria-hidden="true" size={24} />} title="No plans yet" />
      ) : null}
      <section className="notes-list notes-list--compact" aria-label="Plan list">
        {plans.map((plan) => (
          <article className="note-card note-card--compact" key={plan.id}>
            <div className="note-card__heading">
              <h3>{plan.title}</h3>
              <span className="note-badge">{plan.status}</span>
            </div>
            <p>{plan.goal || 'No goal yet'}</p>
            {plan.items ? <pre>{plan.items}</pre> : null}
            <div className="note-card__meta">
              <time dateTime={plan.updatedAt}>{new Date(plan.updatedAt).toLocaleString()}</time>
              <Button icon={<CheckCircle2 aria-hidden="true" size={16} />} onClick={() => void completePlan(plan)} variant="secondary">
                {plan.status === 'COMPLETED' ? 'Reopen' : 'Complete'}
              </Button>
            </div>
          </article>
        ))}
      </section>
    </div>
  );
}

