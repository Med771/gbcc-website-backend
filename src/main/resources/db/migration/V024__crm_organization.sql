CREATE TABLE crm_organization
(
    id                                  UUID         NOT NULL PRIMARY KEY,
    created_at                          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at                          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    name                                VARCHAR(512) NOT NULL,
    external_number                     VARCHAR(128),
    legal_address                       TEXT,
    delivery_address                    TEXT,
    floor_note                          VARCHAR(255),
    comment_general                     TEXT,
    product_types_note                  TEXT,
    supply_volume_note                  TEXT,
    supply_schedule_note                TEXT,
    cooperation_until                   DATE,
    client_status                       VARCHAR(32)  NOT NULL,
    latitude                            DOUBLE PRECISION,
    longitude                           DOUBLE PRECISION,
    assigned_to_account_id              UUID REFERENCES account (id) ON DELETE SET NULL,
    converted_from_contact_request_id   UUID REFERENCES contact_request (id) ON DELETE SET NULL,
    converted_from_cooperation_request_id UUID REFERENCES cooperation_request (id) ON DELETE SET NULL
);

CREATE INDEX idx_crm_organization_assigned ON crm_organization (assigned_to_account_id);
CREATE INDEX idx_crm_organization_name ON crm_organization (name);
CREATE INDEX idx_crm_organization_status ON crm_organization (client_status);

CREATE TABLE crm_organization_contact
(
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id    UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    full_name          VARCHAR(255),
    department         VARCHAR(255),
    phone              VARCHAR(64),
    email              VARCHAR(255),
    extra_note         TEXT
);

CREATE INDEX idx_crm_org_contact_org ON crm_organization_contact (organization_id);

CREATE TABLE crm_organization_contact_history
(
    id                     UUID         NOT NULL PRIMARY KEY,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ  NOT NULL DEFAULT now(),
    contact_id             UUID         NOT NULL REFERENCES crm_organization_contact (id) ON DELETE CASCADE,
    changed_by_account_id  UUID         NOT NULL REFERENCES account (id) ON DELETE CASCADE,
    previous_snapshot      TEXT         NOT NULL,
    new_snapshot           TEXT         NOT NULL
);

CREATE INDEX idx_crm_org_contact_hist_contact ON crm_organization_contact_history (contact_id);
