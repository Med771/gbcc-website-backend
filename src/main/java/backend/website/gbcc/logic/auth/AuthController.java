package backend.website.gbcc.logic.auth;

import backend.website.gbcc.helper.AuthCookieHelper;
import backend.website.gbcc.logic.auth.dto.AuthLoginRequestDto;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;
import backend.website.gbcc.logic.auth.dto.AuthTokenResponseDto;
import jakarta.validation.Valid;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthCookieHelper authCookieHelper;

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

    @PostMapping("/refresh")
    public AuthTokenResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = readCookieValue(request);
        AuthSessionDto session = authService.refresh(refreshToken);
        authCookieHelper.addAccessCookie(response, session.accessToken());
        authCookieHelper.addRefreshCookie(response, session.refreshToken());
        return toResponse(session);
    }

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
