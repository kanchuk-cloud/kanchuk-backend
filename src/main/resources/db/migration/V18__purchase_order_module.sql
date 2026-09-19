-- ============================================================
-- Kanchuk — Purchase Order Module Schema (V6)
-- Full lifecycle: Draft → Approval → Vendor → GRN → Closed
-- Covers: PO line items, GRN, payments, approvals,
--         audit log, comments, attachments, templates.
-- NO existing columns deleted. Safe to apply on top of V1–V5.
-- ============================================================


-- ────────────────────────────────────────────────────────────
-- 1. PO NUMBER & GRN NUMBER AUTO-SEQUENCES
-- ────────────────────────────────────────────────────────────

CREATE SEQUENCE IF NOT EXISTS po_number_seq  START 1;
CREATE SEQUENCE IF NOT EXISTS grn_number_seq START 1;

CREATE OR REPLACE FUNCTION generate_po_number() RETURNS TEXT AS $$
BEGIN
  RETURN 'PO-' || TO_CHAR(now(), 'YYYY') || '-' || LPAD(nextval('po_number_seq')::TEXT, 5, '0');
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION generate_grn_number() RETURNS TEXT AS $$
BEGIN
  RETURN 'GRN-' || TO_CHAR(now(), 'YYYY') || '-' || LPAD(nextval('grn_number_seq')::TEXT, 5, '0');
END;
$$ LANGUAGE plpgsql;


-- ────────────────────────────────────────────────────────────
-- 2. PO TEMPLATES  (recurring / scheduled restocking)
--    Created before purchase_orders so FK can reference it.
-- ────────────────────────────────────────────────────────────

CREATE TABLE po_templates (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(200) NOT NULL,
  seller_id        UUID         REFERENCES sellers(id),
  recurrence_type  VARCHAR(20)
                     CHECK (recurrence_type IN ('weekly','biweekly','monthly','quarterly','manual')),
  payment_terms    VARCHAR(100),    -- e.g. "Net 30", "50% advance"
  notes            TEXT,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  created_by       UUID         REFERENCES admin_users(id),
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE po_template_items (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  template_id      UUID          NOT NULL REFERENCES po_templates(id) ON DELETE CASCADE,
  variant_id       UUID          REFERENCES product_variants(id),
  sku              VARCHAR(150),
  product_name     VARCHAR(255),
  default_quantity INT           NOT NULL DEFAULT 1,
  unit_cost        DECIMAL(10,2),
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_template_items_tmpl ON po_template_items(template_id);


-- ────────────────────────────────────────────────────────────
-- 3. ENHANCE purchase_orders
--    Add all missing columns. No existing columns removed.
--
--    New status lifecycle (no CHECK constraint so no conflict):
--    draft → pending_approval → approved → sent_to_vendor →
--    partially_received → received → closed | cancelled
-- ────────────────────────────────────────────────────────────

ALTER TABLE purchase_orders
  -- Financial breakdown
  ADD COLUMN IF NOT EXISTS subtotal          DECIMAL(10,2),
  ADD COLUMN IF NOT EXISTS tax_amount        DECIMAL(10,2)  NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS shipping_charges  DECIMAL(10,2)  NOT NULL DEFAULT 0,
  ADD COLUMN IF NOT EXISTS discount_amount   DECIMAL(10,2)  NOT NULL DEFAULT 0,

  -- Multi-currency support
  ADD COLUMN IF NOT EXISTS currency          VARCHAR(3)     NOT NULL DEFAULT 'INR',
  ADD COLUMN IF NOT EXISTS exchange_rate     DECIMAL(10,4)  NOT NULL DEFAULT 1.0000,

  -- Vendor / shipping
  ADD COLUMN IF NOT EXISTS payment_terms     VARCHAR(100),
  ADD COLUMN IF NOT EXISTS shipping_address  JSONB,
  ADD COLUMN IF NOT EXISTS vendor_invoice_number VARCHAR(100),
  ADD COLUMN IF NOT EXISTS vendor_invoice_date   DATE,

  -- Approval workflow
  ADD COLUMN IF NOT EXISTS approval_required  BOOLEAN       NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS approved_at        TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS approved_by        UUID          REFERENCES admin_users(id),

  -- Status timestamps
  ADD COLUMN IF NOT EXISTS sent_at           TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS confirmed_at      TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS cancelled_at      TIMESTAMPTZ,
  ADD COLUMN IF NOT EXISTS cancellation_reason TEXT,

  -- Tracking
  ADD COLUMN IF NOT EXISTS created_by        UUID          REFERENCES admin_users(id),
  ADD COLUMN IF NOT EXISTS is_recurring      BOOLEAN       NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS template_id       UUID          REFERENCES po_templates(id);

-- Performance indexes on purchase_orders
CREATE INDEX IF NOT EXISTS idx_purchase_orders_seller_id  ON purchase_orders(seller_id);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_status     ON purchase_orders(status);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_created_at ON purchase_orders(created_at);
CREATE INDEX IF NOT EXISTS idx_purchase_orders_created_by ON purchase_orders(created_by);


-- ────────────────────────────────────────────────────────────
-- 4. PURCHASE ORDER LINE ITEMS
-- ────────────────────────────────────────────────────────────

CREATE TABLE purchase_order_items (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id             UUID          NOT NULL REFERENCES purchase_orders(id) ON DELETE CASCADE,

  -- Product references (snapshot-friendly)
  product_id        UUID          REFERENCES products(id),
  variant_id        UUID          REFERENCES product_variants(id),
  listing_id        UUID          REFERENCES product_listings(id),
  sku               VARCHAR(150),           -- snapshot at PO creation time
  product_name      VARCHAR(255),           -- snapshot at PO creation time
  variant_label     VARCHAR(255),           -- e.g. "Size: M | Color: Red"

  -- Quantities
  quantity_ordered  INT           NOT NULL CHECK (quantity_ordered > 0),
  quantity_received INT           NOT NULL DEFAULT 0,

  -- Pricing
  unit_cost         DECIMAL(10,2) NOT NULL,
  discount_percent  DECIMAL(5,2)  NOT NULL DEFAULT 0,
  discount_amount   DECIMAL(10,2) NOT NULL DEFAULT 0,
  tax_rate          DECIMAL(5,2)  NOT NULL DEFAULT 0,
  tax_amount        DECIMAL(10,2) NOT NULL DEFAULT 0,
  total_cost        DECIMAL(10,2) NOT NULL, -- (unit_cost * qty - discount) + tax

  -- Batch / expiry (for FMCG/pharma compatibility)
  batch_number      VARCHAR(100),
  expiry_date       DATE,

  notes             TEXT,
  created_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_items_po_id      ON purchase_order_items(po_id);
CREATE INDEX idx_po_items_variant_id ON purchase_order_items(variant_id);
CREATE INDEX idx_po_items_product_id ON purchase_order_items(product_id);


-- ────────────────────────────────────────────────────────────
-- 5. GOODS RECEIPT NOTES (GRN)
--    Supports multiple partial receipts against one PO.
-- ────────────────────────────────────────────────────────────

CREATE TABLE goods_receipts (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id          UUID        NOT NULL REFERENCES purchase_orders(id),
  grn_number     VARCHAR(50) NOT NULL UNIQUE,  -- generate_grn_number()
  status         VARCHAR(20) NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending','inspecting','accepted','partially_accepted','rejected')),

  -- Logistics
  location_id    UUID        REFERENCES stock_locations(id),  -- receiving warehouse
  carrier_name   VARCHAR(100),
  vehicle_number VARCHAR(50),
  invoice_number VARCHAR(100),                -- vendor's delivery invoice
  invoice_date   DATE,

  -- Personnel
  received_by    UUID        REFERENCES admin_users(id),
  inspected_by   UUID        REFERENCES admin_users(id),
  inspected_at   TIMESTAMPTZ,

  notes          TEXT,
  received_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_grn_po_id  ON goods_receipts(po_id);
CREATE INDEX idx_grn_status ON goods_receipts(status);


CREATE TABLE goods_receipt_items (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  grn_id              UUID NOT NULL REFERENCES goods_receipts(id) ON DELETE CASCADE,
  po_item_id          UUID NOT NULL REFERENCES purchase_order_items(id),

  -- Quantities
  quantity_received   INT  NOT NULL CHECK (quantity_received >= 0),
  quantity_accepted   INT  NOT NULL DEFAULT 0,
  quantity_rejected   INT  NOT NULL DEFAULT 0,
  quantity_damaged    INT  NOT NULL DEFAULT 0,

  rejection_reason    TEXT,

  -- Batch tracking
  batch_number        VARCHAR(100),
  expiry_date         DATE,

  notes               TEXT,
  created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_grn_items_grn_id     ON goods_receipt_items(grn_id);
CREATE INDEX idx_grn_items_po_item_id ON goods_receipt_items(po_item_id);


-- ────────────────────────────────────────────────────────────
-- 6. PO PAYMENTS
--    Track all payment installments against a PO.
-- ────────────────────────────────────────────────────────────

CREATE TABLE purchase_order_payments (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id            UUID          NOT NULL REFERENCES purchase_orders(id),
  amount           DECIMAL(10,2) NOT NULL CHECK (amount > 0),
  payment_date     DATE          NOT NULL,
  payment_mode     VARCHAR(50)   NOT NULL
                     CHECK (payment_mode IN ('neft','rtgs','upi','cheque','cash','wire','other')),
  reference_number VARCHAR(100),    -- UTR / cheque number / wire ref
  bank_name        VARCHAR(100),
  notes            TEXT,
  recorded_by      UUID          REFERENCES admin_users(id),
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_payments_po_id ON purchase_order_payments(po_id);


-- ────────────────────────────────────────────────────────────
-- 7. PO APPROVALS  (multi-level workflow)
-- ────────────────────────────────────────────────────────────

CREATE TABLE po_approvals (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id          UUID        NOT NULL REFERENCES purchase_orders(id),
  approval_level INT         NOT NULL DEFAULT 1,   -- 1 = L1, 2 = L2, etc.
  approver_id    UUID        REFERENCES admin_users(id),
  status         VARCHAR(20) NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending','approved','rejected','skipped')),
  comments       TEXT,
  decided_at     TIMESTAMPTZ,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (po_id, approval_level)
);

CREATE INDEX idx_po_approvals_po_id      ON po_approvals(po_id);
CREATE INDEX idx_po_approvals_approver   ON po_approvals(approver_id);
CREATE INDEX idx_po_approvals_status     ON po_approvals(status);


-- ────────────────────────────────────────────────────────────
-- 8. PO AUDIT LOG  (immutable — no updates/deletes allowed)
-- ────────────────────────────────────────────────────────────

CREATE TABLE po_audit_log (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id         UUID         NOT NULL REFERENCES purchase_orders(id),
  action        VARCHAR(50)  NOT NULL,   -- created, status_changed, approved,
                                         -- item_added, grn_created, payment_recorded, etc.
  entity_type   VARCHAR(50),             -- purchase_order, po_item, grn, payment, etc.
  entity_id     UUID,                    -- ID of the affected sub-record
  field_changed VARCHAR(100),
  old_value     TEXT,
  new_value     TEXT,
  changed_by    UUID         REFERENCES admin_users(id),
  changed_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_audit_po_id     ON po_audit_log(po_id);
CREATE INDEX idx_po_audit_changed_at ON po_audit_log(changed_at);


-- ────────────────────────────────────────────────────────────
-- 9. PO COMMENTS  (internal team thread per PO)
-- ────────────────────────────────────────────────────────────

CREATE TABLE po_comments (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id         UUID        NOT NULL REFERENCES purchase_orders(id),
  comment       TEXT        NOT NULL,
  is_internal   BOOLEAN     NOT NULL DEFAULT true,  -- false = visible to vendor
  commented_by  UUID        REFERENCES admin_users(id),
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_comments_po_id ON po_comments(po_id);


-- ────────────────────────────────────────────────────────────
-- 10. PO ATTACHMENTS  (quotations, contracts, invoices, docs)
-- ────────────────────────────────────────────────────────────

CREATE TABLE po_attachments (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_id            UUID         NOT NULL REFERENCES purchase_orders(id),
  grn_id           UUID         REFERENCES goods_receipts(id),   -- optional: attach to GRN
  attachment_type  VARCHAR(30)  NOT NULL
                     CHECK (attachment_type IN
                       ('quotation','contract','vendor_invoice','grn_document',
                        'quality_report','payment_receipt','other')),
  file_name        VARCHAR(255) NOT NULL,
  file_url         TEXT         NOT NULL,
  file_size_bytes  BIGINT,
  content_type     VARCHAR(100),
  uploaded_by      UUID         REFERENCES admin_users(id),
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_po_attachments_po_id  ON po_attachments(po_id);
CREATE INDEX idx_po_attachments_grn_id ON po_attachments(grn_id);


-- ────────────────────────────────────────────────────────────
-- 11. SELLER ENHANCEMENTS
--     Add vendor performance / preferred vendor fields.
--     No existing columns removed.
-- ────────────────────────────────────────────────────────────

ALTER TABLE sellers
  ADD COLUMN IF NOT EXISTS contact_person   VARCHAR(100),
  ADD COLUMN IF NOT EXISTS bank_name        VARCHAR(100),
  ADD COLUMN IF NOT EXISTS bank_account     VARCHAR(50),
  ADD COLUMN IF NOT EXISTS bank_ifsc        VARCHAR(11),
  ADD COLUMN IF NOT EXISTS payment_terms    VARCHAR(100),  -- default terms for this vendor
  ADD COLUMN IF NOT EXISTS lead_time_days   INT,           -- typical delivery lead time
  ADD COLUMN IF NOT EXISTS is_preferred     BOOLEAN        NOT NULL DEFAULT false,
  ADD COLUMN IF NOT EXISTS notes            TEXT;


-- ────────────────────────────────────────────────────────────
-- 12. PREFERRED VENDOR PER PRODUCT VARIANT
--     Links a variant to its preferred vendor with reference price.
-- ────────────────────────────────────────────────────────────

CREATE TABLE variant_preferred_vendors (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  variant_id        UUID          NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
  seller_id         UUID          NOT NULL REFERENCES sellers(id) ON DELETE CASCADE,
  last_purchase_price DECIMAL(10,2),
  is_primary        BOOLEAN       NOT NULL DEFAULT false,  -- primary preferred vendor
  notes             TEXT,
  updated_at        TIMESTAMPTZ   NOT NULL DEFAULT now(),
  UNIQUE (variant_id, seller_id)
);

CREATE INDEX idx_vpv_variant_id ON variant_preferred_vendors(variant_id);
CREATE INDEX idx_vpv_seller_id  ON variant_preferred_vendors(seller_id);


-- ────────────────────────────────────────────────────────────
-- 13. REORDER RULES  (triggers low-stock PO suggestions)
-- ────────────────────────────────────────────────────────────

CREATE TABLE reorder_rules (
  id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  variant_id           UUID    NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
  location_id          UUID    REFERENCES stock_locations(id),
  reorder_point        INT     NOT NULL DEFAULT 0,   -- trigger when qty_on_hand <= this
  reorder_quantity     INT     NOT NULL DEFAULT 1,   -- suggested PO quantity
  preferred_seller_id  UUID    REFERENCES sellers(id),
  is_active            BOOLEAN NOT NULL DEFAULT true,
  created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (variant_id, location_id)
);

CREATE INDEX idx_reorder_rules_variant  ON reorder_rules(variant_id);
CREATE INDEX idx_reorder_rules_location ON reorder_rules(location_id);
