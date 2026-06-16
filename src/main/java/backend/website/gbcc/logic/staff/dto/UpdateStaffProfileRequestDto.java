package backend.website.gbcc.logic.staff.dto;

import jakarta.validation.constraints.Size;

public record UpdateStaffProfileRequestDto(
        @Size(max = 255) String positionTitle,
        @Size(max = 512) String socialLink,
        String bankAccountDetails,
        @Size(max = 64) String phone
) {
}
