ALTER TABLE note
  ADD COLUMN IF NOT EXISTS archived_at timestamp(6) with time zone,
  ADD COLUMN IF NOT EXISTS tags varchar(512) NOT NULL DEFAULT '',
  ADD COLUMN IF NOT EXISTS favorite boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS pinned boolean NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS editor_mode varchar(40) NOT NULL DEFAULT 'RICH_TEXT',
  ADD COLUMN IF NOT EXISTS linked_resources varchar(1024) NOT NULL DEFAULT '';

CREATE INDEX IF NOT EXISTS ix_note_workspace_archived_updated
  ON note (workspace_context_id, archived_at, updated_at DESC);
