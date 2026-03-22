CREATE TABLE orders
(
    id                     UUID            NOT NULL PRIMARY KEY,
    customer_id            UUID            NOT NULL REFERENCES account (id),
    status                 VARCHAR(32)     NOT NULL,
    delivery_address       TEXT            NOT NULL,
    customer_comment       TEXT,
    estimated_delivery_at  TIMESTAMPTZ,
    total_price            NUMERIC(14, 2)  NOT NULL,
    total_discounted_price NUMERIC(14, 2)  NOT NULL,
    created_at             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_orders_status
        CHECK (status IN ('CREATED', 'PROCESSING', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

CREATE INDEX idx_orders_customer_id ON orders (customer_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_created_at ON orders (created_at);

CREATE TABLE order_item
(
    id                        UUID            NOT NULL PRIMARY KEY,
    order_id                  UUID            NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id                UUID            NOT NULL REFERENCES product (id),
    quantity                  INTEGER         NOT NULL,
    unit_price                NUMERIC(12, 2)  NOT NULL,
    unit_discount_percent     NUMERIC(5, 2)   NOT NULL,
    unit_discounted_price     NUMERIC(12, 2)  NOT NULL,
    line_total_price          NUMERIC(14, 2)  NOT NULL,
    line_total_discounted_price NUMERIC(14, 2) NOT NULL,
    created_at                TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_item_quantity_positive CHECK (quantity > 0),
    CONSTRAINT chk_order_item_discount_range CHECK (unit_discount_percent >= 0.00 AND unit_discount_percent <= 100.00)
);

CREATE INDEX idx_order_item_order_id ON order_item (order_id);
CREATE INDEX idx_order_item_product_id ON order_item (product_id);

CREATE TABLE order_status_history
(
    id                      UUID         NOT NULL PRIMARY KEY,
    order_id                UUID         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    from_status             VARCHAR(32),
    to_status               VARCHAR(32)  NOT NULL,
    changed_by_account_id   UUID         REFERENCES account (id),
    comment                 TEXT,
    estimated_delivery_at   TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_order_status_history_to_status
        CHECK (to_status IN ('CREATED', 'PROCESSING', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED')),
    CONSTRAINT chk_order_status_history_from_status
        CHECK (from_status IS NULL OR from_status IN ('CREATED', 'PROCESSING', 'PACKED', 'SHIPPED', 'DELIVERED', 'CANCELLED'))
);

CREATE INDEX idx_order_status_history_order_id ON order_status_history (order_id);
CREATE INDEX idx_order_status_history_created_at ON order_status_history (created_at);
