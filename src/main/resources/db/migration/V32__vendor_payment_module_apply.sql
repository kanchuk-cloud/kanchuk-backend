-- ============================================================
-- V32: Apply Vendor Onboarding & Payment Module columns/tables
-- (V29 was applied earlier with old content; this adds what was missing)
-- ============================================================

-- ────────────────────────────────────────────────────────────
-- 1. Extend sellers table (all IF NOT EXISTS — safe to re-run)
-- ────────────────────────────────────────────────────────────
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS status                    VARCHAR(20)  NOT NULL DEFAULT 'pending';
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS legal_name                VARCHAR(200);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS brand_name                VARCHAR(200);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS business_type             VARCHAR(30);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS pan_number                VARCHAR(10);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS is_msme                   BOOLEAN      NOT NULL DEFAULT false;
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS udyam_number              VARCHAR(20);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS mobile                    VARCHAR(10);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS address_line              VARCHAR(255);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS city                      VARCHAR(100);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS state                     VARCHAR(100);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS pincode                   VARCHAR(6);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS bank_account_number       VARCHAR(50);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS bank_ifsc                 VARCHAR(11);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS bank_account_holder_name  VARCHAR(200);
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS commission_percent        DECIMAL(5,2) NOT NULL DEFAULT 0;
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS payment_cycle_days        INT          NOT NULL DEFAULT 30;
ALTER TABLE sellers ADD COLUMN IF NOT EXISTS kyc_status                VARCHAR(20)  NOT NULL DEFAULT 'not_submitted';

-- ────────────────────────────────────────────────────────────
-- 2. vendor_documents
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vendor_documents (
    id                  UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id           UUID        NOT NULL REFERENCES sellers(id),
    document_type       VARCHAR(30) NOT NULL,
    file_url            TEXT        NOT NULL,
    uploaded_by         VARCHAR(10) NOT NULL DEFAULT 'VENDOR',
    verification_status VARCHAR(20) NOT NULL DEFAULT 'pending',
    verified_by         UUID        REFERENCES admin_users(id),
    verified_at         TIMESTAMPTZ,
    rejection_reason    TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_vendor_docs_vendor ON vendor_documents(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_docs_status ON vendor_documents(verification_status);

-- ────────────────────────────────────────────────────────────
-- 3. vendor_payments
-- ────────────────────────────────────────────────────────────
CREATE SEQUENCE IF NOT EXISTS vendor_payment_seq START 1001;

CREATE TABLE IF NOT EXISTS vendor_payments (
    id                  VARCHAR(20)  PRIMARY KEY,
    vendor_id           UUID         NOT NULL REFERENCES sellers(id),
    order_id            VARCHAR(50),
    invoice_number      VARCHAR(100) NOT NULL,
    invoice_date        DATE         NOT NULL,
    payment_date        DATE,
    payment_method      VARCHAR(10),
    gross_amount        DECIMAL(14,2) NOT NULL,
    commission_amount   DECIMAL(14,2) NOT NULL DEFAULT 0,
    tds_percent         DECIMAL(5,2)  NOT NULL DEFAULT 0,
    tds_amount          DECIMAL(14,2) NOT NULL DEFAULT 0,
    net_payable         DECIMAL(14,2) NOT NULL,
    utr_number          VARCHAR(50),
    status              VARCHAR(15)  NOT NULL DEFAULT 'pending',
    failure_reason      TEXT,
    approved_by         UUID         REFERENCES admin_users(id),
    approved_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    deleted_at          TIMESTAMPTZ
);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_vendor  ON vendor_payments(vendor_id);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_status  ON vendor_payments(status);
CREATE INDEX IF NOT EXISTS idx_vendor_payments_invoice ON vendor_payments(invoice_number);

-- ────────────────────────────────────────────────────────────
-- 4. vendor_status_history
-- ────────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS vendor_status_history (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id   UUID        NOT NULL REFERENCES sellers(id),
    field_name  VARCHAR(30) NOT NULL DEFAULT 'status',
    old_value   VARCHAR(30),
    new_value   VARCHAR(30) NOT NULL,
    changed_by  UUID        REFERENCES admin_users(id),
    reason      TEXT,
    changed_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_vsh_vendor ON vendor_status_history(vendor_id);
