ALTER TABLE delivery_zones
  ADD COLUMN IF NOT EXISTS cross_city_delivery_days INT NOT NULL DEFAULT 5;
