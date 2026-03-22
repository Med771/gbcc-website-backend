package backend.website.gbcc.logic.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCustomerAccountRequestDto(
        @NotBlank(message = "firstName is required")
        @Size(max = 255)
        String firstName,
        @NotBlank(message = "lastName is required")
        @Size(max = 255)
        String lastName,
        @Size(max = 255)
        String patronymic,
        @NotBlank(message = "phone is required")
        String phone,
        @NotBlank(message = "email is required")
        @Email(message = "email must be valid")
        String email,
        /**
         * Новый пароль. Пусто/null — не менять.
         */
        String password
) {
}
