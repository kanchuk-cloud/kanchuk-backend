-- ============================================================
-- Kanchuk — Seed Data (V3)
-- All test user passwords: Admin@123
-- UUID prefix: 5eed0000-0000-0000-XXXX-YYYYYYYYYYYY
-- ============================================================

-- ── Loyalty Tiers ────────────────────────────────────────────
INSERT INTO loyalty_tiers (id, name, min_points, discount_percent, is_active) VALUES
  ('5eed0000-0000-0000-0001-000000000001', 'Bronze',   0,    0.00,  true),
  ('5eed0000-0000-0000-0001-000000000002', 'Silver',   500,  2.50,  true),
  ('5eed0000-0000-0000-0001-000000000003', 'Gold',     1500, 5.00,  true),
  ('5eed0000-0000-0000-0001-000000000004', 'Platinum', 5000, 10.00, true);

-- ── Tax Categories ───────────────────────────────────────────
INSERT INTO tax_categories (id, name, hsn_code, is_active) VALUES
  ('5eed0000-0000-0000-0002-000000000001', 'Silk & Ethnic Wear',  '5007', true),
  ('5eed0000-0000-0000-0002-000000000002', 'Cotton Clothing',     '6105', true),
  ('5eed0000-0000-0000-0002-000000000003', 'Fashion Accessories', '6217', true);

-- ── Tax Rates ────────────────────────────────────────────────
INSERT INTO tax_rates (id, tax_category_id, rate, tax_type, state_code, is_active) VALUES
  ('5eed0000-0000-0000-0003-000000000001', '5eed0000-0000-0000-0002-000000000001', 12.00, 'GST',  NULL, true),
  ('5eed0000-0000-0000-0003-000000000002', '5eed0000-0000-0000-0002-000000000001',  6.00, 'CGST', NULL, true),
  ('5eed0000-0000-0000-0003-000000000003', '5eed0000-0000-0000-0002-000000000001',  6.00, 'SGST', 'MH', true),
  ('5eed0000-0000-0000-0003-000000000004', '5eed0000-0000-0000-0002-000000000002',  5.00, 'GST',  NULL, true),
  ('5eed0000-0000-0000-0003-000000000005', '5eed0000-0000-0000-0002-000000000003', 18.00, 'GST',  NULL, true);

-- ── Fabrics ──────────────────────────────────────────────────
INSERT INTO fabrics (id, name, fabric_family, sort_order, is_active) VALUES
  ('5eed0000-0000-0000-0004-000000000001', 'Banarasi Silk',   'Silk',      1,  true),
  ('5eed0000-0000-0000-0004-000000000002', 'Kanjivaram Silk', 'Silk',      2,  true),
  ('5eed0000-0000-0000-0004-000000000003', 'Chiffon',         'Synthetic', 3,  true),
  ('5eed0000-0000-0000-0004-000000000004', 'Georgette',       'Synthetic', 4,  true),
  ('5eed0000-0000-0000-0004-000000000005', 'Pure Cotton',     'Cotton',    5,  true),
  ('5eed0000-0000-0000-0004-000000000006', 'Velvet',          'Velvet',    6,  true),
  ('5eed0000-0000-0000-0004-000000000007', 'Organza',         'Synthetic', 7,  true),
  ('5eed0000-0000-0000-0004-000000000008', 'Chanderi',        'Blended',   8,  true),
  ('5eed0000-0000-0000-0004-000000000009', 'Crepe',           'Synthetic', 9,  true),
  ('5eed0000-0000-0000-0004-00000000000a', 'Linen',           'Natural',   10, true);

-- ── Categories ───────────────────────────────────────────────
INSERT INTO categories (id, name, slug, code, sort_order, is_active, published_at, meta_title, meta_description) VALUES
  ('5eed0000-0000-0000-0005-000000000001', 'Sarees',           'sarees',           'SAR', 1, true, now(), 'Shop Sarees Online | Kanchuk',        'Explore our exquisite collection of Indian sarees'),
  ('5eed0000-0000-0000-0005-000000000002', 'Lehengas',         'lehengas',         'LEH', 2, true, now(), 'Designer Lehengas | Kanchuk',          'Bridal and festive lehengas for every occasion'),
  ('5eed0000-0000-0000-0005-000000000003', 'Kurta Sets',       'kurta-sets',       'KRT', 3, true, now(), 'Kurta Sets for Women | Kanchuk',       'Elegant kurta sets for daily and festive wear'),
  ('5eed0000-0000-0000-0005-000000000004', 'Suits Unstitched', 'suits-unstitched', 'SUT', 4, true, now(), 'Unstitched Suits | Kanchuk',           'Premium unstitched suit fabrics'),
  ('5eed0000-0000-0000-0005-000000000005', 'Dupattas',         'dupattas',         'DUP', 5, true, now(), 'Designer Dupattas | Kanchuk',          'Handcrafted dupattas to complete your look');

-- ── Sub-Categories ───────────────────────────────────────────
INSERT INTO sub_categories (id, category_id, name, slug, code, sort_order, is_active, published_at) VALUES
  ('5eed0000-0000-0000-0006-000000000001', '5eed0000-0000-0000-0005-000000000001', 'Silk Sarees',          'silk-sarees',          'SLK', 1, true, now()),
  ('5eed0000-0000-0000-0006-000000000002', '5eed0000-0000-0000-0005-000000000001', 'Cotton Sarees',        'cotton-sarees',        'CTN', 2, true, now()),
  ('5eed0000-0000-0000-0006-000000000003', '5eed0000-0000-0000-0005-000000000001', 'Printed Sarees',       'printed-sarees',       'PRT', 3, true, now()),
  ('5eed0000-0000-0000-0006-000000000004', '5eed0000-0000-0000-0005-000000000002', 'Bridal Lehengas',      'bridal-lehengas',      'BRD', 1, true, now()),
  ('5eed0000-0000-0000-0006-000000000005', '5eed0000-0000-0000-0005-000000000002', 'Party Lehengas',       'party-lehengas',       'PTY', 2, true, now()),
  ('5eed0000-0000-0000-0006-000000000006', '5eed0000-0000-0000-0005-000000000003', 'Anarkali Sets',        'anarkali-sets',        'ANK', 1, true, now()),
  ('5eed0000-0000-0000-0006-000000000007', '5eed0000-0000-0000-0005-000000000003', 'Straight Kurtas',      'straight-kurtas',      'STR', 2, true, now()),
  ('5eed0000-0000-0000-0006-000000000008', '5eed0000-0000-0000-0005-000000000004', 'Embroidered Suits',    'embroidered-suits',    'EMB', 1, true, now()),
  ('5eed0000-0000-0000-0006-000000000009', '5eed0000-0000-0000-0005-000000000004', 'Printed Suits',        'printed-suits',        'PRS', 2, true, now()),
  ('5eed0000-0000-0000-0006-00000000000a', '5eed0000-0000-0000-0005-000000000005', 'Embroidered Dupattas', 'embroidered-dupattas', 'EMD', 1, true, now());

-- ── SKU Sequences (so admin-created products get correct next numbers) ──
INSERT INTO sku_sequences (category_id, sub_category_id, last_number) VALUES
  ('5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000001', 2),
  ('5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000002', 1),
  ('5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000003', 1),
  ('5eed0000-0000-0000-0005-000000000002', '5eed0000-0000-0000-0006-000000000004', 1),
  ('5eed0000-0000-0000-0005-000000000002', '5eed0000-0000-0000-0006-000000000005', 1),
  ('5eed0000-0000-0000-0005-000000000003', '5eed0000-0000-0000-0006-000000000006', 1),
  ('5eed0000-0000-0000-0005-000000000004', '5eed0000-0000-0000-0006-000000000008', 1);

-- ── Sellers ──────────────────────────────────────────────────
INSERT INTO sellers (id, name, email, phone, gstin, state_code, sort_order, is_active) VALUES
  ('5eed0000-0000-0000-0007-000000000001', 'Rajesh Textiles Pvt Ltd',  'rajesh@example.com',    '9876543210', '27AABCR1234M1Z5', 'MH', 1, true),
  ('5eed0000-0000-0000-0007-000000000002', 'Surat Silk House',         'suratsilk@example.com', '9876543211', '24AABCS5678M1Z3', 'GJ', 2, true),
  ('5eed0000-0000-0000-0007-000000000003', 'Lucknow Chikan Emporium',  'chikan@example.com',    '9876543212', '09AABCL9012M1Z1', 'UP', 3, true);

-- ── Designers ────────────────────────────────────────────────
INSERT INTO designers (id, name, slug, bio, sort_order, is_active, published_at) VALUES
  ('5eed0000-0000-0000-0008-000000000001', 'Anita Dongre',       'anita-dongre',    'Celebrated Indian fashion designer known for sustainable bridal and festive wear blending tradition with modern sensibility.',               1, true, now()),
  ('5eed0000-0000-0000-0008-000000000002', 'Sabyasachi Mukherjee','sabyasachi',     'India''s foremost luxury designer, renowned for his opulent, heritage-inspired bridal collections and signature aesthetic.',              2, true, now()),
  ('5eed0000-0000-0000-0008-000000000003', 'Tarun Tahiliani',    'tarun-tahiliani', 'Pioneer of luxury Indian fashion, known for his mastery of draping, handlooms, and contemporary Indian silhouettes.',                     3, true, now());

-- ── Occasions ────────────────────────────────────────────────
INSERT INTO occasions (id, name, slug, sort_order, is_active, published_at, meta_title) VALUES
  ('5eed0000-0000-0000-0009-000000000001', 'Wedding', 'wedding', 1, true, now(), 'Wedding Outfits | Kanchuk'),
  ('5eed0000-0000-0000-0009-000000000002', 'Festive', 'festive', 2, true, now(), 'Festive Wear | Kanchuk'),
  ('5eed0000-0000-0000-0009-000000000003', 'Casual',  'casual',  3, true, now(), 'Casual Ethnic Wear | Kanchuk'),
  ('5eed0000-0000-0000-0009-000000000004', 'Party',   'party',   4, true, now(), 'Party Wear | Kanchuk'),
  ('5eed0000-0000-0000-0009-000000000005', 'Office',  'office',  5, true, now(), 'Office Ethnic Wear | Kanchuk');

-- ── Banners ──────────────────────────────────────────────────
INSERT INTO banners (id, title, image_url, mobile_image_url, link_url, placement, sort_order, is_active, published_at, expires_at) VALUES
  ('5eed0000-0000-0000-000a-000000000001', 'New Wedding Collection 2026',   'https://picsum.photos/seed/ban1/1920/600', 'https://picsum.photos/seed/ban1m/768/400', '/products?occasion=wedding', 'hero', 1, true, now(), now() + INTERVAL '90 days'),
  ('5eed0000-0000-0000-000a-000000000002', 'Festive Sale — Up to 40% Off',  'https://picsum.photos/seed/ban2/1920/600', 'https://picsum.photos/seed/ban2m/768/400', '/products?discount=1',      'hero', 2, true, now(), now() + INTERVAL '30 days'),
  ('5eed0000-0000-0000-000a-000000000003', 'New Arrivals — Banarasi Silks', 'https://picsum.photos/seed/ban3/1920/600', 'https://picsum.photos/seed/ban3m/768/400', '/products?category=sarees', 'hero', 3, true, now(), now() + INTERVAL '60 days');

-- ── Offers ───────────────────────────────────────────────────
INSERT INTO offers (id, title, description, occasion_id, discount_percent, sort_order, is_active, published_at, expires_at) VALUES
  ('5eed0000-0000-0000-000b-000000000001', 'Wedding Season Special', 'Flat 20% off on all bridal lehengas and silk sarees', '5eed0000-0000-0000-0009-000000000001', 20, 1, true, now(), now() + INTERVAL '90 days'),
  ('5eed0000-0000-0000-000b-000000000002', 'Festive Bonanza',        'Up to 35% off on festive collection',                '5eed0000-0000-0000-0009-000000000002', 35, 2, true, now(), now() + INTERVAL '30 days'),
  ('5eed0000-0000-0000-000b-000000000003', 'Casual Comfort',         'Buy 2 kurta sets and get 15% off',                  '5eed0000-0000-0000-0009-000000000003', 15, 3, true, now(), now() + INTERVAL '60 days');

-- ── Promotions ───────────────────────────────────────────────
INSERT INTO promotions (id, name, coupon_code, discount_type, discount_value, min_order_amount, max_discount_amount, usage_limit, used_count, is_active, starts_at, expires_at) VALUES
  ('5eed0000-0000-0000-000c-000000000001', 'Welcome Discount',  'WELCOME15', 'percent',  15.00,  999.00,  500.00, 1000, 142, true, now() - INTERVAL '30 days', now() + INTERVAL '365 days'),
  ('5eed0000-0000-0000-000c-000000000002', 'Save 500 on 3000',  'SAVE500',   'flat',    500.00, 3000.00,  500.00,  500,  87, true, now() - INTERVAL '7 days',  now() + INTERVAL '30 days'),
  ('5eed0000-0000-0000-000c-000000000003', 'Festive 25% Off',   'FEST25',    'percent',  25.00, 2000.00, 1500.00,  200,  23, true, now() - INTERVAL '2 days',  now() + INTERVAL '15 days'),
  ('5eed0000-0000-0000-000c-000000000004', 'Free Shipping',     'FREESHIP',  'flat',     49.00,  499.00,   49.00, NULL,  56, true, now() - INTERVAL '60 days', now() + INTERVAL '90 days');

-- ── Gift Cards ───────────────────────────────────────────────
INSERT INTO gift_cards (id, code, initial_amount, remaining_amount, is_active, expires_at) VALUES
  ('5eed0000-0000-0000-000d-000000000001', 'GIFT-KOSH-0001', 500.00,  500.00,  true,  now() + INTERVAL '1 year'),
  ('5eed0000-0000-0000-000d-000000000002', 'GIFT-KOSH-0002', 1000.00, 750.00,  true,  now() + INTERVAL '1 year'),
  ('5eed0000-0000-0000-000d-000000000003', 'GIFT-KOSH-0003', 2000.00, 2000.00, true,  now() + INTERVAL '6 months'),
  ('5eed0000-0000-0000-000d-000000000004', 'GIFT-KOSH-0004', 500.00,  0.00,    false, now() - INTERVAL '1 day');

-- ── Shipping Methods ─────────────────────────────────────────
INSERT INTO shipping_methods (id, name, carrier, base_price, free_above, estimated_days, is_active, sort_order) VALUES
  ('5eed0000-0000-0000-000e-000000000001', 'Standard Delivery', 'Delhivery',  49.00,  999.00, 5, true, 1),
  ('5eed0000-0000-0000-000e-000000000002', 'Express Delivery',  'Blue Dart',  99.00, 1999.00, 2, true, 2),
  ('5eed0000-0000-0000-000e-000000000003', 'Same Day Delivery', 'Dunzo',     149.00, NULL,    0, true, 3);

-- ── Pincodes ─────────────────────────────────────────────────
INSERT INTO pincodes (pincode, city, state, estimated_days, hyperlocal, is_serviceable) VALUES
  ('400001', 'Mumbai',     'Maharashtra',  3, true,  true),
  ('110001', 'New Delhi',  'Delhi',        4, false, true),
  ('560001', 'Bengaluru',  'Karnataka',    4, false, true),
  ('600001', 'Chennai',    'Tamil Nadu',   5, false, true),
  ('700001', 'Kolkata',    'West Bengal',  5, false, true),
  ('500001', 'Hyderabad',  'Telangana',    4, false, true),
  ('380001', 'Ahmedabad',  'Gujarat',      3, false, true),
  ('302001', 'Jaipur',     'Rajasthan',    4, false, true),
  ('226001', 'Lucknow',    'Uttar Pradesh',5, false, true),
  ('411001', 'Pune',       'Maharashtra',  3, true,  true),
  ('160001', 'Chandigarh', 'Punjab',       5, false, true),
  ('682001', 'Kochi',      'Kerala',       6, false, true);

-- ── Stock Locations ──────────────────────────────────────────
INSERT INTO stock_locations (id, name, address, city, state, pincode, is_active) VALUES
  ('5eed0000-0000-0000-000f-000000000001', 'Mumbai Main Warehouse',  'Plot 14, MIDC Industrial Area, Andheri East', 'Mumbai',  'Maharashtra',  '400093', true),
  ('5eed0000-0000-0000-000f-000000000002', 'Delhi Distribution Hub', 'Sector 63, Noida Industrial Area',           'Noida',   'Uttar Pradesh','201301', true),
  ('5eed0000-0000-0000-000f-000000000003', 'Surat Textile Godown',   'Ring Road, Textile Market Area',             'Surat',   'Gujarat',      '395002', true);

-- ── Facets ───────────────────────────────────────────────────
INSERT INTO facets (id, name, code, filter_type, sort_order, is_active) VALUES
  ('5eed0000-0000-0000-0010-000000000001', 'Color',       'color',       'checkbox', 1, true),
  ('5eed0000-0000-0000-0010-000000000002', 'Size',        'size',        'checkbox', 2, true),
  ('5eed0000-0000-0000-0010-000000000003', 'Pattern',     'pattern',     'checkbox', 3, true),
  ('5eed0000-0000-0000-0010-000000000004', 'Occasion',    'occasion',    'checkbox', 4, true),
  ('5eed0000-0000-0000-0010-000000000005', 'Price Range', 'price-range', 'range',    5, true);

-- ── Facet Values ─────────────────────────────────────────────
INSERT INTO facet_values (id, facet_id, value, code, sort_order, is_active) VALUES
  ('5eed0000-0000-0000-0011-000000000001', '5eed0000-0000-0000-0010-000000000001', 'Red',     'RED', 1, true),
  ('5eed0000-0000-0000-0011-000000000002', '5eed0000-0000-0000-0010-000000000001', 'Blue',    'BLU', 2, true),
  ('5eed0000-0000-0000-0011-000000000003', '5eed0000-0000-0000-0010-000000000001', 'Green',   'GRN', 3, true),
  ('5eed0000-0000-0000-0011-000000000004', '5eed0000-0000-0000-0010-000000000001', 'Gold',    'GLD', 4, true),
  ('5eed0000-0000-0000-0011-000000000005', '5eed0000-0000-0000-0010-000000000002', 'XS',      'XS',  1, true),
  ('5eed0000-0000-0000-0011-000000000006', '5eed0000-0000-0000-0010-000000000002', 'S',       'S',   2, true),
  ('5eed0000-0000-0000-0011-000000000007', '5eed0000-0000-0000-0010-000000000002', 'M',       'M',   3, true),
  ('5eed0000-0000-0000-0011-000000000008', '5eed0000-0000-0000-0010-000000000002', 'L',       'L',   4, true),
  ('5eed0000-0000-0000-0011-000000000009', '5eed0000-0000-0000-0010-000000000002', 'XL',      'XL',  5, true),
  ('5eed0000-0000-0000-0011-00000000000a', '5eed0000-0000-0000-0010-000000000003', 'Floral',  'FLR', 1, true),
  ('5eed0000-0000-0000-0011-00000000000b', '5eed0000-0000-0000-0010-000000000003', 'Paisley', 'PAI', 2, true),
  ('5eed0000-0000-0000-0011-00000000000c', '5eed0000-0000-0000-0010-000000000003', 'Zari',    'ZAR', 3, true);

-- ── Size Charts ──────────────────────────────────────────────
INSERT INTO size_charts (id, category_id, name, unit, chart_data, is_active) VALUES
  ('5eed0000-0000-0000-0012-000000000001',
   '5eed0000-0000-0000-0005-000000000003',
   'Kurta Size Guide', 'inches',
   '{"sizes":[{"size":"XS","chest":32,"waist":26,"hips":34,"length":44},{"size":"S","chest":34,"waist":28,"hips":36,"length":45},{"size":"M","chest":36,"waist":30,"hips":38,"length":46},{"size":"L","chest":38,"waist":32,"hips":40,"length":47},{"size":"XL","chest":40,"waist":34,"hips":42,"length":48}]}',
   true),
  ('5eed0000-0000-0000-0012-000000000002',
   '5eed0000-0000-0000-0005-000000000002',
   'Lehenga Size Guide', 'inches',
   '{"sizes":[{"size":"XS","waist":24,"hips":34,"length":42},{"size":"S","waist":26,"hips":36,"length":42},{"size":"M","waist":28,"hips":38,"length":43},{"size":"L","waist":30,"hips":40,"length":43},{"size":"XL","waist":32,"hips":42,"length":44}]}',
   true),
  ('5eed0000-0000-0000-0012-000000000003',
   '5eed0000-0000-0000-0005-000000000004',
   'Suit Fabric Guide', 'meters',
   '{"pieces":[{"piece":"Top","quantity":"2.5 meters"},{"piece":"Bottom","quantity":"2.0 meters"},{"piece":"Dupatta","quantity":"2.25 meters"}]}',
   true);

-- ── Products ─────────────────────────────────────────────────
INSERT INTO products (id, sku, name, short_description, description, gender,
  category_id, sub_category_id, fabric_id, tax_category_id, designer_id,
  thumbnail_url, pattern, occasions, tags, care_instructions,
  is_handloom, is_designer, is_new, is_bestseller, is_trending,
  is_active, sort_order, published_at)
VALUES

('5eed0000-0000-0000-0013-000000000001',
 'SAR-SLK-0001', 'Royal Banarasi Silk Saree in Crimson',
 'Exquisite hand-woven Banarasi silk saree with intricate zari border',
 'Draped in centuries of tradition, this Royal Banarasi Silk Saree is a masterpiece of Indian craftsmanship. Hand-woven in Varanasi by master weavers, the rich crimson base is adorned with golden zari work depicting lotus motifs. Perfect for bridal ceremonies and grand celebrations.',
 'women',
 '5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000001',
 '5eed0000-0000-0000-0004-000000000001', '5eed0000-0000-0000-0002-000000000001',
 '5eed0000-0000-0000-0008-000000000002',
 'https://picsum.photos/seed/p1/800/1000',
 'Zari',
 '["Wedding","Festive","Party"]', '["Bestseller","Handloom","Bridal"]',
 '["Dry clean only","Store in muslin cloth","Avoid direct sunlight"]',
 true, true, true, true, false, true, 1, now()),

('5eed0000-0000-0000-0013-000000000002',
 'SAR-SLK-0002', 'Kanjivaram Silk Saree in Peacock Blue',
 'Traditional Kanjivaram silk with contrast border and pallu',
 'A timeless Kanjivaram silk saree in stunning peacock blue with a deep purple contrast border. The rich pallu features temple motifs woven in pure gold zari — a symbol of South Indian weaving heritage.',
 'women',
 '5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000001',
 '5eed0000-0000-0000-0004-000000000002', '5eed0000-0000-0000-0002-000000000001',
 NULL,
 'https://picsum.photos/seed/p2/800/1000',
 'Temple',
 '["Wedding","Festive"]', '["New Arrival","Handloom"]',
 '["Dry clean only","Store folded with tissue paper"]',
 true, false, true, false, false, true, 2, now()),

('5eed0000-0000-0000-0013-000000000003',
 'LEH-BRD-0001', 'Crimson Velvet Bridal Lehenga Set',
 'Hand-embroidered velvet bridal lehenga with heavy dupatta',
 'An opulent bridal lehenga in deep crimson velvet, featuring hand-embroidered zardozi work across the entire skirt. The matching blouse and dupatta are embellished with pearl and sequin details. This ensemble is a statement of royal grandeur.',
 'women',
 '5eed0000-0000-0000-0005-000000000002', '5eed0000-0000-0000-0006-000000000004',
 '5eed0000-0000-0000-0004-000000000006', '5eed0000-0000-0000-0002-000000000001',
 '5eed0000-0000-0000-0008-000000000001',
 'https://picsum.photos/seed/p3/800/1000',
 'Zardozi',
 '["Wedding","Party"]', '["Designer","Bridal","Bestseller"]',
 '["Dry clean only","Keep away from moisture","Store in garment bag"]',
 false, true, false, true, false, true, 1, now()),

('5eed0000-0000-0000-0013-000000000004',
 'KRT-ANK-0001', 'Floral Georgette Anarkali Kurta Set',
 'Elegant Anarkali with floral print, paired with palazzo and dupatta',
 'A graceful Anarkali kurta set in soft georgette fabric, featuring a delicate floral print in shades of blush and gold. The flowy Anarkali silhouette is paired with matching palazzo pants and a printed dupatta — perfect for festive and party occasions.',
 'women',
 '5eed0000-0000-0000-0005-000000000003', '5eed0000-0000-0000-0006-000000000006',
 '5eed0000-0000-0000-0004-000000000004', '5eed0000-0000-0000-0002-000000000002',
 NULL,
 'https://picsum.photos/seed/p4/800/1000',
 'Floral',
 '["Festive","Party","Casual"]', '["New Arrival","Trending"]',
 '["Hand wash in cold water","Do not wring","Dry in shade"]',
 false, false, true, false, true, true, 1, now()),

('5eed0000-0000-0000-0013-000000000005',
 'SUT-EMB-0001', 'Embroidered Chanderi Suit Fabric — Ivory & Gold',
 'Premium Chanderi suit fabric with hand-block print and thread embroidery',
 'Create your perfect outfit with this premium Chanderi suit fabric in ivory and gold. Features intricate hand-block print with thread embroidery on the top piece. Includes matching bottom fabric and a sheer embroidered dupatta.',
 'women',
 '5eed0000-0000-0000-0005-000000000004', '5eed0000-0000-0000-0006-000000000008',
 '5eed0000-0000-0000-0004-000000000008', '5eed0000-0000-0000-0002-000000000002',
 NULL,
 'https://picsum.photos/seed/p5/800/1000',
 'Block Print',
 '["Festive","Office","Casual"]', '["New Arrival"]',
 '["Dry clean recommended","Iron on low heat","Store in cool dry place"]',
 false, false, true, false, false, true, 1, now()),

('5eed0000-0000-0000-0013-000000000006',
 'SAR-PRT-0001', 'Digital Printed Chiffon Saree — Floral Paradise',
 'Lightweight chiffon saree with vibrant digital floral print',
 'A breezy digital printed chiffon saree with a stunning floral paradise print in jewel tones. The lightweight fabric drapes elegantly and is perfect for day events and office wear. Comes with a matching blouse piece.',
 'women',
 '5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000003',
 '5eed0000-0000-0000-0004-000000000003', '5eed0000-0000-0000-0002-000000000002',
 NULL,
 'https://picsum.photos/seed/p6/800/1000',
 'Floral',
 '["Office","Casual","Party"]', '["Trending","Office Wear"]',
 '["Gentle machine wash","Use mild detergent","Hang dry"]',
 false, false, false, false, true, true, 2, now()),

('5eed0000-0000-0000-0013-000000000007',
 'LEH-PTY-0001', 'Sequined Georgette Party Lehenga — Midnight Blue',
 'Glamorous sequined lehenga set for parties and sangeet',
 'Shimmer all night in this gorgeous midnight blue sequined georgette lehenga. The skirt features all-over sequin work that catches the light beautifully. Paired with a matching crop blouse and a chiffon dupatta with sequin border.',
 'women',
 '5eed0000-0000-0000-0005-000000000002', '5eed0000-0000-0000-0006-000000000005',
 '5eed0000-0000-0000-0004-000000000004', '5eed0000-0000-0000-0002-000000000001',
 NULL,
 'https://picsum.photos/seed/p7/800/1000',
 'Sequined',
 '["Party","Festive"]', '["Party Wear","Glamour"]',
 '["Dry clean only","Do not wring or twist"]',
 false, false, false, false, true, true, 2, now()),

('5eed0000-0000-0000-0013-000000000008',
 'SAR-CTN-0001', 'Hand-block Printed Cotton Saree — Indigo',
 'Comfortable everyday cotton saree with traditional hand-block print',
 'An everyday essential, this hand-block printed cotton saree in indigo and white is crafted by artisans from Rajasthan. The breathable cotton fabric and simple elegance make it perfect for daily wear and casual occasions.',
 'women',
 '5eed0000-0000-0000-0005-000000000001', '5eed0000-0000-0000-0006-000000000002',
 '5eed0000-0000-0000-0004-000000000005', '5eed0000-0000-0000-0002-000000000002',
 NULL,
 'https://picsum.photos/seed/p8/800/1000',
 'Block Print',
 '["Casual","Office"]', '["Handloom","Everyday Wear"]',
 '["Machine washable","Tumble dry low","Iron on medium heat"]',
 true, false, false, false, false, true, 3, now());

-- ── Product Images ────────────────────────────────────────────
INSERT INTO product_images (id, product_id, image_url, alt_text, sort_order) VALUES
  ('5eed0000-0000-0000-0014-000000000001', '5eed0000-0000-0000-0013-000000000001', 'https://picsum.photos/seed/p1a/800/1000', 'Royal Banarasi Silk Saree front view', 0),
  ('5eed0000-0000-0000-0014-000000000002', '5eed0000-0000-0000-0013-000000000001', 'https://picsum.photos/seed/p1b/800/1000', 'Royal Banarasi Silk Saree pallu detail', 1),
  ('5eed0000-0000-0000-0014-000000000003', '5eed0000-0000-0000-0013-000000000001', 'https://picsum.photos/seed/p1c/800/1000', 'Royal Banarasi Silk Saree border closeup', 2),
  ('5eed0000-0000-0000-0014-000000000004', '5eed0000-0000-0000-0013-000000000002', 'https://picsum.photos/seed/p2a/800/1000', 'Kanjivaram Silk Saree front view', 0),
  ('5eed0000-0000-0000-0014-000000000005', '5eed0000-0000-0000-0013-000000000002', 'https://picsum.photos/seed/p2b/800/1000', 'Kanjivaram Silk Saree pallu close-up', 1),
  ('5eed0000-0000-0000-0014-000000000006', '5eed0000-0000-0000-0013-000000000003', 'https://picsum.photos/seed/p3a/800/1000', 'Crimson Velvet Bridal Lehenga full set', 0),
  ('5eed0000-0000-0000-0014-000000000007', '5eed0000-0000-0000-0013-000000000003', 'https://picsum.photos/seed/p3b/800/1000', 'Bridal Lehenga blouse embroidery detail', 1),
  ('5eed0000-0000-0000-0014-000000000008', '5eed0000-0000-0000-0013-000000000004', 'https://picsum.photos/seed/p4a/800/1000', 'Anarkali Kurta Set front view', 0),
  ('5eed0000-0000-0000-0014-000000000009', '5eed0000-0000-0000-0013-000000000005', 'https://picsum.photos/seed/p5a/800/1000', 'Chanderi Suit Fabric full set', 0),
  ('5eed0000-0000-0000-0014-00000000000a', '5eed0000-0000-0000-0013-000000000006', 'https://picsum.photos/seed/p6a/800/1000', 'Printed Chiffon Saree draped view', 0),
  ('5eed0000-0000-0000-0014-00000000000b', '5eed0000-0000-0000-0013-000000000007', 'https://picsum.photos/seed/p7a/800/1000', 'Party Lehenga front view', 0),
  ('5eed0000-0000-0000-0014-00000000000c', '5eed0000-0000-0000-0013-000000000008', 'https://picsum.photos/seed/p8a/800/1000', 'Cotton Saree draped view', 0);

-- ── Product Option Types ──────────────────────────────────────
INSERT INTO product_option_types (id, product_id, name, display_name, sort_order) VALUES
  ('5eed0000-0000-0000-0015-000000000001', '5eed0000-0000-0000-0013-000000000001', 'color', 'Color', 0),
  ('5eed0000-0000-0000-0015-000000000002', '5eed0000-0000-0000-0013-000000000002', 'color', 'Color', 0),
  ('5eed0000-0000-0000-0015-000000000003', '5eed0000-0000-0000-0013-000000000003', 'size',  'Size',  0),
  ('5eed0000-0000-0000-0015-000000000004', '5eed0000-0000-0000-0013-000000000004', 'size',  'Size',  0),
  ('5eed0000-0000-0000-0015-000000000005', '5eed0000-0000-0000-0013-000000000005', 'size',  'Size',  0),
  ('5eed0000-0000-0000-0015-000000000006', '5eed0000-0000-0000-0013-000000000006', 'color', 'Color', 0),
  ('5eed0000-0000-0000-0015-000000000007', '5eed0000-0000-0000-0013-000000000007', 'size',  'Size',  0),
  ('5eed0000-0000-0000-0015-000000000008', '5eed0000-0000-0000-0013-000000000008', 'color', 'Color', 0);

-- ── Product Option Values ─────────────────────────────────────
INSERT INTO product_option_values (id, option_type_id, value, code, sort_order) VALUES
  -- P1 colors
  ('5eed0000-0000-0000-0016-000000000001', '5eed0000-0000-0000-0015-000000000001', 'Crimson',      'CRM', 0),
  ('5eed0000-0000-0000-0016-000000000002', '5eed0000-0000-0000-0015-000000000001', 'Royal Blue',   'RBL', 1),
  -- P2 colors
  ('5eed0000-0000-0000-0016-000000000003', '5eed0000-0000-0000-0015-000000000002', 'Peacock Blue', 'PCB', 0),
  ('5eed0000-0000-0000-0016-000000000004', '5eed0000-0000-0000-0015-000000000002', 'Emerald Green','EMG', 1),
  -- P3 sizes
  ('5eed0000-0000-0000-0016-000000000005', '5eed0000-0000-0000-0015-000000000003', 'XS', 'XS', 0),
  ('5eed0000-0000-0000-0016-000000000006', '5eed0000-0000-0000-0015-000000000003', 'S',  'S',  1),
  ('5eed0000-0000-0000-0016-000000000007', '5eed0000-0000-0000-0015-000000000003', 'M',  'M',  2),
  ('5eed0000-0000-0000-0016-000000000008', '5eed0000-0000-0000-0015-000000000003', 'L',  'L',  3),
  ('5eed0000-0000-0000-0016-000000000009', '5eed0000-0000-0000-0015-000000000003', 'XL', 'XL', 4),
  -- P4 sizes
  ('5eed0000-0000-0000-0016-00000000000a', '5eed0000-0000-0000-0015-000000000004', 'S',  'S',  0),
  ('5eed0000-0000-0000-0016-00000000000b', '5eed0000-0000-0000-0015-000000000004', 'M',  'M',  1),
  ('5eed0000-0000-0000-0016-00000000000c', '5eed0000-0000-0000-0015-000000000004', 'L',  'L',  2),
  ('5eed0000-0000-0000-0016-00000000000d', '5eed0000-0000-0000-0015-000000000004', 'XL', 'XL', 3),
  -- P5 sizes
  ('5eed0000-0000-0000-0016-00000000000e', '5eed0000-0000-0000-0015-000000000005', 'S',  'S',  0),
  ('5eed0000-0000-0000-0016-00000000000f', '5eed0000-0000-0000-0015-000000000005', 'M',  'M',  1),
  ('5eed0000-0000-0000-0016-000000000010', '5eed0000-0000-0000-0015-000000000005', 'L',  'L',  2),
  ('5eed0000-0000-0000-0016-000000000011', '5eed0000-0000-0000-0015-000000000005', 'XL', 'XL', 3),
  -- P6 colors
  ('5eed0000-0000-0000-0016-000000000012', '5eed0000-0000-0000-0015-000000000006', 'Multicolor', 'MLT', 0),
  ('5eed0000-0000-0000-0016-000000000013', '5eed0000-0000-0000-0015-000000000006', 'Pink & Gold', 'PKG', 1),
  -- P7 sizes
  ('5eed0000-0000-0000-0016-000000000014', '5eed0000-0000-0000-0015-000000000007', 'S',  'S',  0),
  ('5eed0000-0000-0000-0016-000000000015', '5eed0000-0000-0000-0015-000000000007', 'M',  'M',  1),
  ('5eed0000-0000-0000-0016-000000000016', '5eed0000-0000-0000-0015-000000000007', 'L',  'L',  2),
  -- P8 colors
  ('5eed0000-0000-0000-0016-000000000017', '5eed0000-0000-0000-0015-000000000008', 'Indigo',  'IND', 0),
  ('5eed0000-0000-0000-0016-000000000018', '5eed0000-0000-0000-0015-000000000008', 'Mustard', 'MST', 1);

-- ── Product Variants ──────────────────────────────────────────
INSERT INTO product_variants (id, product_id, sku, is_active) VALUES
  ('5eed0000-0000-0000-0017-000000000001', '5eed0000-0000-0000-0013-000000000001', 'SAR-SLK-0001-CRM', true),
  ('5eed0000-0000-0000-0017-000000000002', '5eed0000-0000-0000-0013-000000000001', 'SAR-SLK-0001-RBL', true),
  ('5eed0000-0000-0000-0017-000000000003', '5eed0000-0000-0000-0013-000000000002', 'SAR-SLK-0002-PCB', true),
  ('5eed0000-0000-0000-0017-000000000004', '5eed0000-0000-0000-0013-000000000002', 'SAR-SLK-0002-EMG', true),
  ('5eed0000-0000-0000-0017-000000000005', '5eed0000-0000-0000-0013-000000000003', 'LEH-BRD-0001-XS',  true),
  ('5eed0000-0000-0000-0017-000000000006', '5eed0000-0000-0000-0013-000000000003', 'LEH-BRD-0001-S',   true),
  ('5eed0000-0000-0000-0017-000000000007', '5eed0000-0000-0000-0013-000000000003', 'LEH-BRD-0001-M',   true),
  ('5eed0000-0000-0000-0017-000000000008', '5eed0000-0000-0000-0013-000000000003', 'LEH-BRD-0001-L',   true),
  ('5eed0000-0000-0000-0017-000000000009', '5eed0000-0000-0000-0013-000000000003', 'LEH-BRD-0001-XL',  true),
  ('5eed0000-0000-0000-0017-00000000000a', '5eed0000-0000-0000-0013-000000000004', 'KRT-ANK-0001-S',   true),
  ('5eed0000-0000-0000-0017-00000000000b', '5eed0000-0000-0000-0013-000000000004', 'KRT-ANK-0001-M',   true),
  ('5eed0000-0000-0000-0017-00000000000c', '5eed0000-0000-0000-0013-000000000004', 'KRT-ANK-0001-L',   true),
  ('5eed0000-0000-0000-0017-00000000000d', '5eed0000-0000-0000-0013-000000000004', 'KRT-ANK-0001-XL',  true),
  ('5eed0000-0000-0000-0017-00000000000e', '5eed0000-0000-0000-0013-000000000005', 'SUT-EMB-0001-S',   true),
  ('5eed0000-0000-0000-0017-00000000000f', '5eed0000-0000-0000-0013-000000000005', 'SUT-EMB-0001-M',   true),
  ('5eed0000-0000-0000-0017-000000000010', '5eed0000-0000-0000-0013-000000000005', 'SUT-EMB-0001-L',   true),
  ('5eed0000-0000-0000-0017-000000000011', '5eed0000-0000-0000-0013-000000000005', 'SUT-EMB-0001-XL',  true),
  ('5eed0000-0000-0000-0017-000000000012', '5eed0000-0000-0000-0013-000000000006', 'SAR-PRT-0001-MLT', true),
  ('5eed0000-0000-0000-0017-000000000013', '5eed0000-0000-0000-0013-000000000006', 'SAR-PRT-0001-PKG', true),
  ('5eed0000-0000-0000-0017-000000000014', '5eed0000-0000-0000-0013-000000000007', 'LEH-PTY-0001-S',   true),
  ('5eed0000-0000-0000-0017-000000000015', '5eed0000-0000-0000-0013-000000000007', 'LEH-PTY-0001-M',   true),
  ('5eed0000-0000-0000-0017-000000000016', '5eed0000-0000-0000-0013-000000000007', 'LEH-PTY-0001-L',   true),
  ('5eed0000-0000-0000-0017-000000000017', '5eed0000-0000-0000-0013-000000000008', 'SAR-CTN-0001-IND', true),
  ('5eed0000-0000-0000-0017-000000000018', '5eed0000-0000-0000-0013-000000000008', 'SAR-CTN-0001-MST', true);

-- ── Variant Option Values ─────────────────────────────────────
INSERT INTO variant_option_values (variant_id, option_value_id) VALUES
  ('5eed0000-0000-0000-0017-000000000001', '5eed0000-0000-0000-0016-000000000001'),
  ('5eed0000-0000-0000-0017-000000000002', '5eed0000-0000-0000-0016-000000000002'),
  ('5eed0000-0000-0000-0017-000000000003', '5eed0000-0000-0000-0016-000000000003'),
  ('5eed0000-0000-0000-0017-000000000004', '5eed0000-0000-0000-0016-000000000004'),
  ('5eed0000-0000-0000-0017-000000000005', '5eed0000-0000-0000-0016-000000000005'),
  ('5eed0000-0000-0000-0017-000000000006', '5eed0000-0000-0000-0016-000000000006'),
  ('5eed0000-0000-0000-0017-000000000007', '5eed0000-0000-0000-0016-000000000007'),
  ('5eed0000-0000-0000-0017-000000000008', '5eed0000-0000-0000-0016-000000000008'),
  ('5eed0000-0000-0000-0017-000000000009', '5eed0000-0000-0000-0016-000000000009'),
  ('5eed0000-0000-0000-0017-00000000000a', '5eed0000-0000-0000-0016-00000000000a'),
  ('5eed0000-0000-0000-0017-00000000000b', '5eed0000-0000-0000-0016-00000000000b'),
  ('5eed0000-0000-0000-0017-00000000000c', '5eed0000-0000-0000-0016-00000000000c'),
  ('5eed0000-0000-0000-0017-00000000000d', '5eed0000-0000-0000-0016-00000000000d'),
  ('5eed0000-0000-0000-0017-00000000000e', '5eed0000-0000-0000-0016-00000000000e'),
  ('5eed0000-0000-0000-0017-00000000000f', '5eed0000-0000-0000-0016-00000000000f'),
  ('5eed0000-0000-0000-0017-000000000010', '5eed0000-0000-0000-0016-000000000010'),
  ('5eed0000-0000-0000-0017-000000000011', '5eed0000-0000-0000-0016-000000000011'),
  ('5eed0000-0000-0000-0017-000000000012', '5eed0000-0000-0000-0016-000000000012'),
  ('5eed0000-0000-0000-0017-000000000013', '5eed0000-0000-0000-0016-000000000013'),
  ('5eed0000-0000-0000-0017-000000000014', '5eed0000-0000-0000-0016-000000000014'),
  ('5eed0000-0000-0000-0017-000000000015', '5eed0000-0000-0000-0016-000000000015'),
  ('5eed0000-0000-0000-0017-000000000016', '5eed0000-0000-0000-0016-000000000016'),
  ('5eed0000-0000-0000-0017-000000000017', '5eed0000-0000-0000-0016-000000000017'),
  ('5eed0000-0000-0000-0017-000000000018', '5eed0000-0000-0000-0016-000000000018');

-- ── Product Listings ──────────────────────────────────────────
INSERT INTO product_listings (id, variant_id, seller_id, price, compare_at_price, is_active) VALUES
  ('5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-0017-000000000001', '5eed0000-0000-0000-0007-000000000002', 18500.00, 24000.00, true),
  ('5eed0000-0000-0000-0018-000000000002', '5eed0000-0000-0000-0017-000000000002', '5eed0000-0000-0000-0007-000000000002', 18500.00, 24000.00, true),
  ('5eed0000-0000-0000-0018-000000000003', '5eed0000-0000-0000-0017-000000000003', '5eed0000-0000-0000-0007-000000000002', 22000.00, 28000.00, true),
  ('5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-0017-000000000004', '5eed0000-0000-0000-0007-000000000002', 22000.00, 28000.00, true),
  ('5eed0000-0000-0000-0018-000000000005', '5eed0000-0000-0000-0017-000000000005', '5eed0000-0000-0000-0007-000000000001', 85000.00,110000.00, true),
  ('5eed0000-0000-0000-0018-000000000006', '5eed0000-0000-0000-0017-000000000006', '5eed0000-0000-0000-0007-000000000001', 85000.00,110000.00, true),
  ('5eed0000-0000-0000-0018-000000000007', '5eed0000-0000-0000-0017-000000000007', '5eed0000-0000-0000-0007-000000000001', 85000.00,110000.00, true),
  ('5eed0000-0000-0000-0018-000000000008', '5eed0000-0000-0000-0017-000000000008', '5eed0000-0000-0000-0007-000000000001', 85000.00,110000.00, true),
  ('5eed0000-0000-0000-0018-000000000009', '5eed0000-0000-0000-0017-000000000009', '5eed0000-0000-0000-0007-000000000001', 85000.00,110000.00, true),
  ('5eed0000-0000-0000-0018-00000000000a', '5eed0000-0000-0000-0017-00000000000a', '5eed0000-0000-0000-0007-000000000003',  4500.00,  6000.00, true),
  ('5eed0000-0000-0000-0018-00000000000b', '5eed0000-0000-0000-0017-00000000000b', '5eed0000-0000-0000-0007-000000000003',  4500.00,  6000.00, true),
  ('5eed0000-0000-0000-0018-00000000000c', '5eed0000-0000-0000-0017-00000000000c', '5eed0000-0000-0000-0007-000000000003',  4500.00,  6000.00, true),
  ('5eed0000-0000-0000-0018-00000000000d', '5eed0000-0000-0000-0017-00000000000d', '5eed0000-0000-0000-0007-000000000003',  4500.00,  6000.00, true),
  ('5eed0000-0000-0000-0018-00000000000e', '5eed0000-0000-0000-0017-00000000000e', '5eed0000-0000-0000-0007-000000000002',  3200.00,  4000.00, true),
  ('5eed0000-0000-0000-0018-00000000000f', '5eed0000-0000-0000-0017-00000000000f', '5eed0000-0000-0000-0007-000000000002',  3200.00,  4000.00, true),
  ('5eed0000-0000-0000-0018-000000000010', '5eed0000-0000-0000-0017-000000000010', '5eed0000-0000-0000-0007-000000000002',  3200.00,  4000.00, true),
  ('5eed0000-0000-0000-0018-000000000011', '5eed0000-0000-0000-0017-000000000011', '5eed0000-0000-0000-0007-000000000002',  3200.00,  4000.00, true),
  ('5eed0000-0000-0000-0018-000000000012', '5eed0000-0000-0000-0017-000000000012', '5eed0000-0000-0000-0007-000000000002',  1850.00,  2500.00, true),
  ('5eed0000-0000-0000-0018-000000000013', '5eed0000-0000-0000-0017-000000000013', '5eed0000-0000-0000-0007-000000000002',  1850.00,  2500.00, true),
  ('5eed0000-0000-0000-0018-000000000014', '5eed0000-0000-0000-0017-000000000014', '5eed0000-0000-0000-0007-000000000001', 12000.00, 16000.00, true),
  ('5eed0000-0000-0000-0018-000000000015', '5eed0000-0000-0000-0017-000000000015', '5eed0000-0000-0000-0007-000000000001', 12000.00, 16000.00, true),
  ('5eed0000-0000-0000-0018-000000000016', '5eed0000-0000-0000-0017-000000000016', '5eed0000-0000-0000-0007-000000000001', 12000.00, 16000.00, true),
  ('5eed0000-0000-0000-0018-000000000017', '5eed0000-0000-0000-0017-000000000017', '5eed0000-0000-0000-0007-000000000003',  1200.00,  1600.00, true),
  ('5eed0000-0000-0000-0018-000000000018', '5eed0000-0000-0000-0017-000000000018', '5eed0000-0000-0000-0007-000000000003',  1200.00,  1600.00, true);

-- ── Inventory Levels ──────────────────────────────────────────
INSERT INTO inventory_levels (id, listing_id, location_id, quantity_on_hand, quantity_reserved) VALUES
  ('5eed0000-0000-0000-0019-000000000001', '5eed0000-0000-0000-0018-000000000001', '5eed0000-0000-0000-000f-000000000001', 15, 2),
  ('5eed0000-0000-0000-0019-000000000002', '5eed0000-0000-0000-0018-000000000002', '5eed0000-0000-0000-000f-000000000001', 12, 1),
  ('5eed0000-0000-0000-0019-000000000003', '5eed0000-0000-0000-0018-000000000003', '5eed0000-0000-0000-000f-000000000001',  8, 1),
  ('5eed0000-0000-0000-0019-000000000004', '5eed0000-0000-0000-0018-000000000004', '5eed0000-0000-0000-000f-000000000001',  6, 0),
  ('5eed0000-0000-0000-0019-000000000005', '5eed0000-0000-0000-0018-000000000005', '5eed0000-0000-0000-000f-000000000001',  3, 1),
  ('5eed0000-0000-0000-0019-000000000006', '5eed0000-0000-0000-0018-000000000006', '5eed0000-0000-0000-000f-000000000001',  5, 0),
  ('5eed0000-0000-0000-0019-000000000007', '5eed0000-0000-0000-0018-000000000007', '5eed0000-0000-0000-000f-000000000001',  7, 2),
  ('5eed0000-0000-0000-0019-000000000008', '5eed0000-0000-0000-0018-000000000008', '5eed0000-0000-0000-000f-000000000001',  4, 0),
  ('5eed0000-0000-0000-0019-000000000009', '5eed0000-0000-0000-0018-000000000009', '5eed0000-0000-0000-000f-000000000001',  2, 0),
  ('5eed0000-0000-0000-0019-00000000000a', '5eed0000-0000-0000-0018-00000000000a', '5eed0000-0000-0000-000f-000000000002', 20, 3),
  ('5eed0000-0000-0000-0019-00000000000b', '5eed0000-0000-0000-0018-00000000000b', '5eed0000-0000-0000-000f-000000000002', 25, 5),
  ('5eed0000-0000-0000-0019-00000000000c', '5eed0000-0000-0000-0018-00000000000c', '5eed0000-0000-0000-000f-000000000002', 18, 2),
  ('5eed0000-0000-0000-0019-00000000000d', '5eed0000-0000-0000-0018-00000000000d', '5eed0000-0000-0000-000f-000000000002', 10, 1),
  ('5eed0000-0000-0000-0019-00000000000e', '5eed0000-0000-0000-0018-00000000000e', '5eed0000-0000-0000-000f-000000000003', 30, 0),
  ('5eed0000-0000-0000-0019-00000000000f', '5eed0000-0000-0000-0018-00000000000f', '5eed0000-0000-0000-000f-000000000003', 35, 2),
  ('5eed0000-0000-0000-0019-000000000010', '5eed0000-0000-0000-0018-000000000010', '5eed0000-0000-0000-000f-000000000003', 28, 1),
  ('5eed0000-0000-0000-0019-000000000011', '5eed0000-0000-0000-0018-000000000011', '5eed0000-0000-0000-000f-000000000003', 15, 0),
  ('5eed0000-0000-0000-0019-000000000012', '5eed0000-0000-0000-0018-000000000012', '5eed0000-0000-0000-000f-000000000002', 22, 4),
  ('5eed0000-0000-0000-0019-000000000013', '5eed0000-0000-0000-0018-000000000013', '5eed0000-0000-0000-000f-000000000002', 18, 2),
  ('5eed0000-0000-0000-0019-000000000014', '5eed0000-0000-0000-0018-000000000014', '5eed0000-0000-0000-000f-000000000001', 10, 1),
  ('5eed0000-0000-0000-0019-000000000015', '5eed0000-0000-0000-0018-000000000015', '5eed0000-0000-0000-000f-000000000001', 12, 2),
  ('5eed0000-0000-0000-0019-000000000016', '5eed0000-0000-0000-0018-000000000016', '5eed0000-0000-0000-000f-000000000001',  8, 0),
  ('5eed0000-0000-0000-0019-000000000017', '5eed0000-0000-0000-0018-000000000017', '5eed0000-0000-0000-000f-000000000003', 50, 3),
  ('5eed0000-0000-0000-0019-000000000018', '5eed0000-0000-0000-0018-000000000018', '5eed0000-0000-0000-000f-000000000003', 40, 1);

-- ── Product Facet Values ──────────────────────────────────────
INSERT INTO product_facet_values (product_id, facet_value_id) VALUES
  -- P1: Crimson color, Zari pattern
  ('5eed0000-0000-0000-0013-000000000001', '5eed0000-0000-0000-0011-000000000001'),
  ('5eed0000-0000-0000-0013-000000000001', '5eed0000-0000-0000-0011-00000000000c'),
  -- P2: Blue color
  ('5eed0000-0000-0000-0013-000000000002', '5eed0000-0000-0000-0011-000000000002'),
  -- P3: Red color (Crimson lehenga)
  ('5eed0000-0000-0000-0013-000000000003', '5eed0000-0000-0000-0011-000000000001'),
  -- P4: Floral pattern
  ('5eed0000-0000-0000-0013-000000000004', '5eed0000-0000-0000-0011-00000000000a'),
  -- P6: Floral pattern
  ('5eed0000-0000-0000-0013-000000000006', '5eed0000-0000-0000-0011-00000000000a'),
  -- P8: Paisley/Block print → Paisley facet
  ('5eed0000-0000-0000-0013-000000000008', '5eed0000-0000-0000-0011-00000000000b');

-- ── Users ─────────────────────────────────────────────────────
-- All passwords: Admin@123 (same bcrypt hash as admin seed)
INSERT INTO users (id, name, email, phone, password_hash, wallet_balance, loyalty_points, loyalty_tier_id, is_active) VALUES
  ('5eed0000-0000-0000-001a-000000000001', 'Priya Sharma',  'priya.sharma@example.com',  '9876543001', '$2a$12$TBNbXXEVlnEGGz/jkXOOVeSz9TiUME4sW5Xp10yHJ3X6oSTe5Y6.e',  250.00,  850, '5eed0000-0000-0000-0001-000000000002', true),
  ('5eed0000-0000-0000-001a-000000000002', 'Anjali Mehta',  'anjali.mehta@example.com',  '9876543002', '$2a$12$TBNbXXEVlnEGGz/jkXOOVeSz9TiUME4sW5Xp10yHJ3X6oSTe5Y6.e',    0.00,  150, '5eed0000-0000-0000-0001-000000000001', true),
  ('5eed0000-0000-0000-001a-000000000003', 'Deepika Reddy', 'deepika.reddy@example.com', '9876543003', '$2a$12$TBNbXXEVlnEGGz/jkXOOVeSz9TiUME4sW5Xp10yHJ3X6oSTe5Y6.e', 1000.00, 2200, '5eed0000-0000-0000-0001-000000000003', true);

-- ── Addresses ─────────────────────────────────────────────────
INSERT INTO addresses (id, user_id, type, name, phone, line1, line2, city, state, pincode, is_default, is_active) VALUES
  ('5eed0000-0000-0000-001b-000000000001', '5eed0000-0000-0000-001a-000000000001', 'home', 'Priya Sharma',  '9876543001', 'Flat 302, Shree Apartments, Bandra West',       NULL,        'Mumbai',    'Maharashtra',   '400050', true,  true),
  ('5eed0000-0000-0000-001b-000000000002', '5eed0000-0000-0000-001a-000000000002', 'home', 'Anjali Mehta',  '9876543002', 'House No. 14, Sector 22, Dwarka',               NULL,        'New Delhi', 'Delhi',         '110077', true,  true),
  ('5eed0000-0000-0000-001b-000000000003', '5eed0000-0000-0000-001a-000000000003', 'home', 'Deepika Reddy', '9876543003', 'Flat 12B, Prestige Towers, Koramangala',        '6th Block', 'Bengaluru', 'Karnataka',     '560095', true,  true),
  ('5eed0000-0000-0000-001b-000000000004', '5eed0000-0000-0000-001a-000000000003', 'work', 'Deepika Reddy', '9876543003', '3rd Floor, Prestige Tech Park, Marathahalli',   NULL,        'Bengaluru', 'Karnataka',     '560037', false, true);

-- ── Orders ────────────────────────────────────────────────────
INSERT INTO orders (id, order_number, user_id, status, subtotal, discount_amount, delivery_charge, total,
  promotion_id, shipping_method_id, delivery_address_snapshot, placed_at) VALUES

('5eed0000-0000-0000-001c-000000000001',
 'KCH-2026-000001', '5eed0000-0000-0000-001a-000000000001',
 'delivered', 18500.00, 0.00, 0.00, 18500.00,
 NULL, '5eed0000-0000-0000-000e-000000000001',
 '{"name":"Priya Sharma","phone":"9876543001","line1":"Flat 302, Shree Apartments, Bandra West","city":"Mumbai","state":"Maharashtra","pincode":"400050"}',
 now() - INTERVAL '15 days'),

('5eed0000-0000-0000-001c-000000000002',
 'KCH-2026-000002', '5eed0000-0000-0000-001a-000000000002',
 'processing', 4500.00, 675.00, 0.00, 3825.00,
 '5eed0000-0000-0000-000c-000000000001', '5eed0000-0000-0000-000e-000000000002',
 '{"name":"Anjali Mehta","phone":"9876543002","line1":"House No. 14, Sector 22, Dwarka","city":"New Delhi","state":"Delhi","pincode":"110077"}',
 now() - INTERVAL '2 days'),

('5eed0000-0000-0000-001c-000000000003',
 'KCH-2026-000003', '5eed0000-0000-0000-001a-000000000003',
 'pending', 85000.00, 21250.00, 0.00, 63750.00,
 '5eed0000-0000-0000-000c-000000000003', '5eed0000-0000-0000-000e-000000000001',
 '{"name":"Deepika Reddy","phone":"9876543003","line1":"Flat 12B, Prestige Towers, Koramangala","line2":"6th Block","city":"Bengaluru","state":"Karnataka","pincode":"560095"}',
 now() - INTERVAL '1 hour');

-- ── Order Items ───────────────────────────────────────────────
INSERT INTO order_items (id, order_id, listing_id, price, quantity, product_snapshot) VALUES
  ('5eed0000-0000-0000-001d-000000000001',
   '5eed0000-0000-0000-001c-000000000001',
   '5eed0000-0000-0000-0018-000000000001',
   18500.00, 1,
   '{"name":"Royal Banarasi Silk Saree in Crimson","sku":"SAR-SLK-0001-CRM","color":"Crimson"}'),

  ('5eed0000-0000-0000-001d-000000000002',
   '5eed0000-0000-0000-001c-000000000002',
   '5eed0000-0000-0000-0018-00000000000b',
   4500.00, 1,
   '{"name":"Floral Georgette Anarkali Kurta Set","sku":"KRT-ANK-0001-M","size":"M"}'),

  ('5eed0000-0000-0000-001d-000000000003',
   '5eed0000-0000-0000-001c-000000000003',
   '5eed0000-0000-0000-0018-000000000007',
   85000.00, 1,
   '{"name":"Crimson Velvet Bridal Lehenga Set","sku":"LEH-BRD-0001-M","size":"M"}');

-- ── Packaging ─────────────────────────────────────────────────
INSERT INTO packaging (id, order_id, packed_by, package_type, weight_grams, dimensions_cm, status, packed_at) VALUES
  ('5eed0000-0000-0000-001e-000000000001',
   '5eed0000-0000-0000-001c-000000000001',
   'Warehouse Team A', 'premium_box', 800,
   '{"length":45,"width":35,"height":10}',
   'packed', now() - INTERVAL '13 days'),

  ('5eed0000-0000-0000-001e-000000000002',
   '5eed0000-0000-0000-001c-000000000002',
   'Warehouse Team B', 'standard_poly_bag', 400,
   '{"length":40,"width":30,"height":5}',
   'in_progress', NULL);

-- ── Fulfillments ──────────────────────────────────────────────
INSERT INTO fulfillments (id, order_id, status, tracking_number, carrier, shipped_at, delivered_at) VALUES
  ('5eed0000-0000-0000-001f-000000000001',
   '5eed0000-0000-0000-001c-000000000001',
   'delivered', 'DL20260128567890', 'Delhivery',
   now() - INTERVAL '13 days', now() - INTERVAL '10 days');

-- ── Dispatching ───────────────────────────────────────────────
INSERT INTO dispatching (id, order_id, packaging_id, fulfillment_id, dispatched_by, carrier, awb_number, dispatch_type, status, dispatched_at) VALUES
  ('5eed0000-0000-0000-0020-000000000001',
   '5eed0000-0000-0000-001c-000000000001',
   '5eed0000-0000-0000-001e-000000000001',
   '5eed0000-0000-0000-001f-000000000001',
   'Rahul Kumar', 'Delhivery', 'DL20260128567890',
   'courier_pickup', 'dispatched', now() - INTERVAL '13 days');

-- ── Purchase Orders ───────────────────────────────────────────
INSERT INTO purchase_orders (id, po_number, seller_id, status, total_amount, expected_at, notes) VALUES
  ('5eed0000-0000-0000-0021-000000000001',
   'PO-2026-0001', '5eed0000-0000-0000-0007-000000000002',
   'confirmed', 185000.00, now() + INTERVAL '7 days',
   'Restock Banarasi and Kanjivaram silk sarees for wedding season'),

  ('5eed0000-0000-0000-0021-000000000002',
   'PO-2026-0002', '5eed0000-0000-0000-0007-000000000003',
   'received', 42000.00, now() - INTERVAL '5 days',
   'Chikan work kurta sets and Chanderi suit fabrics'),

  ('5eed0000-0000-0000-0021-000000000003',
   'PO-2026-0003', '5eed0000-0000-0000-0007-000000000001',
   'draft', 320000.00, now() + INTERVAL '21 days',
   'Bridal lehenga collection for upcoming festive season');

-- ── Blouse Recommendations ────────────────────────────────────
INSERT INTO blouse_recommendations
  (id, applies_fabric, applies_pattern, applies_occasion,
   style_name, neckline, sleeve, embellishment, fabric_suggestion, reason_text, tag, sort_order, is_active)
VALUES
  ('5eed0000-0000-0000-0022-000000000001',
   'Banarasi Silk', NULL, 'Wedding',
   'Royal Zari Classic', 'Sweetheart', 'Elbow Length', 'Zari Embroidery', 'Raw Silk',
   'A sweetheart neckline complements the grandeur of Banarasi silk, while elbow-length sleeves with zari embroidery mirror the saree''s intricate weave.',
   'AI Top Pick', 0, true),

  ('5eed0000-0000-0000-0022-000000000002',
   NULL, NULL, 'Wedding',
   'Bridal Kalamkari Blouse', 'Boat Neck', 'Full Sleeve', 'Kalamkari Print', 'Chanderi',
   'A boat neck blouse with Kalamkari print creates a stunning contrast for bridal sarees, adding artistic flair.',
   'Trending', 1, true),

  ('5eed0000-0000-0000-0022-000000000003',
   'Kanjivaram Silk', NULL, NULL,
   'Temple Border Back Blouse', 'Deep V Back', 'Cap Sleeve', 'Temple Border Motifs', 'Contrast Silk',
   'A deep V-back blouse in contrast color picks up the Kanjivaram''s pallu hues, creating a dramatic traditional look.',
   NULL, 2, true),

  ('5eed0000-0000-0000-0022-000000000004',
   NULL, 'Floral', 'Casual',
   'Puff Sleeve Trendy Blouse', 'Square Neck', 'Puff Sleeve', 'Floral Applique', 'Cotton',
   'Square neck with puff sleeves adds a contemporary edge to a floral saree, perfect for day events and casual outings.',
   NULL, 3, true),

  ('5eed0000-0000-0000-0022-000000000005',
   NULL, NULL, 'Festive',
   'Mirror Work Festive Blouse', 'Round Neck', 'Three Quarter', 'Mirror Work', 'Velvet',
   'Mirror work on a velvet blouse catches festive light beautifully, adding sparkle to any silk saree.',
   'Trending', 4, true);

-- ── Suit Styling Recommendations ─────────────────────────────
INSERT INTO suit_styling_recommendations
  (id, applies_fabric, applies_pattern, applies_occasion,
   style_name, bottom_style, kurta_style, dupatta_style, reason_text, tag, sort_order, is_active)
VALUES
  ('5eed0000-0000-0000-0023-000000000001',
   'Georgette', 'Floral', 'Festive',
   'Festive Palazzo Look', 'Palazzo', 'Anarkali', 'Contrast',
   'Flowing Anarkali paired with wide-leg palazzo creates an effortlessly chic festive look. A contrast dupatta adds visual interest without overwhelming the floral print.',
   'AI Top Pick', 0, true),

  ('5eed0000-0000-0000-0023-000000000002',
   'Chanderi', NULL, 'Office',
   'Office Elegant Straight Set', 'Churidar', 'Straight Cut', 'No Dupatta',
   'A straight-cut kurta with churidar is the quintessential power dressing combination — neat, polished, and comfortable through a long workday.',
   NULL, 1, true),

  ('5eed0000-0000-0000-0023-000000000003',
   NULL, NULL, 'Wedding',
   'Bridal Anarkali Statement', 'Sharara', 'Anarkali', 'Embroidered',
   'A heavily embroidered Anarkali over a sharara creates a royal bridal silhouette. The embroidered dupatta ties the look together for a cohesive, grand ensemble.',
   'AI Top Pick', 2, true),

  ('5eed0000-0000-0000-0023-000000000004',
   NULL, 'Block Print', 'Casual',
   'Casual Patiala Combo', 'Patiala', 'Straight Cut', 'Printed',
   'Block print suits pair beautifully with Patiala salwar for a relaxed, ethnic-casual look. A printed dupatta that picks up accent colors keeps the outfit lively.',
   'Trending', 3, true),

  ('5eed0000-0000-0000-0023-000000000005',
   'Velvet', NULL, 'Party',
   'Velvet Peplum Party Look', 'Dhoti Pants', 'Peplum', 'With Dupatta',
   'Velvet peplum kurta over dhoti pants is a modern fusion statement for parties. The structured peplum silhouette flatters all body types while the velvet adds luxury.',
   'Trending', 4, true);
