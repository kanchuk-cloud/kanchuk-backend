ALTER TABLE product_images
  ADD COLUMN IF NOT EXISTS variant_id UUID REFERENCES product_variants(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_product_images_variant_id ON product_images(variant_id);
