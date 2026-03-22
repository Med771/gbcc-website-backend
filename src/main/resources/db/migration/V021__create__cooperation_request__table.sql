CREATE TABLE cooperation_request
(
    id                      UUID         NOT NULL PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL,
    phone                   VARCHAR(64)  NOT NULL,
    email                   VARCHAR(255) NOT NULL,
    cooperation_type      VARCHAR(128),
    comment                 TEXT,
    attachment_file_id      UUID REFERENCES files (id) ON DELETE SET NULL,
    consent_processing      BOOLEAN      NOT NULL,
    assigned_to_account_id  UUID REFERENCES account (id) ON DELETE SET NULL,
    assigned_at             TIMESTAMPTZ,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT chk_cooperation_request_consent CHECK (consent_processing = true)
);

CREATE INDEX idx_cooperation_request_assigned ON cooperation_request (assigned_to_account_id);
CREATE INDEX idx_cooperation_request_created ON cooperation_request (created_at DESC);
CREATE INDEX idx_cooperation_request_attachment ON cooperation_request (attachment_file_id);
