-- V21: Add rejection_notes to goods_receipts
ALTER TABLE goods_receipts
    ADD COLUMN IF NOT EXISTS rejection_notes TEXT;
