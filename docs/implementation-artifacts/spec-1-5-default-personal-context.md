---
title: 'Default Personal Context'
type: 'feature'
created: '2026-05-29'
status: 'done'
epic: 1
story: 5
context:
  - '{project-root}/docs/implementation-artifacts/epic-1-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-4-google-sign-in-secure-session.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Signed-in users can now have an auth account, but the app does not yet create or expose the default Personal Context that future notes, reminders, AI, plans, and projects must be scoped to.

**Approach:** Persist workspace contexts and memberships, automatically ensure exactly one Personal Context for each signed-in account, return it in the current session contract, and show Personal as the active context in the frontend shell without a business switcher when there is only one context.

## Boundaries & Constraints

**Always:** Every signed-in account gets a Personal Context automatically; Personal is the default active context; future protected resources must be designed to require workspace context; session responses must expose only safe workspace metadata.

**Ask First:** Business Context creation, company registration, invites, or manual workspace switching require human approval because they belong to Story 1.6.

**Never:** Do not create Business Contexts, invite flows, company tables, note data, or cross-workspace query behavior in this story.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| First signed-in session | OAuth account exists with no workspace | Backend creates Personal Context and returns it as active workspace | Creation is transactional with account/session handling |
| Repeat signed-in session | OAuth account already has Personal Context | Backend reuses existing Personal Context without duplicates | Existing membership is returned |
| Anonymous session | No authentication | Session remains signed out with no workspace | N/A |
| Frontend signed-in shell | Session includes one Personal Context | Shell shows Personal active context and no workspace switcher | Missing workspace shows signed-in account but no switcher |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V4__workspace_contexts.sql` -- workspace context and membership schema.
- `apps/api/src/main/java/com/notebook/api/workspace/**` -- workspace domain, persistence, and Personal Context service.
- `apps/api/src/main/java/com/notebook/api/auth/application/CurrentSession.java` -- session shape with active workspace.
- `apps/api/src/main/java/com/notebook/api/auth/application/CurrentSessionService.java` -- ensures Personal Context for OAuth accounts.
- `apps/api/src/test/java/com/notebook/api/auth/infrastructure/web/AuthSessionApiContractTests.java` -- functional session contract coverage.
- `apps/web/src/features/auth/types.ts` -- frontend session type with active workspace metadata.
- `apps/web/src/app/App.tsx` and `apps/web/src/features/auth/components/AuthStatus.tsx` -- active context display and no switcher for single context.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V4__workspace_contexts.sql` -- add workspace context and membership tables -- persists Personal Context separately from auth account.
- [x] `apps/api/src/main/java/com/notebook/api/workspace/**` -- add domain/repository/service for ensuring Personal Context -- centralizes workspace-scoping foundation.
- [x] `apps/api/src/main/java/com/notebook/api/auth/application/CurrentSession*.java` -- return active workspace and switcher availability -- frontend can render context state.
- [x] `apps/api/src/test/java/com/notebook/api/auth/infrastructure/web/AuthSessionApiContractTests.java` -- test first sign-in creation, repeat reuse, anonymous shape, and no duplicate Personal Context.
- [x] `apps/web/src/features/auth/types.ts` and shell components -- render active Personal Context and hide switcher when only one context exists -- satisfies user-facing story.

**Acceptance Criteria:**
- Given a user signs in for the first time, when session/account setup completes, then the system creates a Personal Context for that user.
- Given the same user fetches session again, when Personal Context already exists, then the system reuses it and does not create a duplicate.
- Given the frontend receives a signed-in session, when the shell renders, then it shows Personal as the active context.
- Given the user has no business membership, when the shell renders workspace controls, then no Business Context switcher is shown.
- Given future protected APIs are built, when they need scoping, then session data includes active workspace metadata they can use as the foundation.

## Spec Change Log

## Result

- Added `workspace_context` and `workspace_membership` persistence.
- Added workspace domain, repositories, and `PersonalWorkspaceService`.
- Extended the current session response with active workspace metadata and switcher availability.
- Ensured signed-in OAuth sessions create/reuse a Personal Context transactionally.
- Updated frontend session types and shell controls to show Personal as active and hide switching for one workspace.
- Added backend contract coverage for anonymous session, Personal Context creation, safe session payloads, and repeat-session reuse.

## Verification

**Commands:**
- `.\mvnw.cmd -q test` from `apps/api` with PostgreSQL running -- expected: backend tests pass.
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
