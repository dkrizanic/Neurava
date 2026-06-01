# Story 4.6 - Generate Note From Messy Input

Status: Done

## Goal

Let a note writer provide messy text to the assistant and receive a structured note preview that can be applied, reviewed in AI Action History, and reverted.

## Scope

This story completes the generated-note flow already introduced by `create_note` previews by adding suggested linked resources and ensuring the approved preview persists all generated note fields.

## Backend

- `create_note` preview returns title, cleaned body, suggested tags, and suggested linked resources.
- Suggested linked resources are extracted from HTTP/HTTPS URLs in the messy input.
- `create_note` apply accepts `linkedResources` and persists them with the saved note.
- Applying still records AI Action History with the full note state.
- Revert uses the existing create-note revert path and removes the AI-created note.

## Frontend

- Assistant note previews now display suggested links alongside title, body, and tags.
- Applying a generated note sends linked resources with the approved preview.
- Existing AI Action History and revert controls remain available after apply.

## Verification

- `apps/api`: `.\mvnw.cmd -q -Dtest=NoteRetrievalIndexFunctionalTest test`
- repo root: `npm --prefix apps/web test -- AssistantPage assistantApi`
