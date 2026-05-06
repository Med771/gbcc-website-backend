package backend.website.gbcc.logic.crm.map.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateCrmCompanyObjectRequestDto(
        @NotBlank String name,
        double latitude,
        double longitude,
        String commentText
) {
}
