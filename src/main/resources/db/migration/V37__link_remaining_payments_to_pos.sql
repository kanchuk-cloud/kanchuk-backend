-- PAY1013: Rajesh Textiles, pending → PO-1789229046656 (partially_received, Rajesh, 100800)
UPDATE vendor_payments SET purchase_order_id = '9662ef48-d404-4fb0-a259-9d9a222a2f8b' WHERE id = 'PAY1013';

-- PAY1014: Surat Silk, failed → PO-2026-0001 (Surat Silk, 185000)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000001' WHERE id = 'PAY1014';

-- PAY1015: Lucknow Chikan, on_hold → PO-2026-0002 (Lucknow Chikan)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000002' WHERE id = 'PAY1015';

-- PAY1016: Rajesh Textiles, pending → PO-2026-0003 (Rajesh, 320000 — second payment on same PO)
UPDATE vendor_payments SET purchase_order_id = '5eed0000-0000-0000-0021-000000000003' WHERE id = 'PAY1016';
