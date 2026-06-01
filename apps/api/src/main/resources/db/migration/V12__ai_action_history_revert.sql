ALTER TABLE ai_action_history
  ADD COLUMN IF NOT EXISTS reverted_at timestamp(6) with time zone,
  ADD COLUMN IF NOT EXISTS revert_summary varchar(512);
