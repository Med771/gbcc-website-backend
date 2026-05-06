package backend.website.gbcc.logic.crm.map.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmCompanyObjectResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String name,
        double latitude,
        double longitude,
        String commentText
) {
}
