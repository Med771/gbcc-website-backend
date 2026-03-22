ALTER TABLE product
    ADD COLUMN popularity_score INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_product_popularity_score ON product (popularity_score DESC);
