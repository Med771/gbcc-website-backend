package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.helper.AuthCookieHelper;
import backend.website.gbcc.logic.auth.AuthController;
import backend.website.gbcc.logic.auth.AuthService;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = AuthController.class)
class AuthControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private AuthCookieHelper authCookieHelper;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void login_returns200() throws Exception {
        when(authService.login(any())).thenReturn(new AuthSessionDto(
                UUID_1,
                AccountRole.CUSTOMER,
                "access",
                Instant.parse("2025-01-01T00:00:00Z"),
                "refresh",
                Instant.parse("2025-02-01T00:00:00Z")
        ));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"a@b.com\",\"password\":\"password12\"}"))
                .andExpect(status().isOk());

        verify(authCookieHelper).addAccessCookie(any(), anyString());
        verify(authCookieHelper).addRefreshCookie(any(), anyString());
    }

    @Test
    void refresh_returns200_whenCookiePresent() throws Exception {
        when(authService.refresh(anyString())).thenReturn(new AuthSessionDto(
                UUID_1,
                AccountRole.CUSTOMER,
                "access",
                Instant.parse("2025-01-01T00:00:00Z"),
                "refresh",
                Instant.parse("2025-02-01T00:00:00Z")
        ));

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new jakarta.servlet.http.Cookie(AuthCookieHelper.REFRESH_COOKIE, "rt")))
                .andExpect(status().isOk());
    }

    @Test
    void logout_returns204() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new jakarta.servlet.http.Cookie(AuthCookieHelper.REFRESH_COOKIE, "rt")))
                .andExpect(status().isNoContent());

        verify(authService).logout(anyString());
        verify(authCookieHelper).clearAuthCookies(any());
    }
}
