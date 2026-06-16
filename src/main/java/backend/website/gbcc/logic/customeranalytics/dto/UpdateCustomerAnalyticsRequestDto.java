package backend.website.gbcc.logic.customeranalytics.dto;

import java.math.BigDecimal;

public record UpdateCustomerAnalyticsRequestDto(
        BigDecimal manualTotalPaid,
        BigDecimal manualCurrentMonthPaid,
        Integer manualTotalOrdersCount,
        Integer manualReferredClientsCount
) {
}
