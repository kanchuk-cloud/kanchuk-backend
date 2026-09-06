-- ============================================================
-- V13 — End-to-end demo seed for delivery routing
--
-- Scenario: Two hubs serving Delhi NCR
--   Hub A → "Central Delhi Hub"  (Connaught Place, 110001) — primary for METRO_EXPRESS
--   Hub B → "Delhi Distribution Hub" (Noida, 201301) — primary for METRO_STANDARD
--
-- Test pincodes (South Delhi) → METRO_EXPRESS zone → routed to Hub A first
-- If Hub A out of stock → fallback to Hub B
--
-- Demo product behaviour:
--   SAR-SLK-0001 (Banarasi Silk Saree)  — IN STOCK at Hub A → shows Hub A
--   SAR-SLK-0002 (Kanjivaram Silk)      — IN STOCK at Hub A → shows Hub A
--   LEH-BRD-0001 (Bridal Lehenga)       — NO STOCK at Hub A, IN STOCK at Hub B
--                                         → shows "nearest available hub" (Hub B)
--   KRT-ANK-0001 (Anarkali Kurta)       — IN STOCK at Hub B only
-- ============================================================

-- ── 1. New hub: Central Delhi ─────────────────────────────────────────────────

INSERT INTO stock_locations (id, name, address, city, state, pincode, is_active) VALUES
  ('5eed0000-0000-0000-000f-000000000004',
   'Central Delhi Hub',
   'F-12, Connaught Circus, New Delhi',
   'New Delhi', 'Delhi', '110001', true);

-- ── 2. South Delhi & NCR pincodes → correct zones ─────────────────────────────
--    All inserted with zone_id; estimated_days kept for backward compat.

-- 110001 (New Delhi) already inserted in V3 and zone assigned in V11 — skip it here.
INSERT INTO pincodes (pincode, city, state, estimated_days, hyperlocal, is_serviceable, zone_id) VALUES
  -- South Delhi → METRO_EXPRESS (same-day/next-day eligible)
  ('110003', 'Daryaganj, Delhi',      'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110017', 'Vasant Vihar, Delhi',   'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110024', 'Lajpat Nagar, Delhi',   'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110029', 'Hauz Khas, Delhi',      'Delhi',         2, true,  true, 'd1000000-0000-0000-0001-000000000001'),
  ('110048', 'Greater Kailash I',     'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110049', 'Greater Kailash II',    'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110062', 'Malviya Nagar, Delhi',  'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110065', 'Vasant Kunj, Delhi',    'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110070', 'Dwarka, Delhi',         'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),
  ('110077', 'Dwarka Sector 6',       'Delhi',         2, false, true, 'd1000000-0000-0000-0001-000000000001'),

  -- NCR (Noida / Gurugram) → METRO_STANDARD
  ('201301', 'Noida Sector 63',       'Uttar Pradesh', 4, false, true, 'd1000000-0000-0000-0001-000000000002'),
  ('201309', 'Noida Extension',       'Uttar Pradesh', 4, false, true, 'd1000000-0000-0000-0001-000000000002'),
  ('201310', 'Greater Noida',         'Uttar Pradesh', 4, false, true, 'd1000000-0000-0000-0001-000000000002'),
  ('122001', 'Gurugram',              'Haryana',       4, false, true, 'd1000000-0000-0000-0001-000000000002'),
  ('122002', 'Gurugram Sector 14',    'Haryana',       4, false, true, 'd1000000-0000-0000-0001-000000000002'),

  -- Mumbai suburbs → METRO_STANDARD (metro hub covers only 400001)
  ('400050', 'Bandra, Mumbai',        'Maharashtra',   4, false, true, 'd1000000-0000-0000-0001-000000000002'),
  ('400093', 'Andheri East, Mumbai',  'Maharashtra',   4, false, true, 'd1000000-0000-0000-0001-000000000002'),

  -- Hub own pincodes (not yet in table)
  ('395002', 'Surat',                 'Gujarat',       6, false, true, 'd1000000-0000-0000-0001-000000000003');

-- ── 3. Zone assignments per hub ───────────────────────────────────────────────
--    priority 1 = this hub is the first choice for that zone
--    priority 2 = fallback if priority-1 hub is out of stock

INSERT INTO stock_location_zones (location_id, zone_id, priority) VALUES
  -- Central Delhi Hub: primary for METRO_EXPRESS, backup for METRO_STANDARD
  ('5eed0000-0000-0000-000f-000000000004', 'd1000000-0000-0000-0001-000000000001', 1),
  ('5eed0000-0000-0000-000f-000000000004', 'd1000000-0000-0000-0001-000000000002', 2),

  -- Delhi Distribution Hub (Noida): primary for METRO_STANDARD, backup for METRO_EXPRESS
  ('5eed0000-0000-0000-000f-000000000002', 'd1000000-0000-0000-0001-000000000002', 1),
  ('5eed0000-0000-0000-000f-000000000002', 'd1000000-0000-0000-0001-000000000001', 2),

  -- Mumbai Main Warehouse: primary for its local METRO_EXPRESS zone
  ('5eed0000-0000-0000-000f-000000000001', 'd1000000-0000-0000-0001-000000000001', 3),

  -- Surat Textile Godown: serves REGIONAL cities
  ('5eed0000-0000-0000-000f-000000000003', 'd1000000-0000-0000-0001-000000000003', 1);

-- ── 4. Inventory at Central Delhi Hub ────────────────────────────────────────
--    SAR-SLK-0001 (Royal Banarasi Silk Saree): both colour variants in stock
--    SAR-SLK-0002 (Kanjivaram Silk Saree): both colour variants in stock
--    LEH-BRD-0001 (Bridal Lehenga): deliberately ZERO qty → triggers fallback

INSERT INTO inventory_levels (id, listing_id, location_id, quantity_on_hand, quantity_reserved) VALUES
  -- SAR-SLK-0001-CRM at Central Delhi Hub
  ('5eed0000-0000-0000-0019-000000000019',
   '5eed0000-0000-0000-0018-000000000001',
   '5eed0000-0000-0000-000f-000000000004', 10, 0),

  -- SAR-SLK-0001-RBL at Central Delhi Hub
  ('5eed0000-0000-0000-0019-00000000001a',
   '5eed0000-0000-0000-0018-000000000002',
   '5eed0000-0000-0000-000f-000000000004', 8, 0),

  -- SAR-SLK-0002-PCB at Central Delhi Hub
  ('5eed0000-0000-0000-0019-00000000001b',
   '5eed0000-0000-0000-0018-000000000003',
   '5eed0000-0000-0000-000f-000000000004', 12, 1),

  -- SAR-SLK-0002-EMG at Central Delhi Hub
  ('5eed0000-0000-0000-0019-00000000001c',
   '5eed0000-0000-0000-0018-000000000004',
   '5eed0000-0000-0000-000f-000000000004', 6, 0);

-- ── 5. Inventory at Delhi Distribution Hub (Noida) ───────────────────────────
--    LEH-BRD-0001 sizes: no stock at Central Delhi → fallback will route here
--    KRT-ANK-0001: already seeded at Noida in V3 (listings 0a-0d)

INSERT INTO inventory_levels (id, listing_id, location_id, quantity_on_hand, quantity_reserved) VALUES
  -- LEH-BRD-0001-XS at Noida
  ('5eed0000-0000-0000-0019-00000000001d',
   '5eed0000-0000-0000-0018-000000000005',
   '5eed0000-0000-0000-000f-000000000002', 5, 0),

  -- LEH-BRD-0001-S at Noida
  ('5eed0000-0000-0000-0019-00000000001e',
   '5eed0000-0000-0000-0018-000000000006',
   '5eed0000-0000-0000-000f-000000000002', 4, 0),

  -- LEH-BRD-0001-M at Noida
  ('5eed0000-0000-0000-0019-00000000001f',
   '5eed0000-0000-0000-0018-000000000007',
   '5eed0000-0000-0000-000f-000000000002', 3, 0),

  -- LEH-BRD-0001-L at Noida
  ('5eed0000-0000-0000-0019-000000000020',
   '5eed0000-0000-0000-0018-000000000008',
   '5eed0000-0000-0000-000f-000000000002', 2, 0),

  -- LEH-BRD-0001-XL at Noida
  ('5eed0000-0000-0000-0019-000000000021',
   '5eed0000-0000-0000-0018-000000000009',
   '5eed0000-0000-0000-000f-000000000002', 1, 0);
