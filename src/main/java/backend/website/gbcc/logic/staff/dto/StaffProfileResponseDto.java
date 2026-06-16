package backend.website.gbcc.logic.staff.dto;

import java.util.UUID;

public record StaffProfileResponseDto(
        UUID accountId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String positionTitle,
        String socialLink,
        String bankAccountDetails,
        String displayId
) {
}
