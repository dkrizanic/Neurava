CREATE TABLE IF NOT EXISTS project_record (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  name varchar(180) NOT NULL,
  description text NOT NULL,
  status varchar(40) NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_project_record_workspace_updated
  ON project_record (workspace_context_id, updated_at DESC);

