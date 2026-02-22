CREATE TABLE files
(
    id         UUID        NOT NULL PRIMARY KEY,
    key        TEXT        NOT NULL,
    bucket     TEXT        NOT NULL,
    file_name  TEXT,
    mime_type  TEXT        NOT NULL,
    size       BIGINT         NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
