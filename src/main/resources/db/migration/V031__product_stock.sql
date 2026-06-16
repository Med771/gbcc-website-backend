ALTER TABLE product ADD COLUMN stock_quantity INTEGER NOT NULL DEFAULT 0;

ALTER TABLE product ADD CONSTRAINT chk_product_stock_non_negative CHECK (stock_quantity >= 0);
