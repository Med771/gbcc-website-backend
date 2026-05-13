package backend.website.gbcc.logic.crm.analytics.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Строка ленты событий сайта")
public record CrmSiteAnalyticsEventRowDto(
        UUID id,
        Instant occurredAt,
        Instant receivedAt,
        UUID visitorId,
        UUID sessionId,
        String eventType,
        String path,
        String referrer,
        String userAgent,
        JsonNode metadata
) {
}
