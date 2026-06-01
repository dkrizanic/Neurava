CREATE TABLE IF NOT EXISTS integration_connection (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  provider varchar(40) NOT NULL,
  enabled boolean NOT NULL,
  permission_summary varchar(512) NOT NULL,
  connected_at timestamp(6) with time zone,
  disconnected_at timestamp(6) with time zone,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_integration_connection_workspace_provider
  ON integration_connection (workspace_context_id, provider);

