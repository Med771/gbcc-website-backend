ALTER TABLE orders ADD COLUMN contact_name VARCHAR(255);
ALTER TABLE orders ADD COLUMN contact_phone VARCHAR(64);
ALTER TABLE orders ADD COLUMN contact_email VARCHAR(255);
ALTER TABLE orders ADD COLUMN manager_notes TEXT;
ALTER TABLE orders ADD COLUMN delivery_fee NUMERIC(14, 2) NOT NULL DEFAULT 0;
ALTER TABLE orders ADD COLUMN crm_organization_id UUID REFERENCES crm_organization (id);

CREATE INDEX idx_orders_crm_organization ON orders (crm_organization_id);
