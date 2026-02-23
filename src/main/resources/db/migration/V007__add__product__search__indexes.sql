CREATE INDEX idx_product_brand_lower_pattern
    ON product ((lower(brand)) varchar_pattern_ops);

CREATE INDEX idx_product_class_name_lower_pattern
    ON product_class ((lower(name)) varchar_pattern_ops);

CREATE INDEX idx_product_series_name_lower_pattern
    ON product_series ((lower(name)) varchar_pattern_ops);

CREATE INDEX idx_product_type_name_lower_pattern
    ON product_type ((lower(name)) varchar_pattern_ops);
