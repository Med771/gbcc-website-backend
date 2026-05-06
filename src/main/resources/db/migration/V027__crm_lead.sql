CREATE TABLE crm_lead
(
    id                      UUID         NOT NULL PRIMARY KEY,
    created_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ  NOT NULL DEFAULT now(),
    company_name            VARCHAR(512) NOT NULL,
    phones                  TEXT,
    presumed_contacts       TEXT,
    manager_comment         TEXT,
    department              VARCHAR(255),
    status                  VARCHAR(32)  NOT NULL,
    assigned_to_account_id  UUID REFERENCES account (id) ON DELETE SET NULL,
    converted_organization_id UUID REFERENCES crm_organization (id) ON DELETE SET NULL
);

CREATE INDEX idx_crm_lead_assigned ON crm_lead (assigned_to_account_id);
CREATE INDEX idx_crm_lead_status ON crm_lead (status);

ALTER TABLE crm_task
    ADD COLUMN lead_id UUID REFERENCES crm_lead (id) ON DELETE SET NULL;

ALTER TABLE crm_task
    ALTER COLUMN organization_id DROP NOT NULL;

ALTER TABLE crm_task
    ADD CONSTRAINT chk_crm_task_org_or_lead CHECK (
        (organization_id IS NOT NULL) OR (lead_id IS NOT NULL)
    );

ALTER TABLE crm_interaction
    ADD COLUMN lead_id UUID REFERENCES crm_lead (id) ON DELETE SET NULL;

ALTER TABLE crm_interaction
    ALTER COLUMN organization_id DROP NOT NULL;

ALTER TABLE crm_interaction
    ADD CONSTRAINT chk_crm_interaction_org_or_lead CHECK (
        (organization_id IS NOT NULL) OR (lead_id IS NOT NULL)
    );
