const navItems = [
  'Notes',
  'Assistant',
  'Search',
  'Reminders',
  'Preparation',
  'Projects',
  'Integrations',
];

export function App() {
  return (
    <main className="app-shell">
      <aside className="sidebar" aria-label="Primary navigation">
        <div className="brand">
          <span className="brand-mark" aria-hidden="true">
            N
          </span>
          <div>
            <p className="eyebrow">Notebook</p>
            <h1>Personal</h1>
          </div>
        </div>

        <nav className="nav-list">
          {navItems.map((item) => (
            <button key={item} type="button" className="nav-item">
              {item}
            </button>
          ))}
        </nav>
      </aside>

      <section className="workspace" aria-labelledby="workspace-title">
        <header className="workspace-header">
          <div>
            <p className="eyebrow">Production scaffold</p>
            <h2 id="workspace-title">AI-native notebook foundation</h2>
          </div>
          <button type="button" className="primary-action">
            Sign in with Google
          </button>
        </header>

        <section className="hero-panel" aria-label="Notebook status">
          <div>
            <p className="eyebrow">Story 1.1</p>
            <h3>Monorepo, Docker, API, and web shell are ready.</h3>
            <p>
              The next stories can build authentication, workspace context, notes,
              AI actions, and integrations on top of this foundation.
            </p>
          </div>
        </section>

        <section className="status-grid" aria-label="Foundation checklist">
          {[
            ['Web', 'React + TypeScript + Vite'],
            ['API', 'Spring Boot + Java 21'],
            ['Data', 'PostgreSQL with pgvector'],
            ['Architecture', 'DDD modules with events'],
          ].map(([label, value]) => (
            <article key={label} className="status-card">
              <p>{label}</p>
              <strong>{value}</strong>
            </article>
          ))}
        </section>
      </section>
    </main>
  );
}
