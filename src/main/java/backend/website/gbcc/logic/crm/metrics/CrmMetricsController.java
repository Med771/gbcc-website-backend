package backend.website.gbcc.logic.crm.metrics;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.crm.metrics.dto.CrmMetricsSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/crm/metrics")
@RequiredArgsConstructor
@Tag(name = "CRM — Metrics")
public class CrmMetricsController {

    private final CrmMetricsService metricsService;

    @Operation(summary = "Сводные метрики CRM (в рамках скоупа ADMIN/OWNER)")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @GetMapping("/summary")
    public CrmMetricsSummaryDto summary() {
        return metricsService.summary();
    }
}
