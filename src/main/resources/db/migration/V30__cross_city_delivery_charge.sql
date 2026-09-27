ALTER TABLE delivery_zones
  ADD COLUMN cross_city_delivery_charge NUMERIC(10, 2) NOT NULL DEFAULT 49.00;
