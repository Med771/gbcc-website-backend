ALTER TABLE crm_interaction ADD COLUMN interaction_type VARCHAR(32) NOT NULL DEFAULT 'CALL';

CREATE INDEX idx_crm_interaction_type ON crm_interaction (interaction_type);
