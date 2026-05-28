---
title: Notebook
status: final
created: 2026-05-25
updated: 2026-05-25
---

# PRD: Notebook

## 0. Document Purpose

This PRD defines the MVP for Notebook, a production-ready public web application intended for strong portfolio presentation and real user use. It captures product goals, target users, MVP scope, non-goals, user journeys, and functional requirements for downstream BMAD architecture, UX, and story creation. Technical preferences already stated by the owner are captured as constraints, but detailed implementation design belongs in the architecture phase.

## 1. Vision

Notebook is an AI-native memory and planning workspace for professionals who need to capture, retrieve, organize, and act on personal and work history. It combines core notebook workflows with an assistant that can understand weak fragments, search across user history, summarize past context, create plans, and update supported app data.

The product should feel like the smartest notebook on the web: a user should be able to say, "find that thing from last month where we talked about the API issue," and the app should locate the relevant note, project, meeting, calendar context, or email source with enough confidence and traceability to be useful.

The long-term vision is an all-in-one personal and business workspace for notes, AI memory, reminders, planning, project history, collaboration, feedback, and scheduling. The MVP focuses on a hybrid first release: a polished personal/professional AI notebook with minimal business collaboration foundations.

## 2. Target User

### 2.1 Primary Persona

The primary MVP user is a programmer, job seeker, student developer, freelancer, or technical professional who manages interview planning, project notes, daily learning, work history, reminders, calendar events, and personal notes in one place.

They need a tool that helps them remember what happened, prepare for upcoming work, retrieve small details from the past, and turn scattered information into useful next actions.

### 2.2 Jobs To Be Done

- When I remember only a small fragment from the past, I want the app to find the relevant note, meeting, project, or email context so I do not waste time searching manually.
- When I prepare for an interview, job application, project, or meeting, I want notes, reminders, and calendar context in one place so I can stay organized.
- When I return to an old project, I want a history summary and next actions so I can quickly regain context.
- When I want to organize my notes, I want AI assistance that can tag, summarize, group, or create content with minimal input.
- When I use personal and business contexts, I want fast switching and clear separation so private and work information do not blur.

### 2.3 Non-Users For MVP

- Companies needing a complete team workspace with deep permissions, colleague tracking, and shared calendar visibility.
- Users needing native mobile apps in the first release.
- Users needing full enterprise administration, compliance tooling, or backup/restore management in MVP.

### 2.4 Key User Journeys

- **UJ-1. Weak-fragment history retrieval.**
  - **Persona + context:** A programmer remembers a small detail from a prior project or meeting but not the title, date, or exact note.
  - **Entry state:** The user is signed in and working in Personal or an invited Business context.
  - **Path:** The user opens AI search or assistant, describes the fragment in natural language, reviews suggested matching sources, opens the most relevant note/project/event/email, and optionally asks for a summary.
  - **Climax:** The app finds the right historical context and shows sources so the user trusts the result.
  - **Resolution:** The user can continue from the found context, create a reminder, or ask the assistant for next actions.

- **UJ-2. Professional planning.**
  - **Persona + context:** A job seeker wants to prepare for an interview week.
  - **Entry state:** The user has notes, reminders, and optionally Google Calendar connected.
  - **Path:** The user asks the assistant to create a plan, the app uses relevant notes and calendar context, the assistant creates plan items/reminders, and the user reviews what changed.
  - **Climax:** The user gets a practical plan with notes, reminders, calendar-aware scheduling, and next steps.
  - **Resolution:** The user can follow the plan, edit it manually, or revert AI-created changes.

- **UJ-3. Project history summary and next actions.**
  - **Persona + context:** A professional returns to a project after time away.
  - **Entry state:** The project has notes, history, and possibly linked calendar/email context.
  - **Path:** The user asks for a project history summary, the assistant gathers relevant sources, produces a timeline, highlights decisions, identifies unresolved items, and suggests next actions.
  - **Climax:** The user understands what happened and what to do next without rereading every note.
  - **Resolution:** The user can save the summary, create reminders/tasks, or continue working in the project context.

## 3. Glossary

- **Notebook** - The overall product.
- **User** - A signed-in person using Notebook.
- **Personal Context** - The default private workspace for a user.
- **Business Context** - A company collaboration workspace available only after an invited collaboration or membership.
- **Workspace Context** - Either Personal Context or Business Context; scopes navigation, search, assistant behavior, and permissions.
- **Note** - A user-created record containing professional, personal, planning, or history content.
- **Project** - A Business Context or professional planning record that groups related notes, history, and summaries.
- **Reminder** - A time-based or context-based item the user or assistant creates to prompt future action.
- **Plan** - A structured plan for interviews, jobs, meetings, projects, or professional planning.
- **AI Assistant** - The conversational interface that can search, summarize, organize, create, and modify supported app data.
- **AI Action** - A data-changing operation performed by the AI Assistant.
- **AI Action History** - The record of an AI Action, including changed entities, previous state, current state, visible summary, and revert capability.
- **Source Reference** - A note, project, calendar event, Gmail thread, date, or other item used to support an AI answer.

## 4. Features

### 4.1 Authentication And Workspace Contexts

**Description:** Users sign in with Google and begin in their Personal Context. Business Context is only available when a registered company invites the user for collaboration and the user accepts or belongs to that company context. Workspace switching appears only when more than one context is available.

**Functional Requirements:**

#### FR-1: Google sign-in

User can sign in with a Google account.

**Consequences:**
- The app creates or retrieves the user's account after successful authentication.
- The app provides a secure authenticated session.

#### FR-2: Default Personal Context

Every user has a Personal Context by default.

**Consequences:**
- New users can create notes, reminders, plans, and AI conversations without joining a company.
- Personal data remains scoped to the Personal Context.

#### FR-3: Invite-only Business Context

User can access a Business Context only after a registered company invites them for collaboration and they accept or belong to that context.

**Consequences:**
- Users without company membership do not see Business Context switching.
- Business data is separated from Personal data.
- Registration includes normal user registration and a visible option to register a company.
- Company registration can become a paid feature later.

#### FR-4: Context-aware navigation and assistant

The app scopes navigation, search, assistant responses, notes, reminders, projects, and AI conversations to the active Workspace Context.

**Consequences:**
- The UI clearly shows the active context.
- AI answers show which context they used.

### 4.2 Notebook Core

**Description:** Users need a polished notebook foundation before AI features can feel trustworthy. The MVP supports manual creation, editing, organization, filtering, and search of notes.

**Functional Requirements:**

#### FR-5: Note CRUD

User can create, edit, archive/delete, and view notes.

**Consequences:**
- Notes persist across sessions.
- Notes belong to a Workspace Context.

#### FR-6: Note organization

User can organize notes with tags/categories, favorites or pinned status, and relevant links to reminders, plans, calendar events, or projects.

**Consequences:**
- User can filter notes by context, tag/category, date, favorite/pinned status, and relevant linked entity.

#### FR-7: Manual search

User can search notes manually without AI.

**Consequences:**
- Search works for title and body content.
- Manual search remains available even if AI features are unavailable.

#### FR-8: Professional editor

User can write professional notes, planning notes, and personal journal-style notes in a polished editor.

**Consequences:**
- Editor supports a clean writing experience suitable for repeated daily use.
- Autosave or clear save-state feedback prevents accidental loss. [ASSUMPTION]
- User can choose the preferred writing mode, such as rich text or Markdown-style editing. [ASSUMPTION]
- The app shows examples/previews of how each writing mode looks before or while choosing.

### 4.3 AI Memory Search

**Description:** AI Memory Search is the MVP's core differentiator. It lets users retrieve past information even when they only remember small fragments.

**Functional Requirements:**

#### FR-9: Natural-language history search

User can ask natural-language questions across notes, reminders, projects, calendar context, and connected Gmail context.

**Consequences:**
- User can search with weak fragments such as "that meeting where we talked about the API problem."
- Results include relevant Source References.

#### FR-10: Source-aware AI answers

AI answers must show the sources used to produce the answer.

**Consequences:**
- Sources may include notes, projects, calendar events, Gmail threads, dates, or reminders.
- User can open referenced sources from the AI result.

#### FR-11: History summaries

User can ask AI to summarize a time period, topic, project, plan, or selected history.

**Consequences:**
- Summaries include important events, decisions, unresolved items, and suggested next actions where relevant.

### 4.4 AI Assistant And AI Actions

**Description:** The AI Assistant can operate across the app. It should feel like an active assistant, not only a chatbot. In MVP, supported AI changes show a preview before applying and remain visible and reversible after applying.

**Functional Requirements:**

#### FR-12: Conversational assistant

User can interact with the AI Assistant through natural language.

**Consequences:**
- Assistant can search, summarize, organize, create notes, suggest tags, suggest reminders, draft plans, and explain past history.
- Assistant asks clarifying questions when the request is too broad or ambiguous.

#### FR-13: AI-created and AI-updated data

AI Assistant can propose supported changes to app data, show a preview before applying, and then apply the changes when the user chooses to proceed.

**Consequences:**
- Supported actions include creating notes, creating reminders, creating plans, creating project summaries, applying tags/categories, saving AI-generated summaries as notes, linking notes to reminders/plans/projects, fixing grammar, and generating complete notes from messy user-provided information.
- Unsupported or risky actions include deleting/archiving notes, sending emails, inviting business users, changing calendar events, modifying company/business membership, or permanently erasing history unless the user performs an explicit manual action.
- AI edits show a preview before applying so the user can inspect the proposed change.

#### FR-14: AI Action History and revert

Every AI Action that changes data creates an AI Action History record.

**Consequences:**
- Record includes changed entities, previous state, current state, user-visible summary, timestamp, and revert capability.
- User can see what AI changed after the action.
- User can revert AI edits.
- MVP version/revert tracking applies only to AI edits, not all manual edits.
- AI Action History is retained forever unless related data is deleted by policy or explicit user action.

#### FR-15: MCP-ready assistant architecture

The assistant should be designed around tool/action invocation so future MCP-style integrations can be added.

**Consequences:**
- MVP does not include a public custom MCP marketplace.
- Architecture should avoid hard-coding assistant behavior as static chat only.
- MVP uses the OpenAI API as the LLM provider.

### 4.5 Google Calendar, Gmail, Reminders, And Planning

**Description:** The app integrates with Google services to make planning and history useful, while keeping permissions explicit and user-controlled.

**Functional Requirements:**

#### FR-16: Google Calendar connection

User can connect Google Calendar with explicit permission.

**Consequences:**
- In Personal Context MVP, Calendar integration is read-only.
- App can view upcoming calendar context after permission is granted.
- User can link notes, reminders, and plans to calendar events.
- AI can use calendar context in planning answers.
- Full create/update calendar support is deferred to Business or future phases.

#### FR-17: Gmail connection

User can connect Gmail with explicit permission.

**Consequences:**
- User can explicitly opt into Smart Gmail integration.
- When Smart Gmail is enabled, AI can perform full Gmail search within granted permissions for planning and history use cases.
- AI answers using Gmail content include Source References.
- Smart Gmail full search must not be enabled silently.

#### FR-18: Reminders

User can create reminders manually, and AI can create reminders as AI Actions.

**Consequences:**
- AI-created reminders are visible in AI Action History and can be reverted.
- Reminders can relate to notes, plans, calendar context, or projects.
- Reminders can sync with Google after the user grants permission.
- MVP syncs reminders to Google Calendar events. Google Tasks support is deferred.

#### FR-19: Planning plans

User can create a Plan for interviews, jobs, meetings, or projects.

**Consequences:**
- Plans can include notes, tasks, reminders, calendar context, and AI-generated next steps.
- AI can generate a plan from user input and available context.

### 4.6 Business-Lite Project History

**Description:** Business features are visible only for invited collaboration contexts and remain intentionally minimal in MVP.

**Functional Requirements:**

#### FR-20: Minimal Business projects

User in a Business Context can use basic Projects and project notes.

**Consequences:**
- Project history can be summarized by AI.
- Project notes remain scoped to the Business Context.

#### FR-21: Future collaboration readiness

The Business Context model should support future expansion into colleagues, feedback, progress tracking, and team calendar workflows.

**Consequences:**
- MVP does not need full team workspace functionality.
- MVP should not paint the architecture into a single-user-only corner.

### 4.7 Production Quality Layer

**Description:** The MVP must be production-ready and portfolio-grade, not a rough prototype.

**Functional Requirements:**

#### FR-22: Responsive polished UI

The app works well on desktop and usable narrower web viewports.

**Consequences:**
- Core workflows do not rely on broken or overlapping layouts.
- Writing, search, and assistant flows feel polished.

#### FR-23: Accessibility and UX quality

Core controls are accessible, clear, and consistent.

**Consequences:**
- Important controls have accessible labels.
- Loading, error, empty, and permission states are designed.

#### FR-24: Privacy and integration controls

User can understand and manage connected Google Calendar and Gmail permissions.

**Consequences:**
- Calendar and Gmail are optional.
- App communicates what connected data may be used for AI and planning.

## 5. Non-Goals For MVP

- Full team/company workspace.
- Colleague progress tracking.
- Shared company calendar visibility.
- Real-time collaboration.
- Native mobile app.
- Complex file/attachment system.
- Full backup/restore UI.
- Advanced admin panel.
- Enterprise roles and permissions beyond basic user/context ownership.
- Public/custom MCP marketplace.
- Full manual-edit version history.

## 6. MVP Scope

### 6.1 In Scope

- Google sign-in.
- Default Personal Context.
- Invite-only Business Context visibility.
- Workspace Context scoping.
- Core notes and manual search.
- AI Memory Search.
- AI Assistant with supported AI Actions.
- AI Action History and revert for AI edits.
- Google Calendar integration.
- Gmail integration.
- Reminders.
- Plans.
- Business-lite Projects and project notes.
- Production-quality responsive web UX.

### 6.2 Out Of Scope For MVP

- Complete organization management.
- Deep team permissions.
- Colleague tracking and feedback workflows.
- Shared calendar availability across a company.
- Advanced logging UI.
- Backup/restore UI.
- Native mobile apps.
- Public MCP/tool marketplace.

## 7. Success Metrics

**Primary**

- **SM-1:** Weak-fragment retrieval success - user can find a relevant historical note, project, event, or email from a vague query. Validates FR-9, FR-10.
- **SM-2:** Planning planning usefulness - user can create a useful plan with notes, reminders, and calendar context. Validates FR-16, FR-18, FR-19.
- **SM-3:** Project history usefulness - user can generate a project history summary with timeline, decisions, unresolved items, and next actions. Validates FR-11, FR-20.

**Secondary**

- **SM-4:** AI trust - user can identify the sources behind AI answers. Validates FR-10, FR-17.
- **SM-5:** AI action safety - user can review and revert AI-created changes. Validates FR-14.
- **SM-6:** Portfolio quality - app demonstrates polished UX, production-ready architecture, and credible AI workflows. Validates FR-22, FR-23, FR-24.

**Counter-metrics**

- **SM-C1:** Do not optimize for AI autonomy at the cost of user trust. AI should act confidently, but changes must remain visible and reversible.
- **SM-C2:** Do not optimize for business/team breadth at the cost of the personal professional MVP.

## 8. Open Questions

No open MVP-blocking product questions remain. Future architecture may surface implementation-specific tradeoffs.

## 9. Assumptions Index

- FR-8: Autosave or clear save-state feedback prevents accidental loss.
- FR-8: User can choose the preferred writing mode, such as rich text or Markdown-style editing.
