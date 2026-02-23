CREATE TABLE account
(
    id            UUID        NOT NULL PRIMARY KEY,
    name          VARCHAR(255),
    phone         VARCHAR(32),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(32)  NOT NULL,
    is_blocked    BOOLEAN      NOT NULL DEFAULT false,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uq_account_email UNIQUE (email),
    CONSTRAINT uq_account_phone UNIQUE (phone)
);

CREATE INDEX idx_account_role ON account (role);
CREATE INDEX idx_account_is_blocked ON account (is_blocked);
