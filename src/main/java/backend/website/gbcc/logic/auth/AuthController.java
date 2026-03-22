package backend.website.gbcc.logic.auth;

import backend.website.gbcc.helper.AuthCookieHelper;
import backend.website.gbcc.logic.auth.dto.AuthLoginRequestDto;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;
import backend.website.gbcc.logic.auth.dto.AuthTokenResponseDto;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Вход по email/паролю, обновление сессии, выход. Access/refresh токены выставляются в HttpOnly cookies.")
public class AuthController {

    private final AuthService authService;
    private final AuthCookieHelper authCookieHelper;

    @Operation(
            summary = "Вход в систему",
            description = """
                    Проверяет email и пароль. При успехе устанавливает cookies:
                    `GBCC_ACCESS_TOKEN` (path `/`) и `GBCC_REFRESH_TOKEN` (path `/auth`).
                    В теле ответа — accountId, роль и время истечения токенов (для отладки/UI).
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный вход",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthTokenResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации тела запроса",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Неверные учётные данные",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Аккаунт заблокирован или регистрация не завершена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/login")
    public AuthTokenResponseDto login(
            @Valid @RequestBody AuthLoginRequestDto requestDto,
            HttpServletResponse response
    ) {
        AuthSessionDto session = authService.login(requestDto);
        authCookieHelper.addAccessCookie(response, session.accessToken());
        authCookieHelper.addRefreshCookie(response, session.refreshToken());
        return toResponse(session);
    }

    @Operation(
            summary = "Обновление access-токена",
            description = """
                    Читает refresh-токен из cookie `GBCC_REFRESH_TOKEN` (запрос на `/auth/refresh` должен отправлять этот cookie).
                    Выдаёт новую пару access+refresh и ротирует refresh в БД.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Новая сессия",
                    content = @Content(schema = @Schema(implementation = AuthTokenResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Refresh отсутствует, недействителен или истёк",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Аккаунт заблокирован",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/refresh")
    public AuthTokenResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookieValue(request);
        AuthSessionDto session = authService.refresh(refreshToken);
        authCookieHelper.addAccessCookie(response, session.accessToken());
        authCookieHelper.addRefreshCookie(response, session.refreshToken());
        return toResponse(session);
    }

    @Operation(
            summary = "Выход",
            description = "Отзывает refresh-токен в БД и очищает cookies авторизации."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Выход выполнен (тело пустое)"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookieValue(request);
        authService.logout(refreshToken);
        authCookieHelper.clearAuthCookies(response);
    }

    private AuthTokenResponseDto toResponse(AuthSessionDto session) {
        return new AuthTokenResponseDto(
                session.accountId(),
                session.role(),
                session.accessTokenExpiresAt(),
                session.refreshTokenExpiresAt()
        );
    }

    private String readCookieValue(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthCookieHelper.REFRESH_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
