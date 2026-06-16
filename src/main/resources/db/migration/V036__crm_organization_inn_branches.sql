ALTER TABLE crm_organization ADD COLUMN inn VARCHAR(12);

CREATE TABLE crm_organization_branch
(
    id              UUID         NOT NULL PRIMARY KEY,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    name            VARCHAR(512) NOT NULL,
    address         TEXT         NOT NULL,
    is_default      BOOLEAN      NOT NULL DEFAULT false
);

CREATE INDEX idx_crm_org_branch_org ON crm_organization_branch (organization_id);

INSERT INTO crm_organization_branch (id, organization_id, name, address, is_default)
SELECT gen_random_uuid(),
       id,
       'Основной',
       delivery_address,
       true
FROM crm_organization
WHERE delivery_address IS NOT NULL
  AND trim(delivery_address) <> '';
