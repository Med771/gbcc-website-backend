package backend.website.gbcc.helper;

import backend.website.gbcc.config.property.JwtProperty;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtTokenHelperTest {

    private JwtProperty jwtProperty;
    private JwtTokenHelper jwtTokenHelper;

    @BeforeEach
    void setUp() {
        jwtProperty = new JwtProperty();
        jwtProperty.setSecret("0123456789abcdef0123456789abcdef"); // 32 chars
        jwtProperty.setAccessTokenTtlMinutes(15);
        jwtProperty.setRefreshTokenTtlDays(30);
        jwtTokenHelper = new JwtTokenHelper(jwtProperty);
    }

    @Test
    void createAccessToken_shouldBeParseable() {
        UUID id = UUID.randomUUID();
        String token = jwtTokenHelper.createAccessToken(id, AccountRole.CUSTOMER);

        assertThat(jwtTokenHelper.parseClaims(token).getSubject()).isEqualTo(id.toString());
        assertThat(jwtTokenHelper.parseClaims(token).get("role", String.class)).isEqualTo("CUSTOMER");
    }

    @Test
    void getAccessTokenExpiresAt_shouldAddTtlMinutes() {
        Instant now = Instant.parse("2025-06-01T12:00:00Z");
        Instant exp = jwtTokenHelper.getAccessTokenExpiresAt(now);
        assertThat(exp).isEqualTo(now.plus(15, ChronoUnit.MINUTES));
    }

    @Test
    void getRefreshTokenExpiresAt_shouldAddTtlDays() {
        Instant now = Instant.parse("2025-06-01T12:00:00Z");
        Instant exp = jwtTokenHelper.getRefreshTokenExpiresAt(now);
        assertThat(exp).isEqualTo(now.plus(30, ChronoUnit.DAYS));
    }

    @Test
    void parseClaims_shouldFail_whenSecretTooShort() {
        jwtProperty.setSecret("short");
        JwtTokenHelper bad = new JwtTokenHelper(jwtProperty);

        assertThatThrownBy(() -> bad.createAccessToken(UUID.randomUUID(), AccountRole.ADMIN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("app.jwt.secret");
    }
}
