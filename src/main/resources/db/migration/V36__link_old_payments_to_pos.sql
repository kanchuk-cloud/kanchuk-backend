-- Link PAY1001–PAY1008 to appropriate POs based on vendor

-- PAY1001: Rajesh Textiles, paid → PO-1789296370666 (received, Rajesh, total 112000)
UPDATE vendor_payments SET purchase_order_id = 'f091084b-70ba-4341-b09e-2997f0e7cdd4' WHERE id = 'PAY1001';

-- PAY1002: Surat Silk, paid → PO-2026-0001 (confirmed, Surat Silk, total 185000)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000001' WHERE id = 'PAY1002';

-- PAY1003: Lucknow Chikan, processing → PO-2026-0002 (received, Lucknow Chikan)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000002' WHERE id = 'PAY1003';

-- PAY1004: Rajesh Textiles, pending → PO-1789301275002 (partially_received, Rajesh, total 61600)
UPDATE vendor_payments SET purchase_order_id = 'd33ac654-9c55-4547-9858-bf55d71409f1' WHERE id = 'PAY1004';

-- PAY1005: Surat Silk, pending → PO-2026-0001 (Surat Silk)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000001' WHERE id = 'PAY1005';

-- PAY1006: Abhishek textiles, failed → PO-1789138119023 (closed, Abhishek, total 21035)
UPDATE vendor_payments SET purchase_order_id = '567e8af5-1f14-455b-92ae-60a69df23cb3' WHERE id = 'PAY1006';

-- PAY1007: Lucknow Chikan, on_hold → PO-2026-0002 (Lucknow Chikan)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000002' WHERE id = 'PAY1007';

-- PAY1008: Abhishek textiles, pending → PO-1789062034056 (sent_to_vendor, Abhishek)
UPDATE vendor_payments SET purchase_order_id = '0fdc9978-0dc7-44be-995b-b7b41135ae88' WHERE id = 'PAY1008';

-- Re-sync PO payment statuses after linking new payments

-- PO-1789296370666 (Rajesh, total 112000): PAY1001 paid (net ~24500) → partial
UPDATE purchase_orders SET payment_status = 'partial' WHERE id = 'f091084b-70ba-4341-b09e-2997f0e7cdd4';

-- PO-2026-0001 (Surat Silk, total 185000): PAY1011 paid(86240) + PAY1002 paid(~18130) + PAY1005 pending → partial
-- already 'partial', no change needed

-- PO-2026-0002 (Lucknow Chikan, total 42000): PAY1012 processing + PAY1003 processing + PAY1007 on_hold → pending
-- no paid payments, stays pending

-- PO-1789301275002 (Rajesh, total 61600): PAY1004 pending → pending (no change)

-- PO-1789138119023 (Abhishek, total 21035): PAY1006 failed → failed payment doesn't count, stays partial
