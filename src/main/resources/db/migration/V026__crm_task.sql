CREATE TABLE crm_task
(
    id                   UUID         NOT NULL PRIMARY KEY,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ  NOT NULL DEFAULT now(),
    organization_id      UUID         NOT NULL REFERENCES crm_organization (id) ON DELETE CASCADE,
    due_at               TIMESTAMPTZ  NOT NULL,
    reason               VARCHAR(512),
    comment_text         TEXT,
    status               VARCHAR(32)  NOT NULL,
    assignee_account_id  UUID         NOT NULL REFERENCES account (id) ON DELETE CASCADE
);

CREATE INDEX idx_crm_task_org ON crm_task (organization_id);
CREATE INDEX idx_crm_task_due ON crm_task (due_at);
CREATE INDEX idx_crm_task_assignee ON crm_task (assignee_account_id);
CREATE INDEX idx_crm_task_status ON crm_task (status);
