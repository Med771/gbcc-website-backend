package backend.website.gbcc.logic.account;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.RegisterCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.UpdateCustomerAccountRequestDto;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@Validated
@Tag(name = "Accounts", description = "Регистрация клиента (публично), управление аккаунтами с JWT (cookie). Правила доступа в сервисном слое.")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Создать аккаунт администратора", description = "Требует авторизации. Обычно для первичного заведения админов.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Админ создан",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректные данные",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Не авторизован",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email или телефон уже заняты",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto createAdmin(@Valid @RequestBody CreateAdminAccountRequestDto requestDto) {
        return accountService.createAdmin(requestDto);
    }

    @Operation(summary = "Регистрация клиента", description = "Публичный endpoint. Создаёт CUSTOMER с активным статусом и паролем.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Клиент зарегистрирован",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт уникальности email/телефона",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/customer/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDto registerCustomer(@Valid @RequestBody RegisterCustomerAccountRequestDto requestDto) {
        return accountService.registerCustomer(requestDto);
    }

    @Operation(
            summary = "Текущий профиль (личный кабинет)",
            description = "JWT. Данные аккаунта из токена: имя, фамилия, отчество, контакты. Пароль в ответе не возвращается."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Профиль",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/me")
    public AccountResponseDto getMyProfile() {
        return accountService.getMyProfile();
    }

    @Operation(
            summary = "Обновить свой профиль",
            description = "JWT. Только роль CUSTOMER. Поле password опционально: если не передать или пусто — пароль не меняется."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Не клиент или валидация",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/me")
    public AccountResponseDto updateMyProfile(@Valid @RequestBody UpdateCustomerAccountRequestDto requestDto) {
        return accountService.updateMyProfile(requestDto);
    }

    @Operation(summary = "Обновить данные клиента", description = "JWT. Только для роли CUSTOMER по указанному accountId (см. правила в сервисе).")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверная роль или данные",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Аккаунт не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт уникальности",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/customer/{accountId}")
    public AccountResponseDto updateCustomer(
            @Parameter(description = "UUID аккаунта") @PathVariable UUID accountId,
            @Valid @RequestBody UpdateCustomerAccountRequestDto requestDto
    ) {
        return accountService.updateCustomer(accountId, requestDto);
    }

    @Operation(summary = "Активация клиента (установка пароля)", description = "JWT. Завершает регистрацию, если применимо.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Активирован",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Неверные данные или роль",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Аккаунт заблокирован",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/customer/{accountId}/activate")
    public AccountResponseDto activateCustomer(
            @PathVariable UUID accountId,
            @Valid @RequestBody ActivateCustomerAccountRequestDto requestDto
    ) {
        return accountService.activateCustomer(accountId, requestDto);
    }

    @Operation(summary = "Удалить клиента", description = "JWT. Удаление записи CUSTOMER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалено"),
            @ApiResponse(responseCode = "400", description = "Не клиент",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping("/customer/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCustomer(@PathVariable UUID accountId) {
        accountService.deleteCustomer(accountId);
    }

    @Operation(summary = "Получить аккаунт по id", description = "JWT. Доступ ограничен правилами (админ видит не всех).")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найден",
                    content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Доступ запрещён",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{accountId}")
    public AccountResponseDto getById(@PathVariable UUID accountId) {
        return accountService.getById(accountId);
    }

    @Operation(
            summary = "Поиск аккаунтов",
            description = "JWT. Фильтры: имя, телефон, email, роль, признак блокировки. Пагинация Spring: page, size, sort."
    )
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Страница результатов",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping
    public PageResponse<AccountResponseDto> search(
            @Parameter(description = "Часть имени") @RequestParam(required = false) String name,
            @Parameter(description = "Телефон") @RequestParam(required = false) String phone,
            @Parameter(description = "Email") @RequestParam(required = false) String email,
            @Parameter(description = "Роль") @RequestParam(required = false) AccountRole role,
            @Parameter(description = "Заблокирован") @RequestParam(required = false) Boolean isBlocked,
            @Parameter(hidden = true) Pageable pageable
    ) {
        AccountSearchRequestDto requestDto = new AccountSearchRequestDto(name, phone, email, role, isBlocked);
        Page<AccountResponseDto> result = accountService.search(requestDto, pageable);
        return PageResponse.fromPage(result);
    }

    @Operation(summary = "Заблокировать аккаунт", description = "JWT. Устанавливает isBlocked=true.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Заблокирован"),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{accountId}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@PathVariable UUID accountId) {
        accountService.block(accountId);
    }

    @Operation(summary = "Разблокировать аккаунт", description = "JWT.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Разблокирован"),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{accountId}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable UUID accountId) {
        accountService.unblock(accountId);
    }
}
