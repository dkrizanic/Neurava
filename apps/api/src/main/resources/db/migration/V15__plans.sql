CREATE TABLE IF NOT EXISTS plan_record (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  title varchar(180) NOT NULL,
  goal text NOT NULL,
  items text NOT NULL,
  linked_resources varchar(1024) NOT NULL,
  status varchar(40) NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_plan_record_workspace_updated
  ON plan_record (workspace_context_id, updated_at DESC);

