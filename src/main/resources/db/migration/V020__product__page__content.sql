ALTER TABLE product
    ADD COLUMN tagline       VARCHAR(512),
    ADD COLUMN tags          VARCHAR(512),
    ADD COLUMN interest_count INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN delivery_text TEXT,
    ADD COLUMN licenses_text TEXT;
