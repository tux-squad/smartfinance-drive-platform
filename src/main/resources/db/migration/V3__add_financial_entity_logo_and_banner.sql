-- Migration Script to add logo_url and banner_url columns to financial_entities table
ALTER TABLE financial_entities ADD COLUMN IF NOT EXISTS logo_url VARCHAR(1000);
ALTER TABLE financial_entities ADD COLUMN IF NOT EXISTS banner_url VARCHAR(1000);
