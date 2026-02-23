package backend.website.gbcc.helper;

import backend.website.gbcc.config.property.JwtProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

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

    private SecretKey getSigningKey() {
        if (!StringUtils.hasText(jwtProperty.getSecret()) || jwtProperty.getSecret().length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be configured with length >= 32");
        }
        return Keys.hmacShaKeyFor(jwtProperty.getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
