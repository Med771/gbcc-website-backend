CREATE TABLE site_analytics_event
(
    id            UUID         NOT NULL PRIMARY KEY,
    occurred_at   TIMESTAMPTZ  NOT NULL,
    received_at   TIMESTAMPTZ  NOT NULL DEFAULT now(),
    visitor_id    UUID         NOT NULL,
    session_id    UUID         NOT NULL,
    event_type    VARCHAR(32)  NOT NULL,
    path          TEXT         NOT NULL,
    referrer      TEXT,
    user_agent    VARCHAR(512),
    metadata      TEXT,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_site_analytics_event_received_at ON site_analytics_event (received_at DESC);
CREATE INDEX idx_site_analytics_event_visitor_received ON site_analytics_event (visitor_id, received_at DESC);
CREATE INDEX idx_site_analytics_event_session_received ON site_analytics_event (session_id, received_at DESC);
CREATE INDEX idx_site_analytics_event_type_received ON site_analytics_event (event_type, received_at DESC);
