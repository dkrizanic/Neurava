CREATE TABLE IF NOT EXISTS reminder (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  title varchar(180) NOT NULL,
  details text NOT NULL,
  due_at timestamp(6) with time zone NOT NULL,
  related_context varchar(512) NOT NULL,
  calendar_sync_enabled boolean NOT NULL,
  calendar_sync_state varchar(40) NOT NULL,
  calendar_event_id varchar(160),
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  completed_at timestamp(6) with time zone,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_reminder_workspace_due
  ON reminder (workspace_context_id, due_at ASC);

