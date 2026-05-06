CREATE TABLE crm_interaction
(
    id                  UUID         NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id     UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    occurred_at         TIMESTAMPTZ  NOT NULL,
    author_account_id   UUID         NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    result_note         VARCHAR(512),
    comment_text        TEXT,
    next_step           TEXT
);

CREATE INDEX idx_crm_interaction_org ON crm_interaction (organization_id);
CREATE INDEX idx_crm_interaction_occurred ON crm_interaction (occurred_at DESC);
CREATE INDEX idx_crm_interaction_author ON crm_interaction (author_account_id);
