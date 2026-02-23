package backend.website.gbcc.helper;

import backend.website.gbcc.config.property.JwtProperty;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthCookieHelper {

    public static final String ACCESS_COOKIE = "GBCC_ACCESS_TOKEN";
    public static final String REFRESH_COOKIE = "GBCC_REFRESH_TOKEN";

    private final JwtProperty jwtProperty;

    public AuthCookieHelper(JwtProperty jwtProperty) {
        this.jwtProperty = jwtProperty;
    }

    public void addAccessCookie(HttpServletResponse response, String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_COOKIE, accessToken)
                .httpOnly(true)
                .secure(jwtProperty.isCookieSecure())
                .sameSite(jwtProperty.getCookieSameSite())
                .path("/")
                .maxAge(Duration.ofMinutes(jwtProperty.getAccessTokenTtlMinutes()))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, refreshToken)
                .httpOnly(true)
                .secure(jwtProperty.isCookieSecure())
                .sameSite(jwtProperty.getCookieSameSite())
                .path("/auth")
                .maxAge(Duration.ofDays(jwtProperty.getRefreshTokenTtlDays()))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearAuthCookies(HttpServletResponse response) {
        ResponseCookie access = ResponseCookie.from(ACCESS_COOKIE, "")
                .httpOnly(true)
                .secure(jwtProperty.isCookieSecure())
                .sameSite(jwtProperty.getCookieSameSite())
                .path("/")
                .maxAge(Duration.ZERO)
                .build();
        ResponseCookie refresh = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(jwtProperty.isCookieSecure())
                .sameSite(jwtProperty.getCookieSameSite())
                .path("/auth")
                .maxAge(Duration.ZERO)
                .build();
        response.addHeader("Set-Cookie", access.toString());
        response.addHeader("Set-Cookie", refresh.toString());
    }
}
