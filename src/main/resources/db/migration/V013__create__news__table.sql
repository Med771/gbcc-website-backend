CREATE TABLE news
(
    id                    UUID        NOT NULL PRIMARY KEY,
    title                 VARCHAR(255) NOT NULL,
    preview_text          TEXT        NOT NULL,
    content               TEXT        NOT NULL,
    cover_file_id         UUID,
    is_published          BOOLEAN     NOT NULL DEFAULT false,
    published_at          TIMESTAMPTZ,
    created_by_account_id UUID        NOT NULL REFERENCES account (id),
    updated_by_account_id UUID        NOT NULL REFERENCES account (id),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_news_published_at_required
        CHECK ((is_published = false AND published_at IS NULL) OR (is_published = true AND published_at IS NOT NULL))
);

CREATE INDEX idx_news_published_at ON news (published_at DESC);
CREATE INDEX idx_news_is_published ON news (is_published);
