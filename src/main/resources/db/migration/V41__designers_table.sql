CREATE TABLE IF NOT EXISTS designers (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(200)  NOT NULL,
    slug        VARCHAR(200)  NOT NULL UNIQUE,
    avatar_url  TEXT,
    bio         TEXT,
    sort_order  INT           NOT NULL DEFAULT 0,
    is_active   BOOLEAN       NOT NULL DEFAULT TRUE,
    published_at TIMESTAMPTZ,
    expires_at  TIMESTAMPTZ,
    meta_title  VARCHAR(70),
    meta_description VARCHAR(160),
    og_image_url TEXT,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    deleted_at  TIMESTAMPTZ
);
