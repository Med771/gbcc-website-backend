package backend.website.gbcc.helper;

import backend.website.gbcc.config.property.JwtProperty;
import backend.website.gbcc.model.AccountRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenHelper {

    private final JwtProperty jwtProperty;

    public JwtTokenHelper(JwtProperty jwtProperty) {
        this.jwtProperty = jwtProperty;
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String createAccessToken(UUID accountId, AccountRole role) {
        Instant now = Instant.now();
        Instant expiresAt = getAccessTokenExpiresAt(now);

        return Jwts.builder()
                .subject(accountId.toString())
                .claim("role", role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiresAt))
                .signWith(getSigningKey())
                .compact();
    }

    public Instant getAccessTokenExpiresAt(Instant now) {
        return now.plus(jwtProperty.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES);
    }

    public Instant getRefreshTokenExpiresAt(Instant now) {
        return now.plus(jwtProperty.getRefreshTokenTtlDays(), ChronoUnit.DAYS);
    }

    private SecretKey getSigningKey() {
        if (!StringUtils.hasText(jwtProperty.getSecret()) || jwtProperty.getSecret().length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be configured with length >= 32");
        }
        return Keys.hmacShaKeyFor(jwtProperty.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
