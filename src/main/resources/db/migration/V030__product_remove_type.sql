UPDATE promotion
SET scope = 'PRODUCT',
    scope_reference_id = (
        SELECT p.id
        FROM product p
        WHERE p.type_id = promotion.scope_reference_id
        LIMIT 1
    )
WHERE scope = 'PRODUCT_TYPE'
  AND scope_reference_id IS NOT NULL
  AND EXISTS (
    SELECT 1 FROM product p WHERE p.type_id = promotion.scope_reference_id
);

UPDATE promotion
SET scope = 'CLASS',
    scope_reference_id = (
        SELECT p.class_id
        FROM product p
        WHERE p.type_id = promotion.scope_reference_id
        LIMIT 1
    )
WHERE scope = 'PRODUCT_TYPE'
  AND scope_reference_id IS NOT NULL;

DELETE FROM promotion WHERE scope = 'PRODUCT_TYPE';

ALTER TABLE promotion DROP CONSTRAINT IF EXISTS chk_promotion_scope;
ALTER TABLE promotion
    ADD CONSTRAINT chk_promotion_scope CHECK (scope IN ('ALL', 'PRODUCT', 'CLASS', 'SERIES'));

ALTER TABLE product DROP CONSTRAINT IF EXISTS uq_product_identity;

DROP INDEX IF EXISTS idx_product_type_id;

ALTER TABLE product DROP COLUMN IF EXISTS type_id;

ALTER TABLE product
    ADD CONSTRAINT uq_product_identity
        UNIQUE (class_id, series_id, brand, height_mm, width_mm, length_mm);

DROP TABLE IF EXISTS product_type;
