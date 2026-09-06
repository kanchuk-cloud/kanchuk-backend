CREATE TABLE image_types (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    label       VARCHAR(100) NOT NULL,
    value       VARCHAR(100) NOT NULL UNIQUE,
    sort_order  INT          NOT NULL DEFAULT 0,
    is_active   BOOLEAN      NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

INSERT INTO image_types (label, value, sort_order) VALUES
    ('Front View',       'front-view',       0),
    ('Back View',        'back-view',        1),
    ('Left Side View',   'left-side-view',   2),
    ('Right Side View',  'right-side-view',  3),
    ('Lehenga',          'lehenga',          4),
    ('Blouse',           'blouse',           5),
    ('Dupatta',          'dupatta',          6),
    ('Detail',           'detail',           7),
    ('Model',            'model',            8);
