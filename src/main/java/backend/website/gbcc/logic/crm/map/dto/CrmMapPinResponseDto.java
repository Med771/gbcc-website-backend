package backend.website.gbcc.logic.crm.map.dto;

import java.util.UUID;

public record CrmMapPinResponseDto(
        CrmMapPinType type,
        UUID id,
        double latitude,
        double longitude,
        String title,
        String summary,
        String statusLabel
) {
}
