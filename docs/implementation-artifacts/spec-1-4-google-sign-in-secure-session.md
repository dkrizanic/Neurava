---
title: 'Google Sign-In And Secure Session'
type: 'feature'
created: '2026-05-29'
status: 'done'
epic: 1
story: 4
context:
  - '{project-root}/docs/implementation-artifacts/epic-1-context.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-2-backend-health-api-foundation.md'
  - '{project-root}/docs/implementation-artifacts/spec-1-3-frontend-app-shell.md'
---

<frozen-after-approval reason="human-owned intent - do not modify unless human renegotiates">

## Intent

**Problem:** The app has Spring Security dependencies and a frontend auth placeholder, but no user account/session contract exists yet. Future workspace-scoped features need a backend-owned authenticated session and a frontend way to detect signed-in state.

**Approach:** Add a Google OAuth2 login foundation that creates or retrieves an auth account on successful login, keeps OAuth tokens server-side, exposes a sanitized current-session API, and shows a signed-out/sign-in state in the React shell.

## Boundaries & Constraints

**Always:** Use backend-owned Spring Security sessions with HttpOnly cookies; never place OAuth tokens in frontend storage; keep app APIs under `/api/v1`; allow local startup without real Google credentials; keep account creation separate from Personal Context creation, which belongs to Story 1.5.

**Ask First:** Adding JWT auth, adding a custom auth provider beyond Google, or implementing workspace creation/switching requires human approval.

**Never:** Do not store OAuth access/refresh tokens in local storage, session storage, or frontend state; do not expose provider token payloads through APIs; do not build company registration or workspace provisioning in this story.

## I/O & Edge-Case Matrix

| Scenario | Input / State | Expected Output / Behavior | Error Handling |
|----------|--------------|---------------------------|----------------|
| Current anonymous session | `GET /api/v1/auth/session` without authentication | Returns `{ "authenticated": false }` | N/A |
| Current signed-in session | Authenticated user calls `GET /api/v1/auth/session` | Returns authenticated account summary without OAuth tokens | Missing optional profile fields return null |
| Login entry | User clicks sign in | Frontend navigates browser to backend Google OAuth entrypoint | OAuth provider errors stay backend-owned |
| OAuth success | Google OAuth principal contains provider id/email/name | Backend creates or updates account and establishes session cookie | Missing provider subject prevents account creation and fails safely |
| Protected API | Anonymous user calls future protected API | Backend requires authentication and frontend can show signed-out state | No stack traces or token data returned |

</frozen-after-approval>

## Code Map

- `apps/api/src/main/java/com/notebook/api/auth/**` -- auth account domain, persistence, OAuth success handling, and session API.
- `apps/api/src/main/java/com/notebook/api/shared/infrastructure/security/SecurityConfig.java` -- security filter chain, OAuth login, session/logout behavior.
- `apps/api/src/main/resources/db/migration/V3__auth_accounts.sql` -- persisted account table.
- `apps/api/src/main/resources/application.yml` and `application-local.yml` -- session cookie and OAuth-safe local configuration.
- `apps/api/src/test/java/com/notebook/api/auth/functional/AuthSessionFunctionalTest.java` -- MockMvc auth/session functional contract.
- `apps/api/src/test/java/com/notebook/api/auth/unit/AuthAccountServiceUnitTest.java` -- auth service unit behavior.
- `apps/web/src/app/providers/AuthProvider.tsx` -- current-session loading and sign-in/logout actions.
- `apps/web/src/features/auth/**` -- frontend auth API/types and signed-out prompt.
- `apps/web/src/app/App.tsx` and route screens -- display session-aware account/sign-in state.

## Tasks & Acceptance

**Execution:**
- [x] `apps/api/src/main/resources/db/migration/V3__auth_accounts.sql` -- add auth account table -- persists Google-backed accounts without token exposure.
- [x] `apps/api/src/main/java/com/notebook/api/auth/**` -- add account entity/repository/service and session controller -- creates/retrieves account and exposes current session.
- [x] `apps/api/src/main/java/com/notebook/api/shared/infrastructure/security/SecurityConfig.java` -- configure conditional OAuth2 login, session cookie behavior, logout, and protected route defaults -- establishes backend-owned sessions while local startup remains possible.
- [x] `apps/api/src/test/java/com/notebook/api/auth/infrastructure/web/AuthSessionApiContractTests.java` -- test anonymous session, authenticated session, no token leakage, and OAuth login success account creation.
- [x] `apps/web/src/app/providers/AuthProvider.tsx` and `apps/web/src/features/auth/**` -- fetch current session and expose sign-in/logout actions -- frontend can show signed-in or signed-out state.
- [x] `apps/web/src/app/App.tsx` and route screens -- show session-aware controls without implementing workspace behavior -- meets shell-level auth UX.

**Acceptance Criteria:**
- Given no user is authenticated, when the frontend loads, then it can fetch a session response showing the user is signed out.
- Given Google OAuth completes successfully, when the backend receives the OAuth principal, then it creates or retrieves an auth account and keeps the browser session authenticated.
- Given the frontend renders signed-out state, when the user chooses sign in, then the browser navigates to the backend Google OAuth entrypoint.
- Given session information is returned to the frontend, when the payload is inspected, then it contains only safe account metadata and no OAuth tokens.
- Given local development has no real Google secrets, when the app starts, then the backend still starts and exposes public system/session endpoints.

## Spec Change Log

## Result

- Added persisted `auth_account` records for Google-backed accounts.
- Added `/api/v1/auth/session` for anonymous and signed-in session state.
- Configured backend-owned session cookies, CORS with credentials, logout, and conditional OAuth2 login so local startup works without Google secrets.
- Added frontend auth session loading, sign-in navigation, logout, shell account controls, and a signed-out prompt.
- Added MockMvc session contract tests that verify anonymous state, account creation from an OAuth2 principal, and no OAuth token leakage.

## Verification

**Commands:**
- `.\mvnw.cmd -q -DskipTests compile` from `apps/api` -- expected: Java compiles.
- `npm run test:api:unit` -- expected: backend unit tests pass.
- `npm run test:api:functional` -- expected: backend functional tests pass.
- `npm run test:api:integration` -- expected: backend integration tests pass.
- `npm --prefix apps/web run test` -- expected: frontend auth state tests pass.
- `npm --prefix apps/web run build` -- expected: TypeScript and Vite build complete successfully.
- `.\mvnw.cmd -q test` from `apps/api` with PostgreSQL running -- expected: backend tests pass.
