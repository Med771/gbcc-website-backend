CREATE TABLE staff_profile
(
    account_id           UUID PRIMARY KEY REFERENCES account (id) ON DELETE CASCADE,
    position_title       VARCHAR(255),
    social_link          VARCHAR(512),
    bank_account_details TEXT,
    display_id           VARCHAR(32) UNIQUE
);

CREATE INDEX idx_staff_profile_display_id ON staff_profile (display_id);
