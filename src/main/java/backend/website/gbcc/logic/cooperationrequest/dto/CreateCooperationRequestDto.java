package backend.website.gbcc.logic.cooperationrequest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCooperationRequestDto(
        @NotBlank(message = "name is required")
        @Size(max = 255)
        String name,
        @NotBlank(message = "phone is required")
        @Size(max = 64)
        String phone,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 255)
        String email,
        @Size(max = 128)
        String cooperationType,
        String comment,
        UUID attachmentFileId,
        @AssertTrue(message = "consent to personal data processing is required")
        Boolean consentProcessing
) {
}
