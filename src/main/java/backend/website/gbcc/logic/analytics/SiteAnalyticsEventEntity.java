package backend.website.gbcc.logic.analytics;

import backend.website.gbcc.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "site_analytics_event")
@Getter
@Setter
public class SiteAnalyticsEventEntity extends BaseEntity {

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "visitor_id", nullable = false)
    private UUID visitorId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "event_type", nullable = false, length = 32)
    private String eventType;

    @Column(name = "path", nullable = false, columnDefinition = "text")
    private String path;

    @Column(name = "referrer", columnDefinition = "text")
    private String referrer;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "metadata", columnDefinition = "text")
    private String metadataJson;
}
