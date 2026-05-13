package backend.website.gbcc.logic.analytics.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.Map;

@Schema(description = "Одно событие в пакете сбора аналитики")
public record SiteAnalyticsCollectEventDto(
        @NotNull
        @Schema(description = "Время события по часам клиента (ISO-8601)", example = "2026-05-13T12:00:00Z")
        Instant occurredAt,

        @NotBlank
        @Size(max = 32)
        @Schema(description = "Тип события", example = "PAGE_VIEW")
        String eventType,

        @Size(max = 1024)
        @Schema(description = "Путь страницы; если не начинается с «/», будет нормализован", example = "/catalog")
        String path,

        @Size(max = 1024)
        @Schema(description = "Referrer (опционально)")
        String referrer,

        @Schema(description = "Произвольный JSON-объект (ограничение размера — app.analytics.max-metadata-chars)")
        Map<String, Object> metadata
) {
}
