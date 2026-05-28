CREATE TABLE IF NOT EXISTS auth_account (
  id uuid NOT NULL,
  provider varchar(40) NOT NULL,
  provider_subject varchar(255) NOT NULL,
  email varchar(320),
  display_name varchar(255),
  avatar_url varchar(1024),
  created_at timestamp(6) with time zone NOT NULL,
  updated_at timestamp(6) with time zone NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT uk_auth_account_provider_subject UNIQUE (provider, provider_subject)
);
