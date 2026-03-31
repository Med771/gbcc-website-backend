ALTER TABLE account
    ADD COLUMN referral_code VARCHAR(32) NULL,
    ADD COLUMN referred_by_account_id UUID NULL,
    ADD CONSTRAINT uq_account_referral_code UNIQUE (referral_code),
    ADD CONSTRAINT fk_account_referred_by
        FOREIGN KEY (referred_by_account_id) REFERENCES account (id);

CREATE INDEX idx_account_referred_by ON account (referred_by_account_id);

CREATE TABLE referral_click
(
    id                   UUID         NOT NULL PRIMARY KEY,
    referrer_account_id  UUID         NOT NULL,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    visitor_fingerprint  VARCHAR(128) NULL,
    CONSTRAINT fk_referral_click_referrer FOREIGN KEY (referrer_account_id) REFERENCES account (id)
);

CREATE INDEX idx_referral_click_referrer_created ON referral_click (referrer_account_id, created_at DESC);

CREATE TABLE referral_commission
(
    id                   UUID           NOT NULL PRIMARY KEY,
    referrer_account_id  UUID           NOT NULL,
    referee_account_id   UUID           NOT NULL,
    order_id             UUID           NOT NULL,
    order_amount         NUMERIC(14, 2) NOT NULL,
    commission_amount    NUMERIC(14, 2) NOT NULL,
    created_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uq_referral_commission_order UNIQUE (order_id),
    CONSTRAINT fk_referral_commission_referrer FOREIGN KEY (referrer_account_id) REFERENCES account (id),
    CONSTRAINT fk_referral_commission_referee FOREIGN KEY (referee_account_id) REFERENCES account (id),
    CONSTRAINT fk_referral_commission_order FOREIGN KEY (order_id) REFERENCES orders (id)
);

CREATE INDEX idx_referral_commission_referrer ON referral_commission (referrer_account_id);
CREATE INDEX idx_referral_commission_referee ON referral_commission (referee_account_id);

CREATE TABLE referral_withdrawal
(
    id                     UUID          NOT NULL PRIMARY KEY,
    account_id             UUID          NOT NULL,
    amount                 NUMERIC(14, 2) NOT NULL,
    status                 VARCHAR(32)   NOT NULL,
    created_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ   NOT NULL DEFAULT now(),
    processed_at           TIMESTAMPTZ   NULL,
    processed_by_account_id UUID         NULL,
    admin_note             VARCHAR(2000) NULL,
    CONSTRAINT fk_referral_withdrawal_account FOREIGN KEY (account_id) REFERENCES account (id),
    CONSTRAINT fk_referral_withdrawal_processed_by FOREIGN KEY (processed_by_account_id) REFERENCES account (id)
);

CREATE INDEX idx_referral_withdrawal_account_status ON referral_withdrawal (account_id, status);
CREATE INDEX idx_referral_withdrawal_created ON referral_withdrawal (created_at DESC);
