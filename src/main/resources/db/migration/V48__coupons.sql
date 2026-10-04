CREATE TABLE IF NOT EXISTS coupons (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    code          VARCHAR(50)   NOT NULL UNIQUE,
    description   VARCHAR(255),
    type          VARCHAR(15)   NOT NULL CHECK (type IN ('PERCENTAGE','FLAT')),
    discount      NUMERIC(10,2) NOT NULL,
    min_order     NUMERIC(10,2) NOT NULL DEFAULT 0,
    max_discount  NUMERIC(10,2),
    expiry_date   DATE          NOT NULL,
    usage_limit   INTEGER,
    usage_count   INTEGER       NOT NULL DEFAULT 0,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

DO $$ BEGIN
  IF (SELECT COUNT(*) FROM coupons) = 0 THEN
    INSERT INTO coupons (code, description, type, discount, min_order, max_discount, expiry_date, is_active) VALUES
      ('KANCHUK20',    '20% off up to ₹500',                  'PERCENTAGE', 20,  999,  500, '2025-12-31', true),
      ('FLAT200',      'Flat ₹200 off on orders above ₹1999', 'FLAT',       200, 1999, NULL, '2025-12-31', true),
      ('FIRSTKANCHUK', '15% off for first-time shoppers',     'PERCENTAGE', 15,  499,  300, '2025-12-31', true);
  END IF;
END $$;
