-- ── Delivery zones ────────────────────────────────────────────────────────────
CREATE TABLE delivery_zones (
  id                       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
  code                     VARCHAR(50)   NOT NULL UNIQUE,
  name                     VARCHAR(100)  NOT NULL,
  standard_delivery_charge NUMERIC(10,2) NOT NULL DEFAULT 49.00,
  express_delivery_charge  NUMERIC(10,2) NOT NULL DEFAULT 99.00,
  standard_delivery_days   INT           NOT NULL DEFAULT 5,
  express_delivery_minutes INT           NOT NULL DEFAULT 120,
  express_available        BOOLEAN       NOT NULL DEFAULT false,
  cod_available            BOOLEAN       NOT NULL DEFAULT true,
  is_active                BOOLEAN       NOT NULL DEFAULT true,
  created_at               TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at               TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ── Extend pincodes with zone FK and timestamps ───────────────────────────────
ALTER TABLE pincodes
  ADD COLUMN zone_id    UUID REFERENCES delivery_zones(id) ON DELETE SET NULL,
  ADD COLUMN created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  ADD COLUMN updated_at TIMESTAMPTZ NOT NULL DEFAULT now();

CREATE INDEX idx_pincodes_zone_id ON pincodes(zone_id);

-- ── Extend orders with delivery snapshot fields ───────────────────────────────
-- These snapshot the delivery info at order-creation time so historical orders
-- are unaffected when zone pricing is later changed by admin.
ALTER TABLE orders
  ADD COLUMN delivery_pincode           VARCHAR(6),
  ADD COLUMN delivery_zone_code         VARCHAR(50),
  ADD COLUMN delivery_method            VARCHAR(20),
  ADD COLUMN estimated_delivery_days    INT,
  ADD COLUMN estimated_delivery_minutes INT;

-- ── Seed delivery zones ───────────────────────────────────────────────────────
INSERT INTO delivery_zones
  (id, code, name,
   standard_delivery_charge, express_delivery_charge,
   standard_delivery_days,   express_delivery_minutes,
   express_available, cod_available, is_active)
VALUES
  ('d1000000-0000-0000-0001-000000000001', 'METRO_EXPRESS',   'Metro Express',    49.00, 99.00, 2, 120, true,  true,  true),
  ('d1000000-0000-0000-0001-000000000002', 'METRO_STANDARD',  'Metro Standard',   49.00,  0.00, 4,   0, false, true,  true),
  ('d1000000-0000-0000-0001-000000000003', 'REGIONAL',        'Regional Cities',  79.00,  0.00, 6,   0, false, true,  true),
  ('d1000000-0000-0000-0001-000000000004', 'REMOTE',          'Remote Areas',     99.00,  0.00, 10,  0, false, false, true);

-- ── Assign seed pincodes to zones ─────────────────────────────────────────────
UPDATE pincodes SET zone_id = 'd1000000-0000-0000-0001-000000000001'
  WHERE pincode IN ('400001', '110001', '411001');

UPDATE pincodes SET zone_id = 'd1000000-0000-0000-0001-000000000002'
  WHERE pincode IN ('560001', '500001', '380001');

UPDATE pincodes SET zone_id = 'd1000000-0000-0000-0001-000000000003'
  WHERE pincode IN ('600001', '700001', '302001', '226001', '160001');

UPDATE pincodes SET zone_id = 'd1000000-0000-0000-0001-000000000004'
  WHERE pincode IN ('682001');
