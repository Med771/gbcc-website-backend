package backend.website.gbcc.logic.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Пакет событий first-party аналитики")
public record SiteAnalyticsCollectRequestDto(
        @NotNull
        @Schema(description = "Идентификатор посетителя (UUID), совпадает с cookie при наличии")
        UUID visitorId,

        @NotNull
        @Schema(description = "Идентификатор сессии (UUID)")
        UUID sessionId,

        @NotEmpty
        @Valid
        @Schema(description = "Список событий (лимит длины — app.analytics.max-batch-size)")
        List<SiteAnalyticsCollectEventDto> events
) {
}
