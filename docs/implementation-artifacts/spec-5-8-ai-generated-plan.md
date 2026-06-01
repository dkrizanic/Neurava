# Story 5.8 - AI-Generated Plan

Status: Done

## Goal

Let AI generate a practical plan preview from a user goal and apply it after approval.

## Implementation

- Added `create_plan` preview and application actions.
- Generated plans include title, goal, plan items, and extracted links.
- Applying an AI plan creates a plan and AI Action History record.
- Revert can remove AI-created plans through the shared create-entity revert path.
- Plans page includes an AI plan preview/apply panel.
- Assistant chat recognizes create-plan prompts.

## Verification

- `apps/web`: `npm run build`
- `apps/web`: `npm test -- --run`
- `apps/api`: Modulith architecture test

