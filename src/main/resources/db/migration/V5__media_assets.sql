CREATE TABLE media_assets (
  id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  category      VARCHAR(50)  NOT NULL,
  subcategory   VARCHAR(50)  NOT NULL,
  filename      VARCHAR(255) NOT NULL,
  original_name VARCHAR(255),
  url           TEXT         NOT NULL,
  size_bytes    BIGINT,
  content_type  VARCHAR(100),
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_media_assets_category_sub ON media_assets(category, subcategory);
