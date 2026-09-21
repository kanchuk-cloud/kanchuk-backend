-- V22: Inventory sub-module (stock movements, adjustments, transfers)

-- ── 1. Enhance inventory_levels ──────────────────────────────────────────────
ALTER TABLE inventory_levels
    ADD COLUMN IF NOT EXISTS low_stock_threshold INT          NOT NULL DEFAULT 5,
    ADD COLUMN IF NOT EXISTS bin_location        VARCHAR(50),
    ADD COLUMN IF NOT EXISTS last_counted_at     TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS last_counted_by     UUID REFERENCES admin_users(id);

-- ── 2. stock_movements — full audit trail ────────────────────────────────────
CREATE TABLE IF NOT EXISTS stock_movements (
    id             UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id     UUID        NOT NULL REFERENCES product_listings(id) ON DELETE CASCADE,
    location_id    UUID        NOT NULL REFERENCES stock_locations(id),
    movement_type  VARCHAR(30) NOT NULL,
    -- grn_receipt | order_deduction | order_return | manual_adjustment
    -- transfer_in | transfer_out | damage_writeoff | count_correction
    qty_before     INT         NOT NULL,
    qty_change     INT         NOT NULL,
    qty_after      INT         NOT NULL,
    reference_type VARCHAR(30),   -- 'grn' | 'order' | 'adjustment' | 'transfer'
    reference_id   UUID,
    notes          TEXT,
    created_by     UUID REFERENCES admin_users(id),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_stock_mov_listing   ON stock_movements(listing_id);
CREATE INDEX IF NOT EXISTS idx_stock_mov_location  ON stock_movements(location_id);
CREATE INDEX IF NOT EXISTS idx_stock_mov_type      ON stock_movements(movement_type);
CREATE INDEX IF NOT EXISTS idx_stock_mov_created   ON stock_movements(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_stock_mov_reference ON stock_movements(reference_type, reference_id);

-- ── 3. stock_adjustments — manual corrections ────────────────────────────────
CREATE TABLE IF NOT EXISTS stock_adjustments (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id  UUID        NOT NULL REFERENCES product_listings(id) ON DELETE CASCADE,
    location_id UUID        NOT NULL REFERENCES stock_locations(id),
    reason_code VARCHAR(30) NOT NULL,
    -- damaged | expired | theft | count_correction | found | other
    qty_before  INT         NOT NULL,
    qty_change  INT         NOT NULL,
    qty_after   INT         NOT NULL,
    notes       TEXT,
    adjusted_by UUID REFERENCES admin_users(id),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_stock_adj_listing  ON stock_adjustments(listing_id);
CREATE INDEX IF NOT EXISTS idx_stock_adj_location ON stock_adjustments(location_id);
CREATE INDEX IF NOT EXISTS idx_stock_adj_reason   ON stock_adjustments(reason_code);

-- ── 4. stock_transfers — move stock between locations ────────────────────────
CREATE TABLE IF NOT EXISTS stock_transfers (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_number  VARCHAR(50) NOT NULL UNIQUE,
    from_location_id UUID        NOT NULL REFERENCES stock_locations(id),
    to_location_id   UUID        NOT NULL REFERENCES stock_locations(id),
    status           VARCHAR(20) NOT NULL DEFAULT 'draft',
    -- draft | in_transit | completed | cancelled
    notes            TEXT,
    created_by       UUID REFERENCES admin_users(id),
    completed_by     UUID REFERENCES admin_users(id),
    completed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS stock_transfer_items (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transfer_id      UUID NOT NULL REFERENCES stock_transfers(id) ON DELETE CASCADE,
    listing_id       UUID NOT NULL REFERENCES product_listings(id),
    qty_requested    INT  NOT NULL CHECK (qty_requested > 0),
    qty_transferred  INT  NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_transfer_items_transfer ON stock_transfer_items(transfer_id);
CREATE INDEX IF NOT EXISTS idx_transfer_items_listing  ON stock_transfer_items(listing_id);
