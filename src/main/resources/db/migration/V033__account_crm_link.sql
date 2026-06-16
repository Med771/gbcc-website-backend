ALTER TABLE account ADD COLUMN crm_organization_id UUID REFERENCES crm_organization (id);
ALTER TABLE account ADD COLUMN brought_by_manager_id UUID REFERENCES account (id);

CREATE INDEX idx_account_crm_organization ON account (crm_organization_id);
CREATE INDEX idx_account_brought_by_manager ON account (brought_by_manager_id);
