-- ─────────────────────────────────────────────────────────────────────────────
-- V39: Comprehensive data correction
-- 1. Link PAY1015 to PO-2026-0002
-- 2. Fix PO total_amounts to match their linked vendor payments
-- 3. Fix missing total_amount on PO-1789062034056
-- 4. Full re-sync of all PO payment_status values
-- ─────────────────────────────────────────────────────────────────────────────

-- 1. Link PAY1015 (Lucknow Chikan, on_hold, 145000) to PO-2026-0002
UPDATE vendor_payments
SET purchase_order_id = '5eed0000-0000-0000-0021-000000000002'
WHERE id = 'PAY1015';

-- 2. Fix PO total_amounts to match active payment sums
-- PO-2026-0002: PAY1003(32000)+PAY1007(27500)+PAY1012(35000)+PAY1015(145000) = 239500
UPDATE purchase_orders SET total_amount = 239500.00 WHERE id = '5eed0000-0000-0000-0021-000000000002';

-- PO-2026-0003: only PAY1010 (gross 125000) — align total with actual payment
UPDATE purchase_orders SET total_amount = 125000.00 WHERE id = '5eed0000-0000-0000-0021-000000000003';

-- PO-1789296370666: only PAY1001 (gross 25000) — align total with actual payment
UPDATE purchase_orders SET total_amount = 25000.00 WHERE id = 'f091084b-70ba-4341-b09e-2997f0e7cdd4';

-- 3. Set missing total_amount on PO-1789062034056
UPDATE purchase_orders SET total_amount = 12000.00 WHERE id = '0fdc9978-0dc7-44be-995b-b7b41135ae88';

-- 4. Full re-sync of all PO payment_status values based on vendor payments
-- Logic: all active (non-failed) payments paid → paid; some paid → partial; none paid → pending
-- Only updates POs that have at least one vendor payment
UPDATE purchase_orders po
SET payment_status = CASE
  -- No active (non-failed) vendor payments at all
  WHEN (
    SELECT COUNT(*) FROM vendor_payments vp
    WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL AND vp.status != 'failed'
  ) = 0 THEN 'pending'
  -- All active payments are paid
  WHEN (
    SELECT COUNT(*) FROM vendor_payments vp
    WHERE vp.purchase_order_id = po.id AND vp.deleted_at IS NULL
      AND vp.status != 'failed' AND vp.status != 'paid'
  ) = 0 THEN 'paid'
  -- Some active payments are paid
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
