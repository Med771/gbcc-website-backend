package backend.website.gbcc.logic.customeranalytics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CustomerAnalyticsResponseDto(
        UUID accountId,
        BigDecimal manualTotalPaid,
        BigDecimal manualCurrentMonthPaid,
        Integer manualTotalOrdersCount,
        Integer manualReferredClientsCount,
        BigDecimal computedTotalPaid,
        BigDecimal computedCurrentMonthPaid,
        Long computedTotalOrdersCount,
        Long computedReferredClientsCount,
        Instant updatedAt,
        UUID updatedByAccountId
) {
}
