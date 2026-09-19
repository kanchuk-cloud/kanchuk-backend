-- Add columns that were missing from V18 on purchase_orders
ALTER TABLE purchase_orders
  ADD COLUMN IF NOT EXISTS payment_status  VARCHAR(20)  NOT NULL DEFAULT 'pending',
  ADD COLUMN IF NOT EXISTS total_items     INT          NOT NULL DEFAULT 0;
