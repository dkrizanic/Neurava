CREATE TABLE IF NOT EXISTS note_retrieval_index (
  note_id uuid NOT NULL,
  workspace_context_id uuid NOT NULL,
  owner_account_id uuid NOT NULL,
  title varchar(180) NOT NULL,
  searchable_text text NOT NULL,
  embedding vector(16) NOT NULL,
  indexed_at timestamp(6) with time zone NOT NULL,
  note_updated_at timestamp(6) with time zone NOT NULL,
  status varchar(40) NOT NULL,
  failure_message varchar(320),
  PRIMARY KEY (note_id)
);

CREATE INDEX IF NOT EXISTS ix_note_retrieval_workspace
  ON note_retrieval_index (workspace_context_id, indexed_at DESC);

CREATE INDEX IF NOT EXISTS ix_note_retrieval_embedding
  ON note_retrieval_index USING hnsw (embedding vector_cosine_ops);
