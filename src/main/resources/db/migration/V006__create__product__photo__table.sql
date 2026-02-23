CREATE TABLE product_photo
(
    id         UUID        NOT NULL PRIMARY KEY,
    product_id UUID        NOT NULL REFERENCES product (id) ON DELETE CASCADE,
    file_id    UUID        NOT NULL REFERENCES files (id),
    sort_order INTEGER     NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_photo_order UNIQUE (product_id, sort_order)
);

CREATE INDEX idx_product_photo_product_id ON product_photo (product_id);
