ALTER TABLE products
    ADD COLUMN IF NOT EXISTS designer_id UUID REFERENCES designers(id);
