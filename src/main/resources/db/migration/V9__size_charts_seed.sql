-- ── Size Charts Seed Data ────────────────────────────────────────────────────
-- Category IDs from V3__seed_data.sql:
--   Sarees           5eed0000-0000-0000-0005-000000000001
--   Lehengas         5eed0000-0000-0000-0005-000000000002
--   Kurta Sets       5eed0000-0000-0000-0005-000000000003
--   Suits Unstitched 5eed0000-0000-0000-0005-000000000004
--   Dupattas         5eed0000-0000-0000-0005-000000000005
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO size_charts (id, category_id, name, unit, chart_data, is_active, created_at, updated_at) VALUES

-- ── SAREES ───────────────────────────────────────────────────────────────────
-- Sarees are free-size drapes; sizing applies to the stitched blouse piece.

('a100c000-0000-0000-0000-000000000001',
 '5eed0000-0000-0000-0005-000000000001',
 'Blouse Size Chart',
 'inches',
 '{
   "note": "Measurements are for the stitched blouse. Provide bust measurement to your tailor or select nearest size.",
   "headers": ["Blouse Size", "Bust (in)", "Bust (cm)", "Waist (in)", "Waist (cm)"],
   "rows": [
     ["28", "28", "71", "24", "61"],
     ["30", "30", "76", "26", "66"],
     ["32", "32", "81", "28", "71"],
     ["34", "34", "86", "30", "76"],
     ["36", "36", "91", "32", "81"],
     ["38", "38", "97", "34", "86"],
     ["40", "40", "102", "36", "91"],
     ["42", "42", "107", "38", "97"],
     ["44", "44", "112", "40", "102"]
   ],
   "tips": [
     "Measure bust at the fullest point keeping the tape parallel to the floor.",
     "Measure waist at the narrowest part of your torso.",
     "Standard saree length is 5.5 m; blouse piece of ~0.8 m is included."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000002',
 '5eed0000-0000-0000-0005-000000000001',
 'Saree Fabric & Length Guide',
 'metres',
 '{
   "note": "Standard saree dimensions by fabric type.",
   "headers": ["Fabric Type", "Standard Length (m)", "Standard Width (cm)", "Blouse Piece Included"],
   "rows": [
     ["Silk (Banarasi, Kanjivaram)", "5.5", "120", "Yes (0.8 m)"],
     ["Cotton (Chanderi, Maheshwari)", "5.5", "110", "Yes (0.8 m)"],
     ["Georgette / Chiffon", "5.5 – 6.0", "110", "Yes (0.8 m)"],
     ["Net / Embroidered", "5.5", "110", "Sometimes"],
     ["Linen / Tussar", "5.5", "115", "Yes (0.8 m)"]
   ],
   "tips": [
     "If no blouse piece is included, purchase 0.8–1 m of matching fabric separately.",
     "Pre-stitched sarees come as 2-piece sets; no blouse alteration needed."
   ]
 }'::jsonb,
 true, now(), now()),

-- ── LEHENGAS ─────────────────────────────────────────────────────────────────

('a100c000-0000-0000-0000-000000000003',
 '5eed0000-0000-0000-0005-000000000002',
 'Lehenga Choli (Top) Size Chart',
 'inches',
 '{
   "note": "Measurements for the choli (blouse/top). Most cholis have a back hook that allows 1–2 inch adjustment.",
   "headers": ["Size", "Bust (in)", "Bust (cm)", "Waist (in)", "Waist (cm)", "Shoulder (in)"],
   "rows": [
     ["XS", "30–32", "76–81", "24–26", "61–66", "13"],
     ["S",  "32–34", "81–86", "26–28", "66–71", "13.5"],
     ["M",  "34–36", "86–91", "28–30", "71–76", "14"],
     ["L",  "36–38", "91–97", "30–32", "76–81", "14.5"],
     ["XL", "38–40", "97–102", "32–34", "81–86", "15"],
     ["XXL","40–42", "102–107", "34–36", "86–91", "15.5"],
     ["3XL","42–44", "107–112", "36–38", "91–97", "16"]
   ],
   "tips": [
     "For custom-fit choli, share your exact bust and waist with us in the order notes.",
     "Padding/lining may add ~1 inch to the bust measurement."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000004',
 '5eed0000-0000-0000-0005-000000000002',
 'Lehenga Skirt Size Chart',
 'inches',
 '{
   "note": "Lehenga skirts come with a drawstring/hook waistband. Length is measured from waist to hem.",
   "headers": ["Size", "Waist (in)", "Waist (cm)", "Hip (in)", "Hip (cm)", "Length (in)", "Length (cm)"],
   "rows": [
     ["XS", "24–26", "61–66", "34–36", "86–91",  "40", "102"],
     ["S",  "26–28", "66–71", "36–38", "91–97",  "41", "104"],
     ["M",  "28–30", "71–76", "38–40", "97–102", "42", "107"],
     ["L",  "30–32", "76–81", "40–42", "102–107","43", "109"],
     ["XL", "32–34", "81–86", "42–44", "107–112","44", "112"],
     ["XXL","34–36", "86–91", "44–46", "112–117","44", "112"],
     ["3XL","36–38", "91–97", "46–48", "117–122","44", "112"]
   ],
   "tips": [
     "Drawstring waistbands allow 2–3 inch flexibility on either side.",
     "Standard floor-length is ~42 in for a 5''4'' height. If you are taller/shorter, mention it in order notes.",
     "Flared (gharara-cut) lehengas may run wider at the hip — check product description."
   ]
 }'::jsonb,
 true, now(), now()),

-- ── KURTA SETS ───────────────────────────────────────────────────────────────

('a100c000-0000-0000-0000-000000000005',
 '5eed0000-0000-0000-0005-000000000003',
 'Kurta Size Chart',
 'inches',
 '{
   "note": "Measurements are body measurements. The kurta is cut with 1–2 inch ease for comfort.",
   "headers": ["Size", "Bust (in)", "Bust (cm)", "Waist (in)", "Waist (cm)", "Hip (in)", "Hip (cm)", "Kurta Length (in)"],
   "rows": [
     ["XS",  "30–32", "76–81",   "24–26", "61–66",   "34–36", "86–91",   "46"],
     ["S",   "32–34", "81–86",   "26–28", "66–71",   "36–38", "91–97",   "47"],
     ["M",   "34–36", "86–91",   "28–30", "71–76",   "38–40", "97–102",  "48"],
     ["L",   "36–38", "91–97",   "30–32", "76–81",   "40–42", "102–107", "49"],
     ["XL",  "38–40", "97–102",  "32–34", "81–86",   "42–44", "107–112", "50"],
     ["XXL", "40–42", "102–107", "34–36", "86–91",   "44–46", "112–117", "51"],
     ["3XL", "42–44", "107–112", "36–38", "91–97",   "46–48", "117–122", "52"],
     ["4XL", "44–46", "112–117", "38–40", "97–102",  "48–50", "122–127", "53"]
   ],
   "tips": [
     "Kurta length listed is for a standard height of 5''4''. Length varies by style — check product description.",
     "For a relaxed fit, go one size up.",
     "Anarkali styles are measured at the bust; flared hem accommodates larger hips."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000006',
 '5eed0000-0000-0000-0005-000000000003',
 'Salwar / Palazzo / Pant Size Chart',
 'inches',
 '{
   "note": "Measurements for the bottom wear that comes with the kurta set. Most have an elasticated waistband.",
   "headers": ["Size", "Waist (in)", "Waist (cm)", "Hip (in)", "Hip (cm)", "Inseam (in)", "Total Length (in)"],
   "rows": [
     ["XS",  "24–26", "61–66",   "34–36", "86–91",   "26", "38"],
     ["S",   "26–28", "66–71",   "36–38", "91–97",   "27", "39"],
     ["M",   "28–30", "71–76",   "38–40", "97–102",  "27", "40"],
     ["L",   "30–32", "76–81",   "40–42", "102–107", "28", "41"],
     ["XL",  "32–34", "81–86",   "42–44", "107–112", "28", "42"],
     ["XXL", "34–36", "86–91",   "44–46", "112–117", "28", "42"],
     ["3XL", "36–38", "91–97",   "46–48", "117–122", "28", "42"],
     ["4XL", "38–40", "97–102",  "48–50", "122–127", "28", "42"]
   ],
   "tips": [
     "Elasticated waistbands accommodate 2–3 inches on either side of the listed range.",
     "Palazzo and wide-leg pants have generous hip ease; choose by waist size.",
     "Cigarette pants are slim-fit; choose by hip measurement."
   ]
 }'::jsonb,
 true, now(), now()),

-- ── SUITS UNSTITCHED ─────────────────────────────────────────────────────────

('a100c000-0000-0000-0000-000000000007',
 '5eed0000-0000-0000-0005-000000000004',
 'Unstitched Suit — Fabric Length Guide',
 'metres',
 '{
   "note": "Standard fabric lengths included in each unstitched suit set. Share your body measurements with your tailor.",
   "headers": ["Piece", "Standard Length (m)", "Typical Width (cm)", "Notes"],
   "rows": [
     ["Kameez (Top fabric)",   "2.5", "44–45", "Enough for sizes up to 3XL"],
     ["Salwar (Bottom fabric)", "2.5", "44–45", "Enough for sizes up to 3XL"],
     ["Dupatta",               "2.5", "100",   "Pre-finished edges on most fabrics"]
   ],
   "tips": [
     "For sizes above 3XL, confirm with us before ordering — some fabrics need extra yardage.",
     "Embroidered kameez pieces may be shorter (2.0–2.3 m) but carry no wastage."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000008',
 '5eed0000-0000-0000-0005-000000000004',
 'Measurement Guide for Your Tailor',
 'inches',
 '{
   "note": "Share these body measurements with your tailor when getting an unstitched suit stitched.",
   "headers": ["Measurement", "Where to Measure", "Typical Range (in)"],
   "rows": [
     ["Bust / Chest",    "Fullest part of chest, tape parallel to floor",        "30 – 48"],
     ["Waist",           "Narrowest part of torso",                              "24 – 42"],
     ["Hip",             "Fullest part of hip, ~7 in below natural waist",       "34 – 52"],
     ["Shoulder Width",  "Seam to seam across the back",                         "13 – 17"],
     ["Kameez Length",   "Shoulder to desired hem (knee/thigh/floor)",           "36 – 56"],
     ["Sleeve Length",   "Shoulder seam to wrist",                               "20 – 25"],
     ["Salwar Length",   "Waist to ankle bone",                                  "38 – 44"],
     ["Salwar Bottom",   "Circumference at ankle (for straight/cigarette cut)",  "12 – 20"]
   ],
   "tips": [
     "Always measure over innerwear, not bulky clothes.",
     "Add 1–2 inches to bust and hip for comfort ease.",
     "Mention preferred neckline, sleeve style, and bottom cut to your tailor."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000009',
 '5eed0000-0000-0000-0005-000000000004',
 'Recommended Stitching Allowances',
 'inches',
 '{
   "note": "Standard stitching allowances included in the recommended fabric lengths. Share with your tailor.",
   "headers": ["Garment Part", "Seam Allowance (in)", "Hem Allowance (in)"],
   "rows": [
     ["Kameez side seams",    "0.5", "1.5"],
     ["Kameez shoulder",      "0.5", "—"],
     ["Kameez neckline",      "0.5", "0.5"],
     ["Sleeve seam",          "0.5", "1.0"],
     ["Salwar waistband",     "—",   "1.5"],
     ["Salwar inseam",        "0.5", "—"],
     ["Salwar bottom",        "—",   "1.5"]
   ],
   "tips": [
     "Request tailor to leave 1 inch extra at side seams for future alterations.",
     "For embroidered fabrics, ask tailor to cut carefully to preserve motif placement."
   ]
 }'::jsonb,
 true, now(), now()),

-- ── DUPATTAS ─────────────────────────────────────────────────────────────────

('a100c000-0000-0000-0000-000000000010',
 '5eed0000-0000-0000-0005-000000000005',
 'Dupatta Dimensions Guide',
 'metres',
 '{
   "note": "Dupattas are sold in standard dimensions. Most styles are one-size; length and width vary by type.",
   "headers": ["Dupatta Type", "Length (m)", "Width (cm)", "Best Worn With"],
   "rows": [
     ["Regular Dupatta",       "2.5", "100", "Kurta sets, salwar suits"],
     ["Bridal / Heavy Dupatta","3.0", "110", "Lehengas, bridal wear"],
     ["Stole / Scarf",         "2.0", "80",  "Indo-western, casual kurtas"],
     ["Chunni",                "2.5", "90",  "Punjabi suits, anarkalis"],
     ["Phulkari Dupatta",      "2.5", "100", "Kurta sets, traditional wear"],
     ["Net / Sheer Dupatta",   "2.5", "100", "Party lehengas, anarkalis"]
   ],
   "tips": [
     "Dupattas with heavy embroidery or gota work may feel shorter due to the weight — drape over shoulder rather than neck.",
     "Check product description for exact dimensions as designer pieces may vary."
   ]
 }'::jsonb,
 true, now(), now()),

('a100c000-0000-0000-0000-000000000011',
 '5eed0000-0000-0000-0005-000000000005',
 'Dupatta Draping Style Guide',
 NULL,
 '{
   "note": "Different draping styles suit different looks. No measurements needed — this is a style reference.",
   "headers": ["Draping Style", "Suited For", "Occasion"],
   "rows": [
     ["Over one shoulder (single drape)", "Straight kurtas, anarkalis", "Casual, office, daily wear"],
     ["Double drape (both shoulders)",    "Suit sets, salwar kameez",   "Festive, family functions"],
     ["Head drape (ghunghat style)",      "Bridal, heavy dupattas",     "Wedding ceremonies"],
     ["Pant tuck (front pleat tuck)",     "Palazzo sets, pant suits",   "Indo-western, parties"],
     ["Cape style (pinned at shoulders)", "Anarkalis, lehengas",        "Receptions, cocktail events"]
   ]
 }'::jsonb,
 true, now(), now());
