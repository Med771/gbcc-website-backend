CREATE TABLE contact_request
(
    id                      UUID         NOT NULL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    email                   VARCHAR(255) NOT NULL,
    phone                   VARCHAR(64),
    message                 TEXT         NOT NULL,
    consent_processing      BOOLEAN      NOT NULL,
    assigned_to_account_id  UUID REFERENCES account (id) ON DELETE SET NULL,
    assigned_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_contact_request_consent CHECK (consent_processing = true)
);

CREATE INDEX idx_contact_request_assigned ON contact_request (assigned_to_account_id);
CREATE INDEX idx_contact_request_created ON contact_request (created_at DESC);
