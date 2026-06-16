ALTER TABLE crm_supply ADD COLUMN supply_time TIME;
ALTER TABLE crm_supply ADD COLUMN delivery_address TEXT;
ALTER TABLE crm_supply ADD COLUMN object_name VARCHAR(512);
ALTER TABLE crm_supply ADD COLUMN branch_id UUID REFERENCES crm_organization_branch (id) ON DELETE SET NULL;
ALTER TABLE crm_supply ADD COLUMN product_id UUID REFERENCES product (id) ON DELETE SET NULL;

CREATE INDEX idx_crm_supply_supply_at ON crm_supply (supply_at);
CREATE INDEX idx_crm_supply_branch ON crm_supply (branch_id);
