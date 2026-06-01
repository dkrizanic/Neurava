ALTER TABLE note
  ADD COLUMN IF NOT EXISTS note_date date;

UPDATE note
  SET note_date = CAST(created_at AS date)
  WHERE note_date IS NULL;

ALTER TABLE note
  ALTER COLUMN note_date SET NOT NULL;

CREATE INDEX IF NOT EXISTS ix_note_workspace_note_date_updated
  ON note (workspace_context_id, note_date, updated_at DESC);
