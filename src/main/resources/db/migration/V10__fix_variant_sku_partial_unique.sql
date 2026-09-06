-- Replace the full unique constraint on product_variants.sku with a partial
-- unique index that only covers non-deleted rows. This allows re-creating a
-- variant after it has been soft-deleted (deleted_at IS NOT NULL rows are ignored).

ALTER TABLE product_variants DROP CONSTRAINT IF EXISTS product_variants_sku_key;

CREATE UNIQUE INDEX IF NOT EXISTS product_variants_sku_active_uq
    ON product_variants (sku)
    WHERE deleted_at IS NULL;
