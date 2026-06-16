package backend.website.gbcc.logic.crm.organization.dto;

import jakarta.validation.constraints.Size;

public record CreateCrmOrganizationContactRequestDto(
        @Size(max = 255) String fullName,
        @Size(max = 255) String department,
        @Size(max = 255) String positionTitle,
        String socialLinks,
        @Size(max = 64) String phone,
        @Size(max = 255) String email,
        String extraNote
) {
}
