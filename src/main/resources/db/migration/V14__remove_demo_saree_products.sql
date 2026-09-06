-- Remove the two demo saree products inserted by V3__seed_data.sql:
--   SAR-PRT-0001  Digital Printed Chiffon Saree — Floral Paradise
--   SAR-CTN-0001  Hand-block Printed Cotton Saree — Indigo
-- Delete leaf tables first to satisfy FK constraints.

-- 1. Inventory levels
DELETE FROM inventory_levels WHERE id IN (
    '5eed0000-0000-0000-0019-000000000012',
    '5eed0000-0000-0000-0019-000000000013',
    '5eed0000-0000-0000-0019-000000000017',
    '5eed0000-0000-0000-0019-000000000018'
);

-- 2. Product listings (soft-delete column present but hard-delete is fine for seed cleanup)
DELETE FROM product_listings WHERE id IN (
    '5eed0000-0000-0000-0018-000000000012',
    '5eed0000-0000-0000-0018-000000000013',
    '5eed0000-0000-0000-0018-000000000017',
    '5eed0000-0000-0000-0018-000000000018'
);

-- 3. Variant ↔ option-value join rows (ON DELETE CASCADE would handle this,
--    but being explicit avoids relying on cascade order)
DELETE FROM variant_option_values WHERE variant_id IN (
    '5eed0000-0000-0000-0017-000000000012',
    '5eed0000-0000-0000-0017-000000000013',
    '5eed0000-0000-0000-0017-000000000017',
    '5eed0000-0000-0000-0017-000000000018'
);

-- 4. Product variants
DELETE FROM product_variants WHERE id IN (
    '5eed0000-0000-0000-0017-000000000012',
    '5eed0000-0000-0000-0017-000000000013',
    '5eed0000-0000-0000-0017-000000000017',
    '5eed0000-0000-0000-0017-000000000018'
);

-- 5. Option values (option_types ON DELETE CASCADE covers this too, but explicit is safer)
DELETE FROM product_option_values WHERE id IN (
    '5eed0000-0000-0000-0016-000000000012',
    '5eed0000-0000-0000-0016-000000000013',
    '5eed0000-0000-0000-0016-000000000017',
    '5eed0000-0000-0000-0016-000000000018'
);

-- 6. Option types
DELETE FROM product_option_types WHERE id IN (
    '5eed0000-0000-0000-0015-000000000006',
    '5eed0000-0000-0000-0015-000000000008'
);

-- 7. Product images
DELETE FROM product_images WHERE id IN (
    '5eed0000-0000-0000-0014-00000000000a',
    '5eed0000-0000-0000-0014-00000000000c'
);

-- 8. Facet-value links
DELETE FROM product_facet_values WHERE product_id IN (
    '5eed0000-0000-0000-0013-000000000006',
    '5eed0000-0000-0000-0013-000000000008'
);

-- 9. Products
DELETE FROM products WHERE id IN (
    '5eed0000-0000-0000-0013-000000000006',
    '5eed0000-0000-0000-0013-000000000008'
);
