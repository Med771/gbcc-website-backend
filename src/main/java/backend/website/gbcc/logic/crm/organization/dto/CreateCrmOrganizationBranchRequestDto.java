package backend.website.gbcc.logic.crm.organization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCrmOrganizationBranchRequestDto(
        @NotBlank @Size(max = 512) String name,
        @NotBlank String address,
        Boolean isDefault
) {
}
