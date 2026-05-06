package backend.website.gbcc.logic.crm.metrics.dto;

import java.util.Map;

public record CrmMetricsSummaryDto(
        long organizations,
        long leads,
        Map<String, Long> leadsByStatus,
        long openTasks,
        long overdueTasks,
        long supplies
) {
}
