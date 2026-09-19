-- ============================================================
-- V20: Vendor Portal
-- Creates vendor_users table and extends goods_receipts
-- ============================================================

CREATE TABLE vendor_users (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id     UUID         NOT NULL REFERENCES sellers(id),
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'VENDOR',
    is_active     BOOLEAN      NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at    TIMESTAMPTZ
);

CREATE UNIQUE INDEX idx_vendor_users_email   ON vendor_users(email) WHERE deleted_at IS NULL;
CREATE INDEX         idx_vendor_users_seller  ON vendor_users(seller_id);

-- Track which vendor user submitted the GRN + idempotency guard for inventory
ALTER TABLE goods_receipts
    ADD COLUMN IF NOT EXISTS submitted_by_vendor_user_id UUID REFERENCES vendor_users(id),
    ADD COLUMN IF NOT EXISTS inventory_updated           BOOLEAN NOT NULL DEFAULT false;
