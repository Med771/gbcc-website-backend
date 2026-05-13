package backend.website.gbcc.logic.crm.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Путь и число событий за период")
public record CrmSiteAnalyticsPathCountDto(
        @Schema(example = "/")
        String path,
        @Schema(example = "42")
        long eventCount
) {
}
