package backend.website.gbcc.logic.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterCustomerAccountRequestDto(
        @NotBlank(message = "name is required")
        String name,
        @NotBlank(message = "phone is required")
        String phone,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must contain at least 8 characters")
        String password
) {
}
