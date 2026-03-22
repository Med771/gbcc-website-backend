CREATE TABLE refresh_token
(
    id         UUID        NOT NULL PRIMARY KEY,
    account_id UUID        NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    token_hash TEXT        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked    BOOLEAN     NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_refresh_token_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_account_id ON refresh_token (account_id);
CREATE INDEX idx_refresh_token_expires_at ON refresh_token (expires_at);
