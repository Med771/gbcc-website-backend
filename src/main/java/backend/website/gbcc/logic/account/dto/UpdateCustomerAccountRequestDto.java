package backend.website.gbcc.logic.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateCustomerAccountRequestDto(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "phone is required")
        String phone,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email
) {
}
