CREATE TABLE customer_analytics
(
    account_id                      UUID PRIMARY KEY REFERENCES account (id) ON DELETE CASCADE,
    manual_total_paid               NUMERIC(14, 2),
    manual_current_month_paid       NUMERIC(14, 2),
    manual_total_orders_count       INTEGER,
    manual_referred_clients_count   INTEGER,
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_by_account_id           UUID REFERENCES account (id)
);
