-- Migration Script to add user_id column to financial_entities table and create index
ALTER TABLE financial_entities ADD COLUMN IF NOT EXISTS user_id VARCHAR(255);
CREATE INDEX IF NOT EXISTS idx_financial_entities_user_id ON financial_entities(user_id);
