CREATE TABLE promotion
(
    id                   UUID            NOT NULL PRIMARY KEY,
    name                 VARCHAR(255)    NOT NULL,
    discount_percent     NUMERIC(5, 2)   NOT NULL,
    valid_from           TIMESTAMPTZ,
    valid_to             TIMESTAMPTZ,
    is_active            BOOLEAN         NOT NULL DEFAULT true,
    priority             INTEGER         NOT NULL DEFAULT 0,
    scope                VARCHAR(32)     NOT NULL,
    scope_reference_id   UUID,
    created_at           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_promotion_scope CHECK (scope IN ('ALL', 'PRODUCT', 'CLASS', 'SERIES', 'PRODUCT_TYPE')),
    CONSTRAINT chk_promotion_discount CHECK (discount_percent >= 0 AND discount_percent <= 100),
    CONSTRAINT chk_promotion_scope_ref CHECK (
        (scope = 'ALL' AND scope_reference_id IS NULL)
            OR (scope <> 'ALL' AND scope_reference_id IS NOT NULL)
        )
);

CREATE INDEX idx_promotion_active ON promotion (is_active);
CREATE INDEX idx_promotion_valid_from ON promotion (valid_from);
CREATE INDEX idx_promotion_valid_to ON promotion (valid_to);
