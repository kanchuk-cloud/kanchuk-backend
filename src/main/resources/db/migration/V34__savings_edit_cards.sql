CREATE TABLE savings_edit_cards (
    id            UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    category_slug VARCHAR(100)  NOT NULL,
    image_url     VARCHAR(500),
    label_override VARCHAR(100),
    sort_order    INT           NOT NULL DEFAULT 0,
    is_active     BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);
