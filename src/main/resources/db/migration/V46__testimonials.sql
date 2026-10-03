CREATE TABLE IF NOT EXISTS testimonials (
  id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  customer_name  VARCHAR(120) NOT NULL,
  city           VARCHAR(80),
  quote          TEXT         NOT NULL,
  rating         SMALLINT     NOT NULL DEFAULT 5 CHECK (rating BETWEEN 1 AND 5),
  product_name   VARCHAR(200),
  avatar_url     VARCHAR(500),
  is_verified    BOOLEAN      NOT NULL DEFAULT TRUE,
  is_active      BOOLEAN      NOT NULL DEFAULT TRUE,
  display_order  INT          NOT NULL DEFAULT 0,
  created_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

DO $$
BEGIN
  IF (SELECT COUNT(*) FROM testimonials) = 0 THEN
    INSERT INTO testimonials (customer_name, city, quote, rating, product_name, is_verified, is_active, display_order) VALUES
      ('Priya Sharma',   'Mumbai',    'The Kanjivaram saree I ordered was absolutely stunning! The quality is exceptional and it arrived beautifully packed. Will definitely shop again.', 5, 'Pure Kanjivaram Silk Saree',          TRUE, TRUE, 1),
      ('Ananya Reddy',   'Hyderabad', 'Used the AI Stylist feature and it suggested the perfect jewellery for my lehenga. The recommendations were spot-on and the products were delivered on time.', 5, 'Designer Lehenga + Kundan Necklace',  TRUE, TRUE, 2),
      ('Meera Nair',     'Bangalore', 'Bought a Chikankari kurta set for Eid. The embroidery is so delicate and beautiful. I got so many compliments! Kanchuk is now my go-to for ethnic wear.', 5, 'Chikankari Kurta Set',                TRUE, TRUE, 3),
      ('Divya Patel',    'Ahmedabad', 'The Bandhani saree is gorgeous! Super fast delivery too — ordered in the evening and it arrived the next morning. The packaging was so luxurious.', 4, 'Pure Silk Bandhani Saree',            TRUE, TRUE, 4),
      ('Roshni Joshi',   'Jaipur',    'I''ve been shopping ethnic wear for years and Kanchuk is hands down the best curation I''ve found. The handloom section is a treasure trove.', 5, 'Handloom Collection',                  TRUE, TRUE, 5),
      ('Kavitha Menon',  'Chennai',   'Ordered a Kanjivaram saree for my daughter''s wedding. The quality exceeded all expectations. The team even helped with customisation. 10/10 experience.', 5, 'Kanjivaram Silk Saree',               TRUE, TRUE, 6);
  END IF;
END $$;
