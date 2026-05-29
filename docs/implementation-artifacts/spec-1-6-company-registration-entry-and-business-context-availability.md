---
title: 'Company Registration Entry And Business Context Availability'
type: 'feature'
created: '2026-05-29'
status: 'done'
epic: 1
story: 6
context:
  - '{project-root}/docs/implementation-artifacts/epic-1-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-5-default-personal-context.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** Signed-in users can work in Personal Context, but there is no entry point for registering a company or making Business Context available only to eligible accounts.

**Approach:** Add a minimal company registration flow that persists a registered company, creates a Business Context owned by the registering account, adds the owner membership, and exposes the option from workspace/account settings while keeping Personal Context simple until the user opts in.

## Boundaries & Constraints

**Always:** Company registration requires an authenticated account; Business Context availability is based on ownership or membership; Personal and Business workspaces must remain separate workspace records; the workspace switcher appears only when more than one context is available.

**Ask First:** Invites, collaboration roles beyond owner/member, manual active-workspace switching, billing/legal company verification, and business data features require human approval because they belong to later stories.

**Never:** Do not expose OAuth tokens, create Business Contexts automatically on sign-in, build note/project data, or make Business Context visible to users without ownership or membership.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Register from Personal | Signed-in user posts company name | API returns company and Business workspace metadata; account now has multiple workspace memberships | N/A |
| Repeat registration | Same owner already has a company/business workspace | API returns the existing company/workspace without duplicate memberships | N/A |
| Invalid name | Blank or too-long company name | API returns validation problem details | 400 with field errors |
| Anonymous registration | No authenticated session | Request is rejected before registration | 401/403 from security |
| Shell settings | Signed-in Personal Context user opens settings | Register company action is visible; switcher remains hidden until API session has multiple contexts | N/A |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/resources/db/migration/V5__registered_companies.sql` -- registered company table linked to owner account and Business workspace.
- `apps/api/src/main/java/com/notebook/api/workspace/domain/**` -- company entity and Business workspace creation behavior.
- `apps/api/src/main/java/com/notebook/api/workspace/application/**` -- registration use case and response shape.
- `apps/api/src/main/java/com/notebook/api/workspace/infrastructure/web/CompanyRegistrationController.java` -- `/api/v1/workspaces/companies` contract.
- `apps/api/src/test/java/com/notebook/api/workspace/**` -- unit and functional registration coverage.
- `apps/web/src/features/workspace/**` -- frontend registration API and settings dialog UI.
- `apps/web/src/app/App.tsx` and `apps/web/src/app/styles/globals.css` -- expose workspace settings action in the shell.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V5__registered_companies.sql` -- add company persistence -- provides a durable company record without mixing it into auth accounts.
- [x] `apps/api/src/main/java/com/notebook/api/workspace/**` -- add company registration use case -- creates/reuses Business Context and owner membership.
- [x] `apps/api/src/test/java/com/notebook/api/workspace/**` -- cover happy path, repeat registration, and validation -- protects workspace eligibility rules.
- [x] `apps/web/src/features/workspace/**` -- add registration API and settings dialog -- gives signed-in users the account/workspace option.
- [x] `apps/web/src/app/App.tsx` -- open workspace settings from the shell -- keeps the feature discoverable from Personal Context.

**Acceptance Criteria:**
- Given a signed-in user is in Personal Context, when they open workspace options, then the app shows a register company action.
- Given a signed-in user registers a company, when registration succeeds, then the backend creates or reuses a minimal company record and Business Context owner membership.
- Given an account has both Personal and Business memberships, when the session is fetched, then workspace switching is marked available.
- Given a user has no Business membership, when the shell renders, then no Business switcher is shown.
- Given Business Context exists, when later data is stored, then it can be scoped separately from Personal by workspace context id.

## Spec Change Log

## Result

- Added registered company persistence linked to owner account and Business workspace.
- Added Business workspace creation/reuse plus owner membership registration.
- Added `/api/v1/workspaces/companies` for authenticated company registration.
- Added workspace settings dialog in the React shell with company registration.
- Added backend unit and functional coverage for registration behavior and frontend tests for the dialog.
- Tightened module isolation by replacing direct workspace-to-auth domain references with scalar account IDs, publishing account/company events, adding a workspace listener for Personal Context setup, and adding a Spring Modulith boundary test.

## Verification

**Commands:**
- `npm run test:api:unit` -- expected: backend unit tests pass.
- `npm run test:api:functional` -- expected: backend functional tests pass. Blocked locally because Docker Desktop/PostgreSQL was unavailable.
- `npm --prefix apps/web run test` -- expected: frontend tests pass.
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
