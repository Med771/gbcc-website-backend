package backend.website.gbcc.logic.crm.analytics;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsEventRowDto;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsSummaryDto;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/crm/analytics")
@RequiredArgsConstructor
@Tag(name = "CRM — Site analytics", description = "Чтение событий first-party аналитики сайта (JWT, роли CRM).")
public class CrmSiteAnalyticsController {

    private final CrmSiteAnalyticsService siteAnalyticsService;

    @Operation(summary = "Сводка по событиям сайта", description = "Уникальные посетители, число событий, топ путей, активные сессии за 24 ч.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Неверный диапазон дат",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к CRM",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/summary")
    public CrmSiteAnalyticsSummaryDto summary(
            @RequestParam Instant from,
            @RequestParam Instant to
    ) {
        return siteAnalyticsService.summary(from, to);
    }

    @Operation(summary = "Лента событий", description = "Постранично по received_at (по умолчанию — новые сверху).")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Неверный диапазон дат",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет доступа к CRM",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/events")
    public PageResponse<CrmSiteAnalyticsEventRowDto> events(
            @RequestParam Instant from,
            @RequestParam Instant to,
            @Parameter(hidden = true)
            @PageableDefault(size = 20, sort = "receivedAt", direction = org.springframework.data.domain.Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<CrmSiteAnalyticsEventRowDto> page = siteAnalyticsService.events(from, to, pageable);
        return PageResponse.fromPage(page);
    }
}
