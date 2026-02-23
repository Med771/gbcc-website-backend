package backend.website.gbcc.logic.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivateCustomerAccountRequestDto(
        @NotBlank(message = "password is required")
        @Size(min = 8, message = "password must contain at least 8 characters")
        String password
) {
}
