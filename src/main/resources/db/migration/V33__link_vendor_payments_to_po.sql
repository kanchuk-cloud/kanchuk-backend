-- V33: Link vendor_payments to purchase_orders
ALTER TABLE vendor_payments
  ADD COLUMN IF NOT EXISTS purchase_order_id UUID REFERENCES purchase_orders(id);

CREATE INDEX IF NOT EXISTS idx_vendor_payments_po ON vendor_payments(purchase_order_id);
