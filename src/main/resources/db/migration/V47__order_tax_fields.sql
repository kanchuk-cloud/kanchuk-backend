-- GST tax breakdown on order items
ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS taxable_value  NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS gst_rate       NUMERIC(5,4),
    ADD COLUMN IF NOT EXISTS tax_amount     NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS cgst           NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS sgst           NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS igst           NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS is_inter_state BOOLEAN NOT NULL DEFAULT FALSE;

-- GST summary on orders
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS subtotal_taxable NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS total_cgst       NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS total_sgst       NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS total_igst       NUMERIC(12,2),
    ADD COLUMN IF NOT EXISTS total_tax        NUMERIC(12,2);

-- State codes for GST intra/inter-state determination
ALTER TABLE stock_locations
    ADD COLUMN IF NOT EXISTS state_code CHAR(2);

ALTER TABLE pincodes
    ADD COLUMN IF NOT EXISTS state_code CHAR(2);
