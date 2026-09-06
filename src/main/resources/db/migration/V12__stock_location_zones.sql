CREATE TABLE stock_location_zones (
  location_id UUID NOT NULL REFERENCES stock_locations(id) ON DELETE CASCADE,
  zone_id     UUID NOT NULL REFERENCES delivery_zones(id)  ON DELETE CASCADE,
  priority    INT  NOT NULL DEFAULT 1,
  PRIMARY KEY (location_id, zone_id)
);

CREATE INDEX idx_slz_zone_priority ON stock_location_zones(zone_id, priority);
