-- ─────────────────────────────────────────────────────────────────────────────
-- V40: Clean all vendor payment data, insert 7 realistic synced records
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. Delete all existing vendor payments
DELETE FROM vendor_payments;

-- 2. Reset PO payment_status to pending for all POs
UPDATE purchase_orders SET payment_status = 'pending';

-- 3. Reset sequence to 1001
SELECT setval('vendor_payment_seq', 1000, true);

-- 4. Insert 7 clean vendor payments across all statuses and vendors

INSERT INTO vendor_payments (
    id, vendor_id, purchase_order_id,
    invoice_number, invoice_date,
    gross_amount, tds_percent, tds_amount, commission_amount, net_payable,
    status, payment_method,
    utr_number, payment_date, approved_at,
    failure_reason,
    created_at, updated_at
) VALUES

-- 1. Rajesh Textiles | PO-2026-0003 | PAID
('PAY1001',
 '5eed0000-0000-0000-0007-000000000001',
 '5eed0000-0000-0000-0021-000000000003',
 'INV-RT-2026-001', '2026-08-10',
 120000.00, 2.00, 2400.00, 0.00, 117600.00,
 'paid', 'NEFT',
 'NEFT20260812001234', '2026-08-12',
 '2026-08-11 10:00:00+05:30', NULL,
 now(), now()),

-- 2. Surat Silk House | PO-2026-0001 | PAID
('PAY1002',
 '5eed0000-0000-0000-0007-000000000002',
 '5eed0000-0000-0000-0021-000000000001',
 'INV-SS-2026-041', '2026-08-18',
 185000.00, 2.00, 3700.00, 0.00, 181300.00,
 'paid', 'RTGS',
 'RTGS20260820005678', '2026-08-20',
 '2026-08-19 14:30:00+05:30', NULL,
 now(), now()),

-- 3. Lucknow Chikan Emporium | PO-2026-0002 | PROCESSING (approved, awaiting UTR)
('PAY1003',
 '5eed0000-0000-0000-0007-000000000003',
 '5eed0000-0000-0000-0021-000000000002',
 'INV-LC-2026-017', '2026-09-02',
 95000.00, 2.00, 1900.00, 0.00, 93100.00,
 'processing', 'NEFT',
 NULL, NULL,
 '2026-09-04 09:15:00+05:30', NULL,
 now(), now()),

-- 4. Rajesh Textiles | PO-1789229046656 | PENDING
('PAY1004',
 '5eed0000-0000-0000-0007-000000000001',
 '9662ef48-d404-4fb0-a259-9d9a222a2f8b',
 'INV-RT-2026-002', '2026-09-10',
 54000.00, 2.00, 1080.00, 0.00, 52920.00,
 'pending', 'UPI',
 NULL, NULL, NULL, NULL,
 now(), now()),

-- 5. Abhishek Textiles | PO-1789138119023 | PENDING
('PAY1005',
 '3cf83d31-e275-4fce-97e7-0c7e42443139',
 '567e8af5-1f14-455b-92ae-60a69df23cb3',
 'INV-AT-2026-003', '2026-09-15',
 21000.00, 2.00, 420.00, 0.00, 20580.00,
 'pending', 'NEFT',
 NULL, NULL, NULL, NULL,
 now(), now()),

-- 6. Surat Silk House | PO-2026-0001 | FAILED (bank details mismatch)
('PAY1006',
 '5eed0000-0000-0000-0007-000000000002',
 '5eed0000-0000-0000-0021-000000000001',
 'INV-SS-2026-042', '2026-09-20',
 42000.00, 2.00, 840.00, 0.00, 41160.00,
 'failed', 'NEFT',
 NULL, NULL,
 '2026-09-21 11:00:00+05:30',
 'Bank account IFSC code invalid — transaction rejected by NPCI',
 now(), now()),

-- 7. Lucknow Chikan Emporium | PO-1789062034056 | ON HOLD (compliance check)
('PAY1007',
 '5eed0000-0000-0000-0007-000000000003',
 '0fdc9978-0dc7-44be-995b-b7b41135ae88',
 'INV-LC-2026-018', '2026-09-25',
 68000.00, 2.00, 1360.00, 0.00, 66640.00,
 'on_hold', 'RTGS',
 NULL, NULL,
 '2026-09-26 16:00:00+05:30', NULL,
 now(), now());

-- 5. Update sequence past inserted IDs
SELECT setval('vendor_payment_seq', 1007, true);

-- 6. Sync PO payment_status based on new vendor payments
UPDATE purchase_orders po
SET payment_status = CASE
  WHEN (
    SELECT COUNT(*) FROM vendor_payments vp
    WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL AND vp.status != 'failed'
  ) = 0 THEN 'pending'
  WHEN (
    SELECT COUNT(*) FROM vendor_payments vp
    WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL
      AND vp.status != 'failed' AND vp.status != 'paid'
  ) = 0 THEN 'paid'
  WHEN (
    SELECT COUNT(*) FROM vendor_payments vp
    WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL AND vp.status = 'paid'
  ) > 0 THEN 'partial'
  ELSE 'pending'
END
WHERE EXISTS (
  SELECT 1 FROM vendor_payments vp
  WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL
);

-- 7. Fix PO total_amounts to match their vendor payment amounts
UPDATE purchase_orders SET total_amount = 120000.00 WHERE id = '5eed0000-0000-0000-0021-000000000003';
UPDATE purchase_orders SET total_amount = 227000.00 WHERE id = '5eed0000-0000-0000-0021-000000000001'; -- PAY1002(185000)+PAY1006(42000 failed)
UPDATE purchase_orders SET total_amount = 95000.00  WHERE id = '5eed0000-0000-0000-0021-000000000002';
UPDATE purchase_orders SET total_amount = 54000.00  WHERE id = '9662ef48-d404-4fb0-a259-9d9a222a2f8b';
UPDATE purchase_orders SET total_amount = 21000.00  WHERE id = '567e8af5-1f14-455b-92ae-60a69df23cb3';
UPDATE purchase_orders SET total_amount = 68000.00  WHERE id = '0fdc9978-0dc7-44be-995b-b7b41135ae88';
