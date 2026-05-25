---
stepsCompleted: [1, 2, 3, 4]
inputDocuments:
  - docs/planning-artifacts/prds/prd-notebook-2026-05-25/prd.md
  - docs/planning-artifacts/architecture.md
workflowType: epics-and-stories
lastStep: 4
status: complete
completedAt: 2026-05-25
---

# Notebook - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for Notebook, decomposing the requirements from the PRD and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: User can sign in with a Google account; the app creates or retrieves the user's account and provides a secure authenticated session.

FR2: Every user has a default Personal Context for notes, reminders, preparation plans, and AI conversations.

FR3: User can access a Business Context only after a registered company invites them for collaboration and they accept or belong to that context; registration includes normal user registration and a visible option to register a company.

FR4: The app scopes navigation, search, assistant responses, notes, reminders, projects, and AI conversations to the active Workspace Context.

FR5: User can create, edit, archive/delete, and view notes; notes persist and belong to a Workspace Context.

FR6: User can organize notes with tags/categories, favorites or pinned status, and relevant links to reminders, preparation plans, calendar events, or projects.

FR7: User can manually search note title and body content without AI.

FR8: User can write professional notes, preparation notes, and personal journal-style notes in a polished editor with autosave or clear save-state feedback, selectable writing mode, and examples/previews of modes.

FR9: User can ask natural-language questions across notes, reminders, projects, calendar context, and connected Gmail context, including weak-fragment searches.

FR10: AI answers must show Source References and allow the user to open referenced sources.

FR11: User can ask AI to summarize a time period, topic, project, preparation plan, or selected history, including important events, decisions, unresolved items, and next actions where relevant.

FR12: User can interact with the AI Assistant through natural language; assistant can search, summarize, organize, create notes, suggest tags, suggest reminders, draft plans, explain history, and ask clarifying questions.

FR13: AI Assistant can propose supported changes to app data, show a preview before applying, and apply changes when the user chooses to proceed; risky actions require explicit manual user action.

FR14: Every AI Action that changes data creates an AI Action History record with changed entities, previous state, current state, user-visible summary, timestamp, and revert capability; AI Action History is retained forever unless related data is deleted by policy or explicit user action.

FR15: Assistant should be designed around tool/action invocation for future MCP-style integrations, while MVP uses OpenAI API and does not include a public custom MCP marketplace.

FR16: User can connect Google Calendar with explicit permission; Personal Context MVP is read-only, supports upcoming calendar context, links to notes/reminders/preparation plans, and AI planning context.

FR17: User can explicitly opt into Smart Gmail integration; when enabled, AI can perform full Gmail search within granted permissions and must show Source References.

FR18: User can create reminders manually, and AI can create reminders as AI Actions; reminders can relate to notes, preparation plans, calendar context, or projects and sync to Google Calendar events after permission.

FR19: User can create a Preparation Plan for interviews, jobs, meetings, or projects; plans can include notes, tasks, reminders, calendar context, and AI-generated next steps.

FR20: User in a Business Context can use basic Projects and project notes; project history can be summarized by AI and remains scoped to the Business Context.

FR21: Business Context model should support future expansion into colleagues, feedback, progress tracking, and team calendar workflows without requiring full team workspace functionality in MVP.

FR22: The app works well on desktop and usable narrower web viewports; core workflows do not break layouts.

FR23: Core controls are accessible, clear, and consistent; important controls have accessible labels and loading/error/empty/permission states are designed.

FR24: User can understand and manage connected Google Calendar and Gmail permissions; Calendar and Gmail are optional and the app communicates what connected data may be used for AI and planning.

### NonFunctional Requirements

NFR1: The MVP must be production-ready and portfolio-grade rather than a rough prototype.

NFR2: The UI must be responsive and polished across desktop and usable narrow web viewports.

NFR3: Core workflows must provide accessible controls, labels, and states.

NFR4: Google integrations must be privacy-aware, permissioned, optional, and user-controlled.

NFR5: AI responses using retrieved content must provide source references for trust and traceability.

NFR6: AI edits must show preview before applying and remain visible and reversible after applying.

NFR7: Workspace Context scoping must apply across navigation, search, assistant behavior, notes, reminders, projects, and permissions.

NFR8: The system must avoid logging secrets, OAuth tokens, Gmail content, or sensitive OpenAI payloads.

NFR9: Integration failures from Google/OpenAI should be recoverable where possible and must not lose user data.

NFR10: Dates/times exchanged through APIs must use ISO 8601 UTC strings.

### Additional Requirements

- Scaffold a monorepo with `apps/web` and `apps/api`.
- Frontend starter: Vite React TypeScript.
- Backend starter: Spring Initializr with Java 21, Spring Boot 4, Maven, Spring Web, Security, OAuth2 Client, Data JPA, PostgreSQL, Flyway, Validation, Actuator, Docker Compose, Spring Modulith, and Spring AI OpenAI.
- Docker is required for local infrastructure and production-ready packaging.
- PostgreSQL with pgvector is used for MVP semantic retrieval.
- Use Flyway migrations, including pgvector extension setup.
- Backend is a DDD modular monolith using Spring Modulith boundaries.
- Every backend domain module must contain `application`, `domain`, and `infrastructure` layers.
- Domains are independent and must communicate through events, not direct access to another domain's internals.
- REST API base path is `/api/v1`.
- Backend session strategy uses HttpOnly, Secure, SameSite cookies.
- Google tokens are server-side and encrypted at rest if persisted.
- REST APIs use OpenAPI documentation and RFC 7807-style problem details.
- Frontend uses React Router, TanStack Query, Tailwind CSS, lucide-react, and TipTap editor.
- AI operations use preview/apply/revert command-style endpoints.
- AI Action History stores before/after state forever unless related data is deleted by policy or explicit user action.
- Docker Compose starts PostgreSQL with pgvector and local dependencies.
- GitHub Actions is recommended for CI after scaffold.
- Backend domain modules: `identity`, `workspace`, `notes`, `ai`, `search`, `reminders`, `preparation`, `projects`, `integrations`, `shared`.
- Frontend feature modules: `auth`, `workspace`, `notes`, `assistant`, `search`, `reminders`, `preparation`, `projects`, `integrations`, `shared`.

### UX Design Requirements

No separate UX Design document was found. UX requirements are extracted from PRD production quality requirements:

UX-DR1: Core workflows must have polished responsive layouts on desktop and usable narrower web viewports.

UX-DR2: Notes editor must support selectable writing modes and show examples/previews of how each mode looks.

UX-DR3: AI assistant UI must show sources, previews, applied changes, and revert actions.

UX-DR4: App must provide loading, error, empty, and permission states for core flows and integrations.

UX-DR5: Integration permission screens must clearly explain what Calendar and Gmail data may be used for AI and planning.

### FR Coverage Map

FR1: Epic 1 - Google sign-in and secure authenticated session.

FR2: Epic 1 - Default Personal Context.

FR3: Epic 1 and Epic 6 - Business Context registration/invite access and future business collaboration foundation.

FR4: Epic 1 and Epic 6 - Workspace-scoped navigation, search, assistant, data, and permissions.

FR5: Epic 2 - Note create/edit/archive/delete/view.

FR6: Epic 2 - Note organization with tags/categories, favorites/pins, and links.

FR7: Epic 2 - Manual note search.

FR8: Epic 2 - Professional editor with save feedback and selectable writing modes.

FR9: Epic 3 - Natural-language weak-fragment history search.

FR10: Epic 3 - Source-aware AI answers.

FR11: Epic 3 - AI history summaries.

FR12: Epic 3 - Conversational AI assistant.

FR13: Epic 4 - AI proposed changes with preview/apply flow.

FR14: Epic 4 - AI Action History and revert.

FR15: Epic 3 - MCP-ready/tool-action assistant architecture.

FR16: Epic 5 - Google Calendar connection and read-only personal calendar context.

FR17: Epic 5 - Smart Gmail opt-in and source-aware Gmail search.

FR18: Epic 5 - Manual and AI reminders with Google Calendar event sync.

FR19: Epic 5 - Preparation plans.

FR20: Epic 6 - Business-lite projects and project notes.

FR21: Epic 6 - Future business collaboration readiness.

FR22: Epic 1 and Epic 2 - Responsive polished UI foundation and note workflows.

FR23: Epic 1 and Epic 2 - Accessible controls and designed states.

FR24: Epic 1 and Epic 5 - Privacy and integration permission controls.

## Epic List

### Epic 1: Production Foundation And Account Context

Users can access a production-ready web app, sign in with Google, and work inside the correct Personal or Business context. This epic also establishes Docker, backend/frontend scaffold, database, DDD modules, security, and baseline UI shell because no later user value can work without it.

**FRs covered:** FR1, FR2, FR3, FR4, FR22, FR23, FR24

### Epic 2: Core Notebook Experience

Users can create, edit, organize, search, and manage notes in a polished notebook UI with workspace scoping and a professional editor.

**FRs covered:** FR5, FR6, FR7, FR8, FR22, FR23

### Epic 3: AI Memory Search And Source-Aware Assistant

Users can ask natural-language questions, find weak-fragment history, view source references, summarize history, and interact with the assistant.

**FRs covered:** FR9, FR10, FR11, FR12, FR15

### Epic 4: Safe AI Actions And Revertible Changes

Users can let AI propose and apply supported changes, preview edits, review AI Action History, and revert AI changes.

**FRs covered:** FR13, FR14

### Epic 5: Calendar, Gmail, Reminders, And Preparation Planning

Users can connect Google Calendar/Gmail, manage reminders, sync reminders to Google Calendar events, and build preparation plans with calendar and AI context.

**FRs covered:** FR16, FR17, FR18, FR19, FR24

### Epic 6: Business-Lite Projects And Future Collaboration Foundation

Invited business users can access minimal project notes/history while the architecture remains ready for future collaboration, colleagues, feedback, and progress tracking.

**FRs covered:** FR20, FR21, FR3, FR4

## Epic 1: Production Foundation And Account Context

Users can access a production-ready web app, sign in with Google, and work inside the correct Personal or Business context. This epic also establishes Docker, backend/frontend scaffold, database, DDD modules, security, and baseline UI shell because no later user value can work without it.

### Story 1.1: Scaffold Production Monorepo

As a developer,
I want the web app, API app, Docker infrastructure, and base project structure scaffolded,
So that the product has a production-ready foundation for all user-facing work.

**Acceptance Criteria:**

**Given** the repository is empty except BMAD docs
**When** the scaffold is created
**Then** the repo contains `apps/web` and `apps/api` following the architecture structure
**And** Docker Compose defines PostgreSQL with pgvector support
**And** root environment examples and README setup instructions exist
**And** the frontend and backend can be started independently in local development
**And** the backend contains empty DDD domain module folders for identity, workspace, notes, ai, search, reminders, preparation, projects, integrations, and shared

### Story 1.2: Backend Health And API Foundation

As a developer,
I want the API to expose health, version, and consistent error responses,
So that frontend and future stories can rely on a stable backend contract.

**Acceptance Criteria:**

**Given** the API app is running
**When** a client calls `/actuator/health`
**Then** the API returns a healthy status
**And** application configuration supports local profile and environment variables
**And** `/api/v1` is the base path for application APIs
**And** API validation/errors use RFC 7807-style problem details
**And** backend logs do not expose secrets or tokens

### Story 1.3: Frontend App Shell

As a user,
I want a polished responsive app shell,
So that I can navigate the product comfortably before individual features are added.

**Acceptance Criteria:**

**Given** the web app is running
**When** the user opens the app
**Then** the app displays a responsive layout with top-level navigation areas for notes, assistant/search, reminders, preparation, projects, and integrations
**And** the layout works on desktop and narrower web viewports without overlap
**And** shared UI components exist for buttons, forms, dialogs, loading states, and empty states
**And** the app has accessible labels for primary navigation and controls
**And** route-level loading and not-found/error states exist

### Story 1.4: Google Sign-In And Secure Session

As a user,
I want to sign in with Google,
So that I can securely access my notebook account.

**Acceptance Criteria:**

**Given** Google OAuth configuration is available
**When** the user chooses Google sign-in
**Then** the backend completes OAuth login using Spring Security OAuth2 Client
**And** the app creates or retrieves the user account
**And** the backend establishes an HttpOnly, Secure, SameSite session cookie
**And** the frontend can fetch the current user session
**And** unauthenticated users are redirected or shown a sign-in state
**And** OAuth tokens are never exposed to frontend local storage

### Story 1.5: Default Personal Context

As a signed-in user,
I want a Personal Context created automatically,
So that I can start using the notebook without joining a company.

**Acceptance Criteria:**

**Given** a user signs in for the first time
**When** account setup completes
**Then** the system creates a Personal Context for that user
**And** the frontend shows Personal as the active context
**And** protected APIs include Workspace Context scoping
**And** user data queries are limited to the active context
**And** no Business Context switcher is shown when the user has no business membership

### Story 1.6: Company Registration Entry And Business Context Availability

As a user,
I want to see an option to register a company and access Business Context only when eligible,
So that personal use stays simple while business collaboration is possible later.

**Acceptance Criteria:**

**Given** a signed-in user is in Personal Context
**When** the user opens account/workspace options
**Then** the app shows a visible option to register a company
**And** company registration can create a minimal registered company record
**And** Business Context becomes available only through company ownership, invite, or membership
**And** the workspace switcher appears only when more than one context is available
**And** Business Context data is separated from Personal Context data

## Epic 2: Core Notebook Experience

Users can create, edit, organize, search, and manage notes in a polished notebook UI with workspace scoping and a professional editor.

### Story 2.1: Create And View Workspace-Scoped Notes

As a signed-in user,
I want to create and view notes in my active workspace,
So that I can capture information in the right personal or business context.

**Acceptance Criteria:**

**Given** the user is signed in with an active Workspace Context
**When** the user creates a note
**Then** the note is saved in the active Workspace Context
**And** the note appears in the workspace note list
**And** the note has a title, body, created timestamp, updated timestamp, and owner/context metadata
**And** notes from other unavailable contexts are not returned
**And** empty note states are shown when no notes exist

### Story 2.2: Edit Notes With Autosave Feedback

As a signed-in user,
I want to edit notes with clear save feedback,
So that I can trust my writing is preserved.

**Acceptance Criteria:**

**Given** the user opens an existing note
**When** the user edits the title or body
**Then** changes are saved without requiring a full page reload
**And** the UI shows saving/saved/error save states
**And** the note updated timestamp changes after successful save
**And** failed saves show a recoverable error state without losing typed content

### Story 2.3: Archive And Restore Notes

As a signed-in user,
I want to archive and restore notes,
So that I can clean up my notebook without permanently losing content.

**Acceptance Criteria:**

**Given** the user owns a note in the active Workspace Context
**When** the user archives the note
**Then** the note is removed from the default active notes list
**And** the note remains retrievable in an archived view/filter
**And** the user can restore the note
**And** archived notes remain scoped to their Workspace Context

### Story 2.4: Organize Notes With Tags, Favorites, And Links

As a signed-in user,
I want to organize notes with tags, favorites, and relevant links,
So that I can keep related information easy to find.

**Acceptance Criteria:**

**Given** the user is editing or viewing a note
**When** the user adds tags/categories or marks the note as favorite/pinned
**Then** the note metadata is saved and visible in the UI
**And** the user can filter notes by tag/category and favorite/pinned status
**And** the user can link a note to available reminders, preparation plans, calendar events, or projects when those entities exist
**And** links respect Workspace Context boundaries

### Story 2.5: Manual Note Search And Filters

As a signed-in user,
I want to manually search and filter notes,
So that I can find content without using AI.

**Acceptance Criteria:**

**Given** the user has notes in the active Workspace Context
**When** the user searches by title or body text
**Then** matching notes are displayed
**And** non-matching notes are hidden
**And** the user can combine search with filters for tag/category, date, favorite/pinned, and archived state
**And** no-result states are clear and helpful
**And** search does not return notes from unavailable contexts

### Story 2.6: Selectable Professional Editor Modes

As a signed-in user,
I want to choose a writing mode and see examples of each mode,
So that I can write notes in the format that fits my workflow.

**Acceptance Criteria:**

**Given** the user opens the editor settings or first-use editor prompt
**When** writing mode options are shown
**Then** the app displays examples/previews of rich text and Markdown-style writing
**And** the user can choose a preferred writing mode
**And** the editor supports professional note writing and personal journal-style notes
**And** the selected mode is remembered for the user
**And** editor controls are keyboard accessible and labeled

## Epic 3: AI Memory Search And Source-Aware Assistant

Users can ask natural-language questions, find weak-fragment history, view source references, summarize history, and interact with the assistant.

### Story 3.1: AI Retrieval Index For Notes

As a signed-in user,
I want my notes to become searchable by meaning,
So that AI memory search can find useful history even when my query is vague.

**Acceptance Criteria:**

**Given** a note is created or updated
**When** the note content is saved
**Then** the system prepares searchable text for retrieval
**And** the system stores embeddings in PostgreSQL with pgvector
**And** retrieval records remain scoped to the note's Workspace Context
**And** indexing failures are logged safely without losing the note

### Story 3.2: Weak-Fragment AI Search

As a signed-in user,
I want to search using vague remembered fragments,
So that I can find past notes without remembering exact titles or dates.

**Acceptance Criteria:**

**Given** the user has indexed notes in the active Workspace Context
**When** the user asks a weak-fragment search question
**Then** the system returns relevant matches using semantic retrieval
**And** each match includes title, snippet, source type, and date
**And** results are limited to the active Workspace Context
**And** no-result states suggest ways to refine the query

### Story 3.3: Source-Aware AI Answers

As a signed-in user,
I want AI answers to show their sources,
So that I can trust and inspect the answer.

**Acceptance Criteria:**

**Given** AI uses retrieved content to answer a question
**When** the answer is displayed
**Then** the answer includes Source References
**And** each Source Reference can be opened from the UI
**And** the UI distinguishes answer text from source material
**And** the assistant indicates when it lacks enough source context

### Story 3.4: History Summary Generation

As a signed-in user,
I want AI to summarize a topic, project, preparation plan, or time period,
So that I can quickly regain context.

**Acceptance Criteria:**

**Given** relevant source content exists in the active Workspace Context
**When** the user asks for a history summary
**Then** the AI produces a structured summary with key events, decisions, unresolved items, and next actions where relevant
**And** the summary includes Source References
**And** the user can save the summary as a note

### Story 3.5: Conversational Assistant Panel

As a signed-in user,
I want a conversational assistant panel,
So that I can ask questions, refine results, and work from the same place.

**Acceptance Criteria:**

**Given** the user opens the assistant
**When** the user sends a natural-language message
**Then** the assistant shows conversation history for the current session
**And** the assistant can ask clarifying questions for broad or ambiguous requests
**And** the assistant clearly shows the active Workspace Context
**And** loading and error states are visible and recoverable

### Story 3.6: Tool-Action Assistant Foundation

As a developer,
I want assistant capabilities modeled as tool/actions,
So that future MCP-style integrations can be added without rewriting the assistant.

**Acceptance Criteria:**

**Given** assistant operations are implemented
**When** a capability such as search, summarize, or save summary is invoked
**Then** the backend routes it through an explicit tool/action abstraction
**And** tool/action inputs and outputs are typed
**And** unsupported actions return clear problem details
**And** the implementation does not hard-code the assistant as static chat only

## Epic 4: Safe AI Actions And Revertible Changes

Users can let AI propose and apply supported changes, preview edits, review AI Action History, and revert AI changes.

### Story 4.1: AI Change Preview

As a signed-in user,
I want to preview AI-proposed changes before applying them,
So that I stay in control of AI edits.

**Acceptance Criteria:**

**Given** the user asks AI to create or modify supported app data
**When** the AI produces a proposed change
**Then** the UI shows a preview before anything is applied
**And** the preview explains what entities would change
**And** the user can apply or cancel the proposed change
**And** preview generation does not persist final user-visible changes

### Story 4.2: Apply AI Actions

As a signed-in user,
I want to apply approved AI changes,
So that AI can help me create and organize notebook data.

**Acceptance Criteria:**

**Given** an AI change preview exists
**When** the user applies the change
**Then** the system persists the supported change
**And** supported changes include creating notes, reminders, preparation plans, project summaries, tags/categories, saved summaries, and entity links
**And** unsupported risky actions are refused or routed to explicit manual user action
**And** the UI shows what changed after applying

### Story 4.3: AI Action History Records

As a signed-in user,
I want AI edits recorded in history,
So that I can understand what AI changed.

**Acceptance Criteria:**

**Given** an AI action is applied
**When** the action completes
**Then** an AI Action History record is created
**And** the record includes changed entities, previous state, current state, summary, timestamp, user, and Workspace Context
**And** AI Action History is retained unless related data is deleted by policy or explicit user action
**And** manual user edits do not require full version history in MVP

### Story 4.4: Revert AI Actions

As a signed-in user,
I want to revert AI-applied changes,
So that I can safely undo AI edits I dislike.

**Acceptance Criteria:**

**Given** an AI Action History record supports revert
**When** the user chooses revert
**Then** the system restores the previous state for the affected entities
**And** the revert action is visible to the user
**And** the system prevents reverting when related data no longer exists and explains why
**And** revert respects Workspace Context permissions

### Story 4.5: One-Button Grammar Fix

As a note writer,
I want one-button AI grammar fixing,
So that I can polish notes quickly without leaving the editor.

**Acceptance Criteria:**

**Given** the user is editing a note
**When** the user selects grammar fix
**Then** AI generates a preview of the corrected text
**And** the user can compare the current and proposed text
**And** applying the fix creates an AI Action History record
**And** the user can revert the grammar fix

### Story 4.6: Generate Note From Messy Input

As a note writer,
I want AI to generate a clean note from messy input,
So that raw thoughts become useful structured notes.

**Acceptance Criteria:**

**Given** the user provides messy or unstructured text
**When** the user asks AI to generate a note
**Then** AI returns a structured note preview
**And** the preview can include title, body, suggested tags, and links when available
**And** applying the generated note saves it in the active Workspace Context
**And** the action can be reviewed and reverted

## Epic 5: Calendar, Gmail, Reminders, And Preparation Planning

Users can connect Google Calendar/Gmail, manage reminders, sync reminders to Google Calendar events, and build preparation plans with calendar and AI context.

### Story 5.1: Google Calendar Permission Connection

As a signed-in user,
I want to connect Google Calendar explicitly,
So that the app can use calendar context for planning.

**Acceptance Criteria:**

**Given** the user opens integration settings
**When** the user connects Google Calendar
**Then** the app explains requested permissions before authorization
**And** Calendar access is optional
**And** Personal Context MVP uses read-only calendar context
**And** the user can disconnect Calendar access

### Story 5.2: Calendar Context In App

As a signed-in user,
I want to view upcoming calendar context in Notebook,
So that planning can account for my schedule.

**Acceptance Criteria:**

**Given** Calendar is connected
**When** the user opens calendar-aware planning areas
**Then** upcoming calendar events are visible where relevant
**And** notes, reminders, and preparation plans can link to calendar events
**And** Calendar data is not shown when permission is absent or revoked
**And** Calendar errors show recoverable permission/error states

### Story 5.3: Smart Gmail Opt-In

As a signed-in user,
I want to explicitly opt into Smart Gmail,
So that AI can use email context only when I choose.

**Acceptance Criteria:**

**Given** the user opens integration settings
**When** the user enables Smart Gmail
**Then** the app explains what Gmail data may be searched and used
**And** Smart Gmail full search is never enabled silently
**And** the user can disconnect Smart Gmail
**And** Gmail content is not logged or exposed outside source-aware answers

### Story 5.4: Source-Aware Gmail Search For AI

As a signed-in user with Smart Gmail enabled,
I want AI to search relevant Gmail context,
So that email history can help with preparation and recall.

**Acceptance Criteria:**

**Given** Smart Gmail is enabled
**When** the user asks an AI question that needs email context
**Then** AI can search Gmail within granted permissions
**And** AI answers include Gmail Source References
**And** the user can open or identify the referenced email/thread source
**And** if Gmail is disabled the assistant explains that email context is unavailable

### Story 5.5: Manual Reminders With Google Calendar Sync

As a signed-in user,
I want to create reminders that sync to Google Calendar events,
So that my notebook plans appear on my calendar.

**Acceptance Criteria:**

**Given** the user creates a reminder
**When** Calendar sync is enabled
**Then** the reminder is saved in Notebook
**And** a corresponding Google Calendar event is created or linked
**And** sync state is visible to the user
**And** failed sync keeps the Notebook reminder and shows a recoverable error

### Story 5.6: AI-Created Reminders

As a signed-in user,
I want AI to create reminder previews from my requests,
So that I can quickly turn plans into scheduled actions.

**Acceptance Criteria:**

**Given** the user asks AI to create a reminder
**When** AI proposes the reminder
**Then** the user sees a preview with title, time/date, context link, and sync behavior
**And** applying the reminder creates an AI Action History record
**And** Calendar sync occurs if enabled
**And** the AI-created reminder can be reverted

### Story 5.7: Preparation Plans

As a job seeker or professional,
I want to create preparation plans,
So that I can organize interviews, meetings, jobs, or project work.

**Acceptance Criteria:**

**Given** the user opens preparation planning
**When** the user creates a plan
**Then** the plan can include notes, tasks, reminders, calendar context, and next steps
**And** plans are scoped to the active Workspace Context
**And** empty and loading states are designed

### Story 5.8: AI-Generated Preparation Plan

As a job seeker or professional,
I want AI to generate a preparation plan from my context,
So that I can get a practical plan quickly.

**Acceptance Criteria:**

**Given** the user provides a preparation goal
**When** the user asks AI to generate a plan
**Then** AI uses available notes and calendar context where permitted
**And** AI returns a preview with plan items, reminders, and next steps
**And** applying the plan creates required entities and AI Action History
**And** the user can edit or revert AI-created plan elements

## Epic 6: Business-Lite Projects And Future Collaboration Foundation

Invited business users can access minimal project notes/history while the architecture remains ready for future collaboration, colleagues, feedback, and progress tracking.

### Story 6.1: Business Context Entry For Eligible Users

As a business-invited user,
I want to switch into a Business Context,
So that I can keep business project information separate from personal notes.

**Acceptance Criteria:**

**Given** the user owns or belongs to a company context
**When** the user opens the workspace switcher
**Then** Business Context is available
**And** switching context changes visible notes, projects, assistant context, and search scope
**And** users without business membership do not see Business Context
**And** context changes are clearly visible in the UI

### Story 6.2: Business Project Records

As a business user,
I want to create basic project records,
So that project notes and history have a business home.

**Acceptance Criteria:**

**Given** the user is in Business Context
**When** the user creates a project
**Then** the project is saved in the active Business Context
**And** the project has a name, description, created timestamp, updated timestamp, and status
**And** project records are not visible in Personal Context
**And** empty project states are clear

### Story 6.3: Project Notes

As a business user,
I want to attach notes to projects,
So that project history stays organized.

**Acceptance Criteria:**

**Given** a project exists in Business Context
**When** the user creates or links a note to the project
**Then** the note appears in the project's note list
**And** the note remains workspace-scoped
**And** project note filters work by date and tag/category where available

### Story 6.4: Project History Summary

As a business user,
I want AI to summarize project history,
So that I can quickly understand decisions, unresolved items, and next actions.

**Acceptance Criteria:**

**Given** a project has related notes or history
**When** the user asks for a project history summary
**Then** AI returns a summary with timeline, decisions, unresolved items, and next actions
**And** the summary includes Source References
**And** the user can save the summary as a project note
**And** the summary respects Business Context permissions

### Story 6.5: Collaboration-Ready Business Model

As a product owner,
I want the Business Context model to support future collaboration,
So that later colleague tracking, feedback, progress tracking, and calendar workflows can be added cleanly.

**Acceptance Criteria:**

**Given** the business-lite model is implemented
**When** future collaboration features are planned
**Then** company, membership, project, and workspace boundaries already exist
**And** domain code does not assume all users are single-user personal users
**And** permissions are centralized enough to extend
**And** no MVP story requires full colleague tracking, feedback workflows, or shared company calendar visibility
