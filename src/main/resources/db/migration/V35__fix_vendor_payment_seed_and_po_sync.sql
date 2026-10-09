-- Fix PAY1012: grossAmount was 210000 but PO-2026-0002 total is 42000 (partial payment)
UPDATE vendor_payments
SET gross_amount = 35000.00,
    tds_amount   = 700.00,
    net_payable  = 34300.00
WHERE id = 'PAY1012';

-- Sync PO payment_status based on current vendor payments
-- PO-2026-0001: PAY1011 paid (86240 net) vs total 185000 → partial
UPDATE purchase_orders SET payment_status = 'partial' WHERE id = '5eed0000-0000-0000-0021-000000000001';

-- PO-2026-0002: PAY1012 is processing (not paid) → stays pending
-- (no change)

-- PO-2026-0003: PAY1010 paid (122500 net) vs total 320000 → partial
UPDATE purchase_orders SET payment_status = 'partial' WHERE id = '5eed0000-0000-0000-0021-000000000003';
