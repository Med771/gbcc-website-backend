package backend.website.gbcc.logic.contactrequest.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateContactRequestDto(
        @NotBlank(message = "name is required")
        @Size(max = 255)
        String name,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        @Size(max = 255)
        String email,
        @Size(max = 64)
        String phone,
        @NotBlank(message = "message is required")
        String message,
        @AssertTrue(message = "consent to personal data processing is required")
        Boolean consentProcessing
) {
}
