-- V23: Inventory module dummy seed data
-- stock_movements, stock_adjustments, stock_transfers, stock_transfer_items
-- Also updates inventory_levels thresholds and bin locations, adds low-stock entries

-- ── Variables (used repeatedly) ───────────────────────────────────────────────
-- Locations:
--   5eed0000-0000-0000-000f-000000000001  Mumbai Main Warehouse
--   5eed0000-0000-0000-000f-000000000002  Delhi Distribution Hub
--   5eed0000-0000-0000-000f-000000000003  Surat Textile Godown
--   5eed0000-0000-0000-000f-000000000004  Central Delhi Hub
-- Admin: f919dd10-f862-4755-bfd6-78d8eb87f5fb (Super Admin)

-- ── 1. Update inventory_levels — set thresholds and bin locations ──────────────

UPDATE inventory_levels SET
    low_stock_threshold = 10,
    bin_location = 'A-01-L1'
WHERE listing_id = '5eed0000-0000-0000-0018-000000000004'
  AND location_id = '5eed0000-0000-0000-000f-000000000004';

UPDATE inventory_levels SET
    low_stock_threshold = 15,
    bin_location = 'B-03-L2'
WHERE listing_id = '5eed0000-0000-0000-0018-000000000001'
  AND location_id = '5eed0000-0000-0000-000f-000000000004';

UPDATE inventory_levels SET
    low_stock_threshold = 8,
    bin_location = 'C-02-L1'
WHERE listing_id = '5eed0000-0000-0000-0018-000000000002'
  AND location_id = '5eed0000-0000-0000-000f-000000000001';

UPDATE inventory_levels SET
    low_stock_threshold = 10,
    bin_location = 'A-04-L3'
WHERE listing_id = '87db8a01-28d5-4236-9ee4-27ec82655a5b'
  AND location_id = '5eed0000-0000-0000-000f-000000000004';

UPDATE inventory_levels SET
    low_stock_threshold = 20,
    bin_location = 'D-01-L2',
    quantity_on_hand = 18
WHERE listing_id = '384c36ae-a349-48cb-8893-0435db8fdfc9'
  AND location_id = '5eed0000-0000-0000-000f-000000000002';

UPDATE inventory_levels SET
    low_stock_threshold = 30,
    bin_location = 'E-05-L1'
WHERE listing_id = '2fc6e105-4bf2-468f-84a6-950fca455c30'
  AND location_id = '5eed0000-0000-0000-000f-000000000004';

-- ── 2. stock_movements — varied movement types ─────────────────────────────────

INSERT INTO stock_movements (id, listing_id, location_id, movement_type, qty_before, qty_change, qty_after, reference_type, reference_id, notes, created_by, created_at)
VALUES

-- GRN receipts
('a1000001-0000-0000-0000-000000000001',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'grn_receipt', 0, 20, 20, 'goods_receipt', gen_random_uuid(),
 'Initial stock from GRN-2024-001', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '45 days'),

('a1000001-0000-0000-0000-000000000002',
 '5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-000f-000000000004',
 'grn_receipt', 0, 30, 30, 'goods_receipt', gen_random_uuid(),
 'Initial stock from GRN-2024-002', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '40 days'),

('a1000001-0000-0000-0000-000000000003',
 '5eed0000-0000-0000-0018-000000000002', '5eed0000-0000-0000-000f-000000000001',
 'grn_receipt', 0, 25, 25, 'goods_receipt', gen_random_uuid(),
 'Stock received from Mumbai vendor', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '35 days'),

('a1000001-0000-0000-0000-000000000004',
 '384c36ae-a349-48cb-8893-0435db8fdfc9', '5eed0000-0000-0000-000f-000000000002',
 'grn_receipt', 0, 40, 40, 'goods_receipt', gen_random_uuid(),
 'Restock from GRN-2024-010', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '30 days'),

('a1000001-0000-0000-0000-000000000005',
 '2fc6e105-4bf2-468f-84a6-950fca455c30', '5eed0000-0000-0000-000f-000000000004',
 'grn_receipt', 0, 60, 60, 'goods_receipt', gen_random_uuid(),
 'Large batch from Surat supplier', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '25 days'),

-- Order deductions (sales)
('a1000001-0000-0000-0000-000000000006',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'sale', 20, -8, 12, 'order', gen_random_uuid(),
 'Online order fulfillment', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '20 days'),

('a1000001-0000-0000-0000-000000000007',
 '5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-000f-000000000004',
 'sale', 30, -12, 18, 'order', gen_random_uuid(),
 'Bulk order - wedding season', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '18 days'),

('a1000001-0000-0000-0000-000000000008',
 '2fc6e105-4bf2-468f-84a6-950fca455c30', '5eed0000-0000-0000-000f-000000000004',
 'sale', 60, -10, 50, 'order', gen_random_uuid(),
 'Festival sale orders', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '15 days'),

-- Adjustments
('a1000001-0000-0000-0000-000000000009',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'adjustment', 12, -3, 9, 'stock_adjustment', gen_random_uuid(),
 'Damaged during transit', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '12 days'),

('a1000001-0000-0000-0000-000000000010',
 '5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-000f-000000000004',
 'adjustment', 18, -5, 13, 'stock_adjustment', gen_random_uuid(),
 'Cycle count correction - found discrepancy', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '10 days'),

-- Return
('a1000001-0000-0000-0000-000000000011',
 '384c36ae-a349-48cb-8893-0435db8fdfc9', '5eed0000-0000-0000-000f-000000000002',
 'return', 33, 2, 35, 'order', gen_random_uuid(),
 'Customer return - size exchange', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '8 days'),

-- Transfer movements
('a1000001-0000-0000-0000-000000000012',
 '5eed0000-0000-0000-0018-000000000002', '5eed0000-0000-0000-000f-000000000001',
 'transfer_out', 25, -8, 17, 'stock_transfer', gen_random_uuid(),
 'Transfer to Delhi for high demand', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '6 days'),

('a1000001-0000-0000-0000-000000000013',
 '5eed0000-0000-0000-0018-000000000002', '5eed0000-0000-0000-000f-000000000002',
 'transfer_in', 0, 8, 8, 'stock_transfer', gen_random_uuid(),
 'Transfer received from Mumbai', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '6 days'),

-- More recent GRN receipt
('a1000001-0000-0000-0000-000000000014',
 '87db8a01-28d5-4236-9ee4-27ec82655a5b', '5eed0000-0000-0000-000f-000000000004',
 'grn_receipt', 0, 15, 15, 'goods_receipt', gen_random_uuid(),
 'New product initial stock', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '5 days'),

('a1000001-0000-0000-0000-000000000015',
 '87db8a01-28d5-4236-9ee4-27ec82655a5b', '5eed0000-0000-0000-000f-000000000004',
 'sale', 15, -10, 5, 'order', gen_random_uuid(),
 'Fast-selling new arrival orders', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '2 days'),

('a1000001-0000-0000-0000-000000000016',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'adjustment', 9, -3, 6, 'stock_adjustment', gen_random_uuid(),
 'Damaged items written off', 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '1 day');

-- ── 3. stock_adjustments ────────────────────────────────────────────────────────

INSERT INTO stock_adjustments (id, listing_id, location_id, reason_code, qty_before, qty_change, qty_after, notes, adjusted_by, created_at, updated_at)
VALUES

('b2000001-0000-0000-0000-000000000001',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'damaged', 12, -3, 9,
 '3 pieces found with torn zari borders during quality check. Disposed.',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '12 days', NOW() - INTERVAL '12 days'),

('b2000001-0000-0000-0000-000000000002',
 '5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-000f-000000000004',
 'cycle_count', 18, -5, 13,
 'Physical count found 5 units missing vs system. Likely mis-shelved. Counted 3 times - consistent.',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '10 days', NOW() - INTERVAL '10 days'),

('b2000001-0000-0000-0000-000000000003',
 '384c36ae-a349-48cb-8893-0435db8fdfc9', '5eed0000-0000-0000-000f-000000000002',
 'found', 33, 2, 35,
 '2 units found behind shelf B-04. Previously recorded as missing in last cycle count.',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '8 days', NOW() - INTERVAL '8 days'),

('b2000001-0000-0000-0000-000000000004',
 '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000004',
 'damaged', 9, -3, 6,
 'Water damage from leaking AC duct. 3 sarees written off. Insurance claim filed.',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '1 day', NOW() - INTERVAL '1 day'),

('b2000001-0000-0000-0000-000000000005',
 '2fc6e105-4bf2-468f-84a6-950fca455c30', '5eed0000-0000-0000-000f-000000000004',
 'correction', 50, 0, 50,
 'System re-sync after ERP migration. Quantities confirmed correct via physical audit.',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '3 days', NOW() - INTERVAL '3 days');

-- ── 4. stock_transfers ─────────────────────────────────────────────────────────

INSERT INTO stock_transfers (id, transfer_number, from_location_id, to_location_id, status, notes, created_by, completed_by, completed_at, created_at, updated_at)
VALUES

-- Completed transfer
('c3000001-0000-0000-0000-000000000001',
 'TRF-20260914-001',
 '5eed0000-0000-0000-000f-000000000001',  -- Mumbai Main Warehouse
 '5eed0000-0000-0000-000f-000000000002',  -- Delhi Distribution Hub
 'completed',
 'Festival season rebalancing — moving slow sellers from Mumbai to Delhi demand zone',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NOW() - INTERVAL '6 days',
 NOW() - INTERVAL '8 days', NOW() - INTERVAL '6 days'),

-- In-transit transfer
('c3000001-0000-0000-0000-000000000002',
 'TRF-20260918-002',
 '5eed0000-0000-0000-000f-000000000004',  -- Central Delhi Hub
 '5eed0000-0000-0000-000f-000000000003',  -- Surat Textile Godown
 'in_transit',
 'Moving excess saree stock to Surat for local exhibition',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NULL, NULL,
 NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'),

-- Draft transfer
('c3000001-0000-0000-0000-000000000003',
 'TRF-20260920-003',
 '5eed0000-0000-0000-000f-000000000002',  -- Delhi Distribution Hub
 '5eed0000-0000-0000-000f-000000000004',  -- Central Delhi Hub
 'draft',
 'Consolidating Delhi stock for upcoming wedding season',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NULL, NULL,
 NOW() - INTERVAL '1 hour', NOW() - INTERVAL '1 hour'),

-- Cancelled transfer
('c3000001-0000-0000-0000-000000000004',
 'TRF-20260910-004',
 '5eed0000-0000-0000-000f-000000000003',  -- Surat Textile Godown
 '5eed0000-0000-0000-000f-000000000001',  -- Mumbai Main Warehouse
 'cancelled',
 'Cancelled — vendor direct-shipped to Mumbai instead',
 'f919dd10-f862-4755-bfd6-78d8eb87f5fb',
 NULL, NULL,
 NOW() - INTERVAL '10 days', NOW() - INTERVAL '9 days');

-- ── 5. stock_transfer_items ────────────────────────────────────────────────────

INSERT INTO stock_transfer_items (id, transfer_id, listing_id, qty_requested, qty_transferred, created_at)
VALUES

-- Items for completed transfer (TRF-20260914-001)
('d4000001-0000-0000-0000-000000000001',
 'c3000001-0000-0000-0000-000000000001',
 '5eed0000-0000-0000-0018-000000000002',
 8, 8,
 NOW() - INTERVAL '8 days'),

-- Items for in-transit transfer (TRF-20260918-002)
('d4000001-0000-0000-0000-000000000002',
 'c3000001-0000-0000-0000-000000000002',
 '2fc6e105-4bf2-468f-84a6-950fca455c30',
 10, 0,
 NOW() - INTERVAL '2 days'),

('d4000001-0000-0000-0000-000000000003',
 'c3000001-0000-0000-0000-000000000002',
 '5eed0000-0000-0000-0018-000000000001',
 5, 0,
 NOW() - INTERVAL '2 days'),

-- Items for draft transfer (TRF-20260920-003)
('d4000001-0000-0000-0000-000000000004',
 'c3000001-0000-0000-0000-000000000003',
 '384c36ae-a349-48cb-8893-0435db8fdfc9',
 12, 0,
 NOW() - INTERVAL '1 hour'),

-- Items for cancelled transfer (TRF-20260910-004)
('d4000001-0000-0000-0000-000000000005',
 'c3000001-0000-0000-0000-000000000004',
 '5eed0000-0000-0000-0018-000000000004',
 5, 0,
 NOW() - INTERVAL '10 days');
