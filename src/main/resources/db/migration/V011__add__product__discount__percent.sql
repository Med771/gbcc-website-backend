ALTER TABLE product
    ADD COLUMN discount_percent NUMERIC(5, 2) NOT NULL DEFAULT 0.00;

ALTER TABLE product
    ADD CONSTRAINT chk_product_discount_percent_range
        CHECK (discount_percent >= 0.00 AND discount_percent <= 100.00);
