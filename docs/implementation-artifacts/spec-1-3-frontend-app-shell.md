---
title: 'Frontend App Shell'
type: 'feature'
created: '2026-05-29'
status: 'done'
epic: 1
story: 3
context:
  - '{project-root}/docs/implementation-artifacts/epic-1-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-2-backend-health-api-foundation.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The web app is still a scaffold-level static page, so future features do not yet have a stable navigation, route, or shared UI foundation to build on.

**Approach:** Build a polished, responsive React app shell with top-level feature routes, accessible navigation, route loading/error/not-found states, and reusable shared UI primitives for buttons, forms, dialogs, loading states, and empty states.

## Boundaries & Constraints

**Always:** Keep the work inside `apps/web`; follow the architecture's feature-first frontend structure; use React Router for route ownership; keep the UI responsive down to narrow web viewports; expose accessible labels for primary navigation and controls; keep shared UI components independent of feature modules.

**Ask First:** Adding a full design system package, introducing authentication behavior, or replacing the Vite/React foundation requires human approval.

**Never:** Do not implement Google sign-in, workspace context behavior, notes CRUD, real assistant/search behavior, reminders scheduling, project management, or integration connections in this story.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Open app shell | User opens `/` | Shell renders with sidebar/header areas and route content for the overview | N/A |
| Navigate feature route | User selects notes, assistant/search, reminders, plans, projects, or integrations | URL changes and route content updates without a full page reload | Unknown routes render a not-found route |
| Narrow viewport | Viewport is tablet/mobile width | Navigation and content reflow without text/control overlap | N/A |
| Route pending state | Lazy route is loading | A route-level loading state is visible and labelled | N/A |
| Route error | A route throws during render | Error boundary shows a sanitized route error state with recovery navigation | Raw stack traces are not shown |

</frozen-after-approval>

## Code Map

- `apps/web/package.json` -- frontend dependencies and build scripts.
- `apps/web/src/main.tsx` -- router provider entrypoint.
- `apps/web/src/app/router.tsx` -- top-level route definitions and route error handling.
- `apps/web/src/app/App.tsx` -- responsive app shell layout and route outlet.
- `apps/web/src/app/styles/globals.css` -- app-wide responsive shell and shared UI styling.
- `apps/web/src/app/routes/*.tsx` -- route screens for overview, feature placeholders, loading/error, and not-found states.
- `apps/web/src/shared/ui/*` -- shared UI primitives for buttons, forms, dialogs, loading states, and empty states.

## Tasks & Acceptance

**Execution:**
- [x] `apps/web/package.json` -- add React Router dependency -- route-level ownership is required by architecture.
- [x] `apps/web/src/app/router.tsx` and `apps/web/src/main.tsx` -- replace placeholder route list with browser router setup -- enables actual SPA navigation and error elements.
- [x] `apps/web/src/app/App.tsx` and `apps/web/src/app/styles/globals.css` -- implement responsive shell with primary navigation and content outlet -- satisfies story shell and viewport requirements.
- [x] `apps/web/src/app/routes/*.tsx` -- add overview, feature placeholder, loading, error, and not-found route screens -- gives future stories stable route states.
- [x] `apps/web/src/shared/ui/*` -- add Button, Field, Dialog, LoadingState, and EmptyState primitives -- establishes shared component foundation.
- [x] Verify the I/O matrix through build and manual viewport inspection.

**Acceptance Criteria:**
- Given the web app is running, when the user opens it, then it displays top-level navigation areas for notes, assistant/search, reminders, plans, projects, and integrations.
- Given the user navigates through top-level sections, when a section is selected, then the URL and visible content match the selected section.
- Given the viewport is desktop or narrow web width, when the shell renders, then navigation, controls, and text do not overlap.
- Given route content is loading or unknown, when the route resolves or fails to match, then accessible loading and not-found/error states are shown.
- Given shared frontend code is inspected, when future features need controls, then shared primitives exist for buttons, forms, dialogs, loading states, and empty states.

## Spec Change Log

## Result

- Replaced the static scaffold page with a real React Router app shell.
- Added accessible primary navigation for notes, assistant, search, reminders, plans, projects, and integrations.
- Added lazy route rendering with a shared loading state, sanitized route error handling, and a not-found page.
- Added shared UI primitives for buttons, form fields, dialogs, loading states, and empty states.
- Updated responsive styling so the shell reflows from sidebar layout to stacked narrow viewport layout without overlapping controls.

## Verification

**Commands:**
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
