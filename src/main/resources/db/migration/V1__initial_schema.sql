-- ============================================================
-- Kanchuk — Initial Schema
-- ============================================================

-- ── Loyalty tiers (referenced by users) ──────────────────────
CREATE TABLE loyalty_tiers (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(50)  NOT NULL UNIQUE,
  min_points       INT          NOT NULL DEFAULT 0,
  discount_percent DECIMAL(5,2) NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Tax ───────────────────────────────────────────────────────
CREATE TABLE tax_categories (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name       VARCHAR(100) NOT NULL UNIQUE,
  hsn_code   VARCHAR(8),
  is_active  BOOLEAN      NOT NULL DEFAULT true,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE tax_rates (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  tax_category_id UUID         NOT NULL REFERENCES tax_categories(id),
  rate            DECIMAL(5,2) NOT NULL,
  tax_type        VARCHAR(10)  NOT NULL CHECK (tax_type IN ('GST','IGST','CGST','SGST')),
  state_code      VARCHAR(2),
  is_active       BOOLEAN      NOT NULL DEFAULT true,
  created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_tax_rates_category ON tax_rates(tax_category_id);

-- ── Fabrics ───────────────────────────────────────────────────
CREATE TABLE fabrics (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(100) NOT NULL UNIQUE,
  fabric_family VARCHAR(100) NOT NULL,
  sort_order    INT          NOT NULL DEFAULT 0,
  is_active     BOOLEAN      NOT NULL DEFAULT true,
  deleted_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_fabrics_family ON fabrics(fabric_family);

-- ── Categories ────────────────────────────────────────────────
CREATE TABLE categories (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(100) NOT NULL,
  slug             VARCHAR(100) NOT NULL UNIQUE,
  code             VARCHAR(10)  NOT NULL UNIQUE,
  image_url        TEXT,
  icon             VARCHAR(50),
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE sub_categories (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id      UUID         NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
  name             VARCHAR(100) NOT NULL,
  slug             VARCHAR(100) NOT NULL,
  code             VARCHAR(10)  NOT NULL,
  image_url        TEXT,
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  UNIQUE (category_id, slug),
  UNIQUE (category_id, code)
);
CREATE INDEX idx_sub_categories_category_id ON sub_categories(category_id);

-- ── SKU sequences ─────────────────────────────────────────────
CREATE TABLE sku_sequences (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id     UUID NOT NULL REFERENCES categories(id),
  sub_category_id UUID REFERENCES sub_categories(id),
  last_number     INT  NOT NULL DEFAULT 0,
  UNIQUE (category_id, sub_category_id)
);

CREATE OR REPLACE FUNCTION generate_product_sku(
  p_category_id     UUID,
  p_sub_category_id UUID DEFAULT NULL
) RETURNS TEXT AS $$
DECLARE
  v_cat_code  VARCHAR(10);
  v_sub_code  VARCHAR(10);
  v_next_num  INT;
BEGIN
  SELECT code INTO v_cat_code FROM categories     WHERE id = p_category_id;
  SELECT code INTO v_sub_code FROM sub_categories WHERE id = p_sub_category_id;
  UPDATE sku_sequences
     SET last_number = last_number + 1
   WHERE category_id = p_category_id
     AND (sub_category_id = p_sub_category_id
          OR (sub_category_id IS NULL AND p_sub_category_id IS NULL))
  RETURNING last_number INTO v_next_num;
  IF NOT FOUND THEN
    INSERT INTO sku_sequences (category_id, sub_category_id, last_number)
    VALUES (p_category_id, p_sub_category_id, 1)
    RETURNING last_number INTO v_next_num;
  END IF;
  IF p_sub_category_id IS NOT NULL THEN
    RETURN v_cat_code || '-' || v_sub_code || '-' || LPAD(v_next_num::TEXT, 4, '0');
  ELSE
    RETURN v_cat_code || '-' || LPAD(v_next_num::TEXT, 4, '0');
  END IF;
END;
$$ LANGUAGE plpgsql;

-- ── Sellers ───────────────────────────────────────────────────
CREATE TABLE sellers (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name         VARCHAR(200) NOT NULL,
  email        VARCHAR(255) UNIQUE,
  phone        VARCHAR(15),
  gstin        VARCHAR(15),
  state_code   VARCHAR(2),
  sort_order   INT         NOT NULL DEFAULT 0,
  is_active    BOOLEAN     NOT NULL DEFAULT true,
  published_at TIMESTAMPTZ,
  expires_at   TIMESTAMPTZ,
  deleted_at   TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Designers ─────────────────────────────────────────────────
CREATE TABLE designers (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(200) NOT NULL,
  slug             VARCHAR(200) NOT NULL UNIQUE,
  avatar_url       TEXT,
  bio              TEXT,
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Products ──────────────────────────────────────────────────
CREATE TABLE products (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  sku               VARCHAR(100) NOT NULL UNIQUE,
  name              VARCHAR(255) NOT NULL,
  short_description TEXT,
  description       TEXT,
  gender            VARCHAR(20)  NOT NULL DEFAULT 'women',
  category_id       UUID REFERENCES categories(id),
  sub_category_id   UUID REFERENCES sub_categories(id),
  fabric_id         UUID REFERENCES fabrics(id),
  tax_category_id   UUID REFERENCES tax_categories(id),
  designer_id       UUID REFERENCES designers(id),
  thumbnail_url     TEXT,
  pattern           VARCHAR(50),
  sleeve            VARCHAR(50),
  craft_type        VARCHAR(100),
  occasions         JSONB,
  tags              JSONB,
  care_instructions JSONB,
  return_policy     TEXT,
  is_handloom       BOOLEAN     NOT NULL DEFAULT false,
  is_designer       BOOLEAN     NOT NULL DEFAULT false,
  is_new            BOOLEAN     NOT NULL DEFAULT false,
  is_bestseller     BOOLEAN     NOT NULL DEFAULT false,
  is_trending       BOOLEAN     NOT NULL DEFAULT false,
  is_active         BOOLEAN     NOT NULL DEFAULT true,
  sort_order        INT         NOT NULL DEFAULT 0,
  published_at      TIMESTAMPTZ,
  expires_at        TIMESTAMPTZ,
  meta_title        VARCHAR(70),
  meta_description  VARCHAR(160),
  og_image_url      TEXT,
  deleted_at        TIMESTAMPTZ,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_fabric   ON products(fabric_id);
CREATE INDEX idx_products_sku      ON products(sku);

CREATE TABLE product_images (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  image_url  TEXT NOT NULL,
  alt_text   VARCHAR(255),
  sort_order INT  NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_option_types (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id   UUID        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  name         VARCHAR(50) NOT NULL,
  display_name VARCHAR(100),
  sort_order   INT         NOT NULL DEFAULT 0,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_option_values (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  option_type_id UUID        NOT NULL REFERENCES product_option_types(id) ON DELETE CASCADE,
  value          VARCHAR(100) NOT NULL,
  code           VARCHAR(10)  NOT NULL,
  sort_order     INT          NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE product_variants (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  product_id    UUID        NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  sku           VARCHAR(150) NOT NULL UNIQUE,
  thumbnail_url TEXT,
  is_active     BOOLEAN     NOT NULL DEFAULT true,
  deleted_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE variant_option_values (
  variant_id      UUID NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
  option_value_id UUID NOT NULL REFERENCES product_option_values(id) ON DELETE CASCADE,
  PRIMARY KEY (variant_id, option_value_id)
);

CREATE TABLE product_listings (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  variant_id       UUID         NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
  seller_id        UUID REFERENCES sellers(id),
  price            DECIMAL(10,2) NOT NULL,
  compare_at_price DECIMAL(10,2),
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Inventory ─────────────────────────────────────────────────
CREATE TABLE stock_locations (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name       VARCHAR(100) NOT NULL,
  address    VARCHAR(255),
  city       VARCHAR(100),
  state      VARCHAR(100),
  pincode    VARCHAR(6),
  is_active  BOOLEAN     NOT NULL DEFAULT true,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE inventory_levels (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  listing_id        UUID NOT NULL REFERENCES product_listings(id) ON DELETE CASCADE,
  location_id       UUID NOT NULL REFERENCES stock_locations(id),
  quantity_on_hand  INT  NOT NULL DEFAULT 0,
  quantity_reserved INT  NOT NULL DEFAULT 0,
  created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (listing_id, location_id)
);

-- ── Facets ────────────────────────────────────────────────────
CREATE TABLE facets (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name        VARCHAR(100) NOT NULL,
  code        VARCHAR(100) NOT NULL UNIQUE,
  filter_type VARCHAR(20)  NOT NULL DEFAULT 'checkbox',
  sort_order  INT          NOT NULL DEFAULT 0,
  is_active   BOOLEAN      NOT NULL DEFAULT true,
  deleted_at  TIMESTAMPTZ,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE facet_values (
  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  facet_id   UUID        NOT NULL REFERENCES facets(id) ON DELETE CASCADE,
  value      VARCHAR(100) NOT NULL,
  code       VARCHAR(10),
  sort_order INT          NOT NULL DEFAULT 0,
  is_active  BOOLEAN      NOT NULL DEFAULT true,
  deleted_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE product_facet_values (
  product_id     UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
  facet_value_id UUID NOT NULL REFERENCES facet_values(id) ON DELETE CASCADE,
  PRIMARY KEY (product_id, facet_value_id)
);

-- ── Size charts ───────────────────────────────────────────────
CREATE TABLE size_charts (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  category_id UUID REFERENCES categories(id),
  name        VARCHAR(200) NOT NULL,
  unit        VARCHAR(20),
  chart_data  JSONB        NOT NULL,
  is_active   BOOLEAN      NOT NULL DEFAULT true,
  deleted_at  TIMESTAMPTZ,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Users ─────────────────────────────────────────────────────
CREATE TABLE users (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(100)  NOT NULL,
  email            VARCHAR(255)  NOT NULL UNIQUE,
  phone            VARCHAR(15),
  password_hash    TEXT          NOT NULL,
  avatar_url       TEXT,
  wallet_balance   DECIMAL(10,2) NOT NULL DEFAULT 0,
  loyalty_points   INT           NOT NULL DEFAULT 0,
  loyalty_tier_id  UUID REFERENCES loyalty_tiers(id),
  is_active        BOOLEAN       NOT NULL DEFAULT true,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE addresses (
  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id     UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  type        VARCHAR(10) NOT NULL CHECK (type IN ('home','work','other')),
  name        VARCHAR(100) NOT NULL,
  phone       VARCHAR(15)  NOT NULL,
  line1       TEXT         NOT NULL,
  line2       TEXT,
  city        VARCHAR(100) NOT NULL,
  state       VARCHAR(100) NOT NULL,
  pincode     VARCHAR(6)   NOT NULL,
  is_default  BOOLEAN      NOT NULL DEFAULT false,
  is_active   BOOLEAN      NOT NULL DEFAULT true,
  deleted_at  TIMESTAMPTZ,
  created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_addresses_user_id ON addresses(user_id);

-- ── Marketing ─────────────────────────────────────────────────
CREATE TABLE occasions (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name             VARCHAR(100) NOT NULL,
  slug             VARCHAR(100) NOT NULL UNIQUE,
  image_url        TEXT,
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE banners (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title            VARCHAR(200) NOT NULL,
  image_url        TEXT         NOT NULL,
  mobile_image_url TEXT,
  link_url         TEXT,
  placement        VARCHAR(50),
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE offers (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  title            VARCHAR(255) NOT NULL,
  description      TEXT,
  image_url        TEXT,
  occasion_id      UUID REFERENCES occasions(id),
  discount_percent INT,
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  published_at     TIMESTAMPTZ,
  expires_at       TIMESTAMPTZ,
  meta_title       VARCHAR(70),
  meta_description VARCHAR(160),
  og_image_url     TEXT,
  deleted_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE promotions (
  id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name                VARCHAR(200)  NOT NULL,
  coupon_code         VARCHAR(50)   UNIQUE,
  discount_type       VARCHAR(20)   NOT NULL,
  discount_value      DECIMAL(10,2) NOT NULL,
  min_order_amount    DECIMAL(10,2),
  max_discount_amount DECIMAL(10,2),
  usage_limit         INT,
  used_count          INT           NOT NULL DEFAULT 0,
  is_active           BOOLEAN       NOT NULL DEFAULT true,
  starts_at           TIMESTAMPTZ,
  expires_at          TIMESTAMPTZ,
  deleted_at          TIMESTAMPTZ,
  created_at          TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at          TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE gift_cards (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  code             VARCHAR(50)   NOT NULL UNIQUE,
  initial_amount   DECIMAL(10,2) NOT NULL,
  remaining_amount DECIMAL(10,2) NOT NULL,
  is_active        BOOLEAN       NOT NULL DEFAULT true,
  expires_at       TIMESTAMPTZ,
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ── Shipping / Logistics ──────────────────────────────────────
CREATE TABLE shipping_methods (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name           VARCHAR(100)  NOT NULL,
  carrier        VARCHAR(100),
  base_price     DECIMAL(10,2) NOT NULL,
  free_above     DECIMAL(10,2),
  estimated_days INT,
  is_active      BOOLEAN       NOT NULL DEFAULT true,
  sort_order     INT           NOT NULL DEFAULT 0,
  deleted_at     TIMESTAMPTZ,
  created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE pincodes (
  pincode        VARCHAR(6) PRIMARY KEY,
  city           VARCHAR(100),
  state          VARCHAR(100),
  estimated_days INT,
  hyperlocal     BOOLEAN NOT NULL DEFAULT false,
  is_serviceable BOOLEAN NOT NULL DEFAULT true
);

-- ── Orders ────────────────────────────────────────────────────
CREATE TABLE orders (
  id                        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_number              VARCHAR(50)   NOT NULL UNIQUE,
  user_id                   UUID REFERENCES users(id),
  status                    VARCHAR(30)   NOT NULL DEFAULT 'pending',
  subtotal                  DECIMAL(10,2) NOT NULL,
  discount_amount           DECIMAL(10,2) NOT NULL DEFAULT 0,
  delivery_charge           DECIMAL(10,2) NOT NULL DEFAULT 0,
  total                     DECIMAL(10,2) NOT NULL,
  promotion_id              UUID REFERENCES promotions(id),
  shipping_method_id        UUID REFERENCES shipping_methods(id),
  delivery_address_snapshot TEXT,
  notes                     TEXT,
  placed_at                 TIMESTAMPTZ,
  created_at                TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at                TIMESTAMPTZ   NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_status  ON orders(status);

CREATE TABLE order_items (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id         UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  listing_id       UUID REFERENCES product_listings(id),
  price            DECIMAL(10,2) NOT NULL,
  quantity         INT           NOT NULL,
  product_snapshot TEXT,
  created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE packaging (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id     UUID        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  packed_by    VARCHAR(100),
  package_type VARCHAR(50),
  weight_grams INT,
  dimensions_cm JSONB,
  status       VARCHAR(20) NOT NULL DEFAULT 'pending'
                 CHECK (status IN ('pending','in_progress','packed')),
  notes        TEXT,
  packed_at    TIMESTAMPTZ,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (order_id)
);

CREATE TABLE fulfillments (
  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id        UUID        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  status          VARCHAR(30) NOT NULL DEFAULT 'pending',
  tracking_number VARCHAR(100),
  carrier         VARCHAR(100),
  shipped_at      TIMESTAMPTZ,
  delivered_at    TIMESTAMPTZ,
  notes           TEXT,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE dispatching (
  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id       UUID        NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
  packaging_id   UUID REFERENCES packaging(id),
  fulfillment_id UUID REFERENCES fulfillments(id),
  dispatched_by  VARCHAR(100),
  carrier        VARCHAR(100),
  awb_number     VARCHAR(100),
  dispatch_type  VARCHAR(20) NOT NULL DEFAULT 'courier_pickup'
                   CHECK (dispatch_type IN ('courier_pickup','self_drop','hyperlocal')),
  status         VARCHAR(20) NOT NULL DEFAULT 'pending'
                   CHECK (status IN ('pending','ready_to_dispatch','dispatched')),
  dispatched_at  TIMESTAMPTZ,
  notes          TEXT,
  created_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (order_id)
);

CREATE TABLE returns (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  order_id      UUID          NOT NULL REFERENCES orders(id),
  order_item_id UUID REFERENCES order_items(id),
  status        VARCHAR(30)   NOT NULL DEFAULT 'requested',
  reason        TEXT,
  refund_amount DECIMAL(10,2),
  requested_at  TIMESTAMPTZ,
  resolved_at   TIMESTAMPTZ,
  created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ── Purchase orders ───────────────────────────────────────────
CREATE TABLE purchase_orders (
  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  po_number    VARCHAR(50)   NOT NULL UNIQUE,
  seller_id    UUID REFERENCES sellers(id),
  status       VARCHAR(30)   NOT NULL DEFAULT 'draft',
  total_amount DECIMAL(10,2),
  expected_at  TIMESTAMPTZ,
  received_at  TIMESTAMPTZ,
  notes        TEXT,
  created_at   TIMESTAMPTZ   NOT NULL DEFAULT now(),
  updated_at   TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- ── Admin users ───────────────────────────────────────────────
CREATE TABLE admin_users (
  id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  name          VARCHAR(100) NOT NULL,
  email         VARCHAR(255) NOT NULL UNIQUE,
  password_hash TEXT         NOT NULL,
  role          VARCHAR(20)  NOT NULL DEFAULT 'ADMIN',
  is_active     BOOLEAN      NOT NULL DEFAULT true,
  deleted_at    TIMESTAMPTZ,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── AI Stylist ────────────────────────────────────────────────
CREATE TABLE blouse_recommendations (
  id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  applies_fabric    VARCHAR(100),
  applies_pattern   VARCHAR(100),
  applies_occasion  VARCHAR(100),
  style_name        VARCHAR(200) NOT NULL,
  neckline          VARCHAR(100) NOT NULL,
  sleeve            VARCHAR(100) NOT NULL,
  embellishment     VARCHAR(100) NOT NULL,
  fabric_suggestion VARCHAR(200) NOT NULL,
  reason_text       TEXT         NOT NULL,
  tag               VARCHAR(50),
  image_url         TEXT,
  sort_order        INT          NOT NULL DEFAULT 0,
  is_active         BOOLEAN      NOT NULL DEFAULT true,
  created_at        TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at        TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE suit_styling_recommendations (
  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  applies_fabric   VARCHAR(100),
  applies_pattern  VARCHAR(100),
  applies_occasion VARCHAR(100),
  style_name       VARCHAR(200) NOT NULL,
  bottom_style     VARCHAR(100) NOT NULL,
  kurta_style      VARCHAR(100) NOT NULL,
  dupatta_style    VARCHAR(100) NOT NULL,
  reason_text      TEXT         NOT NULL,
  tag              VARCHAR(50),
  image_url        TEXT,
  sort_order       INT          NOT NULL DEFAULT 0,
  is_active        BOOLEAN      NOT NULL DEFAULT true,
  created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- ── Seed: default admin ───────────────────────────────────────
-- Password: Admin@123 (bcrypt hash)
INSERT INTO admin_users (name, email, password_hash, role)
VALUES ('Super Admin', 'admin@kanchuk.in',
        '$2a$12$TBNbXXEVlnEGGz/jkXOOVeSz9TiUME4sW5Xp10yHJ3X6oSTe5Y6.e',
        'ADMIN');
