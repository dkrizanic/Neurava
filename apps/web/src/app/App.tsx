import { Bell, Bot, CalendarRange, FileText, FolderKanban, Plug, Search, Settings } from 'lucide-react';
import { NavLink, Outlet } from 'react-router';
import { AuthStatus } from '../features/auth/components/AuthStatus';
import { useAuth } from '../features/auth/hooks/useAuth';
import { sections, type SectionId } from './sections';

const navIcons: Record<SectionId, typeof FileText> = {
  assistant: Bot,
  integrations: Plug,
  notes: FileText,
  plans: CalendarRange,
  projects: FolderKanban,
  reminders: Bell,
  search: Search,
};

export function App() {
  const { activeWorkspace } = useAuth();

  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <NavLink className="brand" to="/" aria-label="Notebook overview">
          <span className="brand-mark" aria-hidden="true">
            N
          </span>
          <span>
            <span className="eyebrow">Notebook</span>
            <span className="brand-title">Personal</span>
          </span>
        </NavLink>

        <nav className="nav-list" aria-label="Product sections">
          {sections.map((item) => {
            const Icon = navIcons[item.id];

            return (
              <NavLink
                aria-label={`Open ${item.label}`}
                className={({ isActive }) => `nav-item${isActive ? ' nav-item--active' : ''}`}
                key={item.path}
                to={item.path}
              >
                <Icon aria-hidden="true" size={18} />
                <span>{item.label}</span>
              </NavLink>
            );
          })}
        </nav>
      </aside>

      <section className="workspace" aria-labelledby="workspace-title">
        <header className="workspace-header">
          <div>
            <p className="eyebrow">Production app shell</p>
            <h1 id="workspace-title">{activeWorkspace?.name ?? 'Personal'} workspace</h1>
          </div>
          <div className="workspace-actions">
            <AuthStatus />
            <button className="icon-button" type="button" aria-label="Open workspace settings">
              <Settings aria-hidden="true" size={20} />
            </button>
          </div>
        </header>

        <div className="workspace-body">
          <Outlet />
        </div>
      </section>
    </main>
  );
}
