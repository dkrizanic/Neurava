CREATE TABLE IF NOT EXISTS note (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  title varchar(180) NOT NULL,
  body text NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_note_workspace_updated
  ON note (workspace_context_id, updated_at DESC);

CREATE INDEX IF NOT EXISTS ix_note_owner_workspace
  ON note (owner_account_id, workspace_context_id);
