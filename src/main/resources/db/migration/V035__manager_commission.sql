ALTER TABLE referral_commission
    ADD COLUMN commission_percent NUMERIC(5, 2),
    ADD COLUMN client_type VARCHAR(16);

UPDATE referral_commission
SET commission_percent = 5,
    client_type = 'RETURNING'
WHERE commission_percent IS NULL;

ALTER TABLE referral_commission
    ALTER COLUMN commission_percent SET NOT NULL,
    ALTER COLUMN client_type SET NOT NULL;

CREATE TABLE manager_referral_commission
(
    id                  UUID PRIMARY KEY,
    manager_account_id  UUID           NOT NULL REFERENCES account (id),
    customer_account_id UUID           NOT NULL REFERENCES account (id),
    order_id            UUID           NOT NULL UNIQUE REFERENCES orders (id),
    order_amount        NUMERIC(14, 2) NOT NULL,
    commission_percent  NUMERIC(5, 2)  NOT NULL,
    commission_amount   NUMERIC(14, 2) NOT NULL,
    client_type         VARCHAR(16)    NOT NULL,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE INDEX idx_manager_referral_commission_manager ON manager_referral_commission (manager_account_id);
CREATE INDEX idx_manager_referral_commission_customer ON manager_referral_commission (customer_account_id);
