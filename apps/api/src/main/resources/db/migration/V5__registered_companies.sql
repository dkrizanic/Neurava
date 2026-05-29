CREATE TABLE IF NOT EXISTS registered_company (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  name varchar(160) NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_registered_company_owner_account
    FOREIGN KEY (owner_account_id) REFERENCES auth_account (id),
  CONSTRAINT fk_registered_company_workspace
    FOREIGN KEY (workspace_context_id) REFERENCES workspace_context (id),
  CONSTRAINT uk_registered_company_owner
    UNIQUE (owner_account_id),
  CONSTRAINT uk_registered_company_workspace
    UNIQUE (workspace_context_id)
);
