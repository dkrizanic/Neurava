---
title: 'Create And View Workspace-Scoped Notes'
type: 'feature'
created: '2026-05-30'
status: 'done'
epic: 2
story: 1
context:
  - '{project-root}/docs/planning-artifacts/epics.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-6-company-registration-entry-and-business-context-availability.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The app has authenticated workspace context but no real note capture or note list, so the core notebook route is still a placeholder.

**Approach:** Add a workspace-scoped note model, persistence, API contract, and Notes screen that lets a signed-in user create a note in the active workspace and view only notes from that workspace.

## Boundaries & Constraints

**Always:** Notes are scoped by active workspace id and owner account id; note APIs require authentication; note responses include title, body, timestamps, owner account id, and workspace context id; module boundaries stay strict by using scalar ids or application ports instead of importing workspace domain models.

**Ask First:** Autosave editing, archiving, tags, links, search, rich editor modes, and AI indexing are later stories.

**Never:** Do not return notes from unavailable contexts, create anonymous notes, add note sharing, or couple the notes domain to auth/workspace persistence entities.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Empty workspace | Signed-in user opens Notes with no notes | API returns empty list; UI shows empty state | N/A |
| Create note | Signed-in user submits title/body | API persists note in active workspace; UI prepends it to list | N/A |
| Invalid note | Blank title or too-long title | API returns validation problem details; UI keeps typed content | 400 with field errors |
| Workspace isolation | Same owner has notes in another workspace | List endpoint returns only active workspace notes | N/A |
| Anonymous access | No authenticated session | API rejects request; UI prompts sign-in | 401/403 from security |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V6__notes.sql` -- note table and workspace index.
- `apps/api/src/main/java/com/notebook/api/notes/**` -- note domain, application service, repository, and HTTP API.
- `apps/api/src/test/java/com/notebook/api/notes/**` -- note service unit tests and API contract coverage.
- `apps/web/src/features/notes/**` -- note API, types, and Notes route component.
- `apps/web/src/app/router.tsx` and `apps/web/src/app/routes/SectionPage.tsx` -- route `/notes` to the real Notes feature while leaving other sections as placeholders.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V6__notes.sql` -- add notes persistence -- records workspace and owner metadata.
- [x] `apps/api/src/main/java/com/notebook/api/notes/**` -- add create/list use case and controller -- exposes workspace-scoped note APIs.
- [x] `apps/api/src/test/java/com/notebook/api/notes/**` -- test create/list/validation/isolation -- protects the core contract.
- [x] `apps/web/src/features/notes/**` -- add Notes screen and API client -- gives the user create and empty/list states.
- [x] `apps/web/src/app/router.tsx` -- route notes to the feature page -- replaces the placeholder.

**Acceptance Criteria:**
- Given a signed-in user with an active workspace, when they create a note, then it is saved with that workspace and owner metadata.
- Given the user opens Notes, when notes exist in the active workspace, then only active-workspace notes are listed.
- Given no notes exist, when the user opens Notes, then an empty state is shown.
- Given the user is anonymous, when they open Notes, then they are asked to sign in and cannot create notes.

## Spec Change Log

## Result

- Added `note` persistence with owner account and workspace context metadata.
- Added a strict-module `notes` package with domain entity, application service, repository, and `/api/v1/notes` controller.
- Wired note creation/listing to authenticated account id and active workspace metadata without importing auth/workspace domain or infrastructure models.
- Replaced the `/notes` placeholder with a real Notes page supporting signed-out prompt, empty state, note composer, and note list.
- Added backend unit and functional note tests plus frontend Notes page tests.

## Verification

**Commands:**
- `npm run test:api:unit` -- expected: backend unit tests pass.
- `npm run test:api:functional` -- expected: backend functional tests pass when PostgreSQL is available.
- `npm --prefix apps/web run test` -- expected: frontend tests pass.
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
