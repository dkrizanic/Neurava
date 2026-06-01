CREATE TABLE IF NOT EXISTS ai_action_history (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  action varchar(80) NOT NULL,
  entity_type varchar(80) NOT NULL,
  entity_id uuid NOT NULL,
  change_type varchar(40) NOT NULL,
  summary varchar(512) NOT NULL,
  previous_state text,
  current_state text NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS ix_ai_action_history_workspace_created
  ON ai_action_history (workspace_context_id, created_at DESC);

CREATE INDEX IF NOT EXISTS ix_ai_action_history_entity
  ON ai_action_history (workspace_context_id, entity_type, entity_id);
