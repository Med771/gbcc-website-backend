CREATE SEQUENCE orders_display_number_seq;

ALTER TABLE orders
    ADD COLUMN display_number           BIGINT,
    ADD COLUMN payment_method         VARCHAR(32) NOT NULL DEFAULT 'CARD_OR_ON_RECEIPT',
    ADD COLUMN receipt_url            TEXT,
    ADD COLUMN estimated_delivery_end TIMESTAMPTZ;

UPDATE orders
SET display_number = nextval('orders_display_number_seq')
WHERE display_number IS NULL;

ALTER TABLE orders
    ALTER COLUMN display_number SET NOT NULL;

ALTER TABLE orders
    ADD CONSTRAINT uq_orders_display_number UNIQUE (display_number);

ALTER TABLE orders
    ADD CONSTRAINT chk_orders_payment_method
        CHECK (payment_method IN ('CARD_ONLINE', 'CARD_OR_ON_RECEIPT'));

DO
$$
DECLARE
    mx BIGINT;
BEGIN
    SELECT COALESCE(MAX(display_number), 0) INTO mx FROM orders;
    IF mx = 0 THEN
        PERFORM setval('orders_display_number_seq', 1, false);
    ELSE
        PERFORM setval('orders_display_number_seq', mx, true);
    END IF;
END
$$;
