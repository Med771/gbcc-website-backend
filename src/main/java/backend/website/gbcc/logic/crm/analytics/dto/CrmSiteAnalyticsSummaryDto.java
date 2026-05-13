package backend.website.gbcc.logic.crm.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Сводка по веб-событиям за период (по received_at)")
public record CrmSiteAnalyticsSummaryDto(
        @Schema(description = "Уникальные visitor_id за [from, to)")
        long uniqueVisitors,
        @Schema(description = "Всего событий за [from, to)")
        long totalEvents,
        @Schema(description = "Топ путей по числу событий")
        List<CrmSiteAnalyticsPathCountDto> topPaths,
        @Schema(description = "Уникальные session_id за последние 24 часа от момента запроса")
        long activeSessionsLast24Hours
) {
}
