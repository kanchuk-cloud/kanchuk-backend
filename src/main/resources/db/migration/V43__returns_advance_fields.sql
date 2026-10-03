-- V35: Add reverse-pickup and inspection fields to returns for the advance workflow
ALTER TABLE returns
    ADD COLUMN IF NOT EXISTS awb_number          VARCHAR(100),
    ADD COLUMN IF NOT EXISTS carrier             VARCHAR(100),
    ADD COLUMN IF NOT EXISTS inspection_notes    TEXT,
    ADD COLUMN IF NOT EXISTS rejection_reason    TEXT,
    ADD COLUMN IF NOT EXISTS pickup_scheduled_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS received_at         TIMESTAMPTZ;
