CREATE TABLE product
(
    id          UUID           NOT NULL PRIMARY KEY,
    class_id    UUID           NOT NULL REFERENCES product_class (id),
    series_id   UUID               NULL REFERENCES product_series (id),
    type_id     UUID               NULL REFERENCES product_type (id),
    brand       VARCHAR(255)   NOT NULL,
    description TEXT,
    height_mm   INTEGER        NOT NULL,
    width_mm    INTEGER        NOT NULL,
    length_mm   INTEGER        NOT NULL,
    weight_kg   NUMERIC(12, 3) NOT NULL,
    price       NUMERIC(12, 2) NOT NULL,
    is_active   BOOLEAN        NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_product_identity
        UNIQUE (class_id, series_id, type_id, brand, height_mm, width_mm, length_mm)
);

CREATE INDEX idx_product_class_id ON product (class_id);
CREATE INDEX idx_product_series_id ON product (series_id);
CREATE INDEX idx_product_type_id ON product (type_id);
CREATE INDEX idx_product_price ON product (price);
