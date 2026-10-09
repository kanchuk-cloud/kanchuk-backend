-- Delink payments whose amounts don't fit their linked POs
-- PAY1016: 320000 gross linked to PO-2026-0003 (which already has PAY1010 paid 125000)
-- PAY1015: 145000 gross linked to PO-2026-0002 (total only 42000)
UPDATE vendor_payments SET purchase_order_id = NULL WHERE id IN ('PAY1015', 'PAY1016');

-- Re-sync payment_status on all POs that have at least one vendor payment
-- Logic:
--   no active (non-failed) payments → pending
--   all active payments are paid   → paid
--   some active payments are paid  → partial
--   no paid payments               → pending
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
