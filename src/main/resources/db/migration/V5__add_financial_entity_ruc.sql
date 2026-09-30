ALTER TABLE financial_entities ADD COLUMN IF NOT EXISTS ruc VARCHAR(11);
CREATE UNIQUE INDEX IF NOT EXISTS idx_financial_entities_ruc ON financial_entities(ruc) WHERE ruc IS NOT NULL;
