package backend.website.gbcc.helper;

import backend.website.gbcc.config.property.JwtProperty;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class AuthCookieHelperTest {

    private JwtProperty jwtProperty;
    private AuthCookieHelper authCookieHelper;

    @BeforeEach
    void setUp() {
        jwtProperty = new JwtProperty();
        jwtProperty.setCookieSecure(false);
        jwtProperty.setCookieSameSite("Lax");
        jwtProperty.setAccessTokenTtlMinutes(15);
        jwtProperty.setRefreshTokenTtlDays(30);
        authCookieHelper = new AuthCookieHelper(jwtProperty);
    }

    @Test
    void addAccessCookie_shouldSetHttpOnlyCookieHeader() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        authCookieHelper.addAccessCookie(response, "token-value");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(Mockito.eq("Set-Cookie"), captor.capture());
        assertThat(captor.getValue()).contains(AuthCookieHelper.ACCESS_COOKIE);
        assertThat(captor.getValue()).contains("HttpOnly");
        assertThat(captor.getValue()).contains("token-value");
    }

    @Test
    void addRefreshCookie_shouldUseAuthPath() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        authCookieHelper.addRefreshCookie(response, "refresh-value");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(response).addHeader(Mockito.eq("Set-Cookie"), captor.capture());
        assertThat(captor.getValue()).contains(AuthCookieHelper.REFRESH_COOKIE);
        assertThat(captor.getValue()).contains("Path=/auth");
    }

    @Test
    void clearAuthCookies_shouldSendTwoSetCookieHeaders() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        authCookieHelper.clearAuthCookies(response);

        verify(response, Mockito.times(2)).addHeader(Mockito.eq("Set-Cookie"), Mockito.anyString());
    }
}
