CREATE TABLE crm_contract
(
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id    UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    start_date         DATE,
    end_date           DATE,
    comment_text       TEXT
);

CREATE INDEX idx_crm_contract_org ON crm_contract (organization_id);

CREATE TABLE crm_contract_line
(
    id                 UUID         NOT NULL PRIMARY KEY,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT now(),
    contract_id        UUID         NOT NULL REFERENCES crm_contract (id) ON DELETE CASCADE,
    planned_date       DATE,
    planned_quantity   NUMERIC(18, 3),
    line_status        VARCHAR(32)  NOT NULL,
    comment_text       TEXT
);

CREATE INDEX idx_crm_contract_line_contract ON crm_contract_line (contract_id);

CREATE TABLE crm_supply
(
    id                  UUID         NOT NULL PRIMARY KEY,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id     UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    supply_at           DATE         NOT NULL,
    product_description TEXT         NOT NULL,
    quantity            NUMERIC(18, 3),
    status              VARCHAR(32)  NOT NULL,
    comment_text        TEXT,
    delivery_latitude   DOUBLE PRECISION,
    delivery_longitude  DOUBLE PRECISION,
    contract_line_id    UUID REFERENCES crm_contract_line (id) ON DELETE SET NULL
);

CREATE INDEX idx_crm_supply_org ON crm_supply (organization_id);
CREATE INDEX idx_crm_supply_at ON crm_supply (supply_at);

CREATE TABLE crm_company_object
(
    id           UUID         NOT NULL PRIMARY KEY,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    name         VARCHAR(512) NOT NULL,
    latitude     DOUBLE PRECISION NOT NULL,
    longitude    DOUBLE PRECISION NOT NULL,
    comment_text TEXT
);

CREATE INDEX idx_crm_company_object_coords ON crm_company_object (latitude, longitude);

ALTER TABLE crm_interaction
    ADD COLUMN next_task_id UUID REFERENCES crm_task (id) ON DELETE SET NULL;
