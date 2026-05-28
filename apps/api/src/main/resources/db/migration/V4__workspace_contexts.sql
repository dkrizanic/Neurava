CREATE TABLE IF NOT EXISTS workspace_context (
  id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  type varchar(40) NOT NULL,
  name varchar(255) NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_workspace_context_owner_account
    FOREIGN KEY (owner_account_id) REFERENCES auth_account (id),
  CONSTRAINT ck_workspace_context_type
    CHECK (type IN ('PERSONAL', 'BUSINESS')),
  CONSTRAINT uk_workspace_context_owner_type
    UNIQUE (owner_account_id, type)
);

CREATE TABLE IF NOT EXISTS workspace_membership (
  id uuid NOT NULL,
  account_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  role varchar(40) NOT NULL,
  created_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_workspace_membership_account
    FOREIGN KEY (account_id) REFERENCES auth_account (id),
  CONSTRAINT fk_workspace_membership_workspace
    FOREIGN KEY (workspace_context_id) REFERENCES workspace_context (id),
  CONSTRAINT ck_workspace_membership_role
    CHECK (role IN ('OWNER', 'MEMBER')),
  CONSTRAINT uk_workspace_membership_account_workspace
    UNIQUE (account_id, workspace_context_id)
);
