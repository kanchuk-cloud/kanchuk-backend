-- V44: Add expected_return_by to returns for customer-facing SLA display
ALTER TABLE returns
    ADD COLUMN IF NOT EXISTS expected_return_by TIMESTAMPTZ;
