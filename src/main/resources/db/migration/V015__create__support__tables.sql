CREATE TABLE support_conversation
(
    id                 UUID            NOT NULL PRIMARY KEY,
    subject            VARCHAR(500),
    status             VARCHAR(32)     NOT NULL,
    guest_name         VARCHAR(255),
    guest_email        VARCHAR(255),
    customer_id        UUID            REFERENCES account (id),
    guest_access_token VARCHAR(64),
    created_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_support_conversation_status
        CHECK (status IN ('OPEN', 'CLOSED')),
    CONSTRAINT chk_support_conversation_guest_or_customer CHECK (
        (customer_id IS NOT NULL AND guest_access_token IS NULL AND guest_name IS NULL AND guest_email IS NULL)
            OR (customer_id IS NULL AND guest_access_token IS NOT NULL AND guest_name IS NOT NULL AND guest_email IS NOT NULL)
        )
);

CREATE UNIQUE INDEX uq_support_conversation_guest_token ON support_conversation (guest_access_token)
    WHERE guest_access_token IS NOT NULL;

CREATE INDEX idx_support_conversation_customer_id ON support_conversation (customer_id);
CREATE INDEX idx_support_conversation_status ON support_conversation (status);
CREATE INDEX idx_support_conversation_created_at ON support_conversation (created_at);

CREATE TABLE support_message
(
    id                  UUID            NOT NULL PRIMARY KEY,
    conversation_id     UUID            NOT NULL REFERENCES support_conversation (id) ON DELETE CASCADE,
    author_type         VARCHAR(32)     NOT NULL,
    author_account_id   UUID            REFERENCES account (id),
    body                TEXT            NOT NULL,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT now(),
    CONSTRAINT chk_support_message_author CHECK (
        (author_type = 'GUEST' AND author_account_id IS NULL)
            OR (author_type = 'CUSTOMER' AND author_account_id IS NOT NULL)
            OR (author_type = 'ADMIN' AND author_account_id IS NOT NULL)
        ),
    CONSTRAINT chk_support_message_author_type CHECK (author_type IN ('GUEST', 'CUSTOMER', 'ADMIN'))
);

CREATE INDEX idx_support_message_conversation_id ON support_message (conversation_id);
CREATE INDEX idx_support_message_created_at ON support_message (created_at);
