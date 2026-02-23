package backend.website.gbcc.logic.auth;

import backend.website.gbcc.helper.JwtTokenHelper;
import backend.website.gbcc.helper.TokenHashHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.auth.dto.AuthLoginRequestDto;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AccountRepository accountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenHelper jwtTokenHelper;
    private final PasswordEncoder passwordEncoder;
    private final TokenHashHelper tokenHashHelper;

    @Override
    @Transactional
    public AuthSessionDto login(AuthLoginRequestDto requestDto) {
        String email = normalizeEmail(requestDto.email());
        AccountEntity account = accountRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (Boolean.TRUE.equals(account.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is blocked");
        }

        if (account.getRegistrationStatus() != AccountRegistrationStatus.ACTIVE
                || !Boolean.TRUE.equals(account.getIsPasswordSet())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account registration is not completed");
        }

        if (!passwordEncoder.matches(requestDto.password(), account.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        return issueTokens(account);
    }

    @Override
    @Transactional
    public AuthSessionDto refresh(String refreshTokenRaw) {
        if (!StringUtils.hasText(refreshTokenRaw)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token is required");
        }
        String tokenHash = tokenHashHelper.sha256(refreshTokenRaw.trim());

        RefreshTokenEntity refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked()) || refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired or revoked");
        }

        AccountEntity account = refreshToken.getAccount();
        if (Boolean.TRUE.equals(account.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is blocked");
        }

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        return issueTokens(account);
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            return;
        }

        String tokenHash = tokenHashHelper.sha256(refreshToken.trim());
        refreshTokenRepository.findByTokenHash(tokenHash)
                .ifPresent(token -> {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                });
    }

    private AuthSessionDto issueTokens(AccountEntity account) {
        Instant now = Instant.now();
        Instant accessExpiresAt = jwtTokenHelper.getAccessTokenExpiresAt(now);
        Instant refreshExpiresAt = jwtTokenHelper.getRefreshTokenExpiresAt(now);

        String accessToken = jwtTokenHelper.createAccessToken(account.getId(), account.getRole());
        String refreshTokenValue = generateRefreshToken();
        String refreshTokenHash = tokenHashHelper.sha256(refreshTokenValue);

        RefreshTokenEntity refreshToken = new RefreshTokenEntity();
        refreshToken.setAccount(account);
        refreshToken.setTokenHash(refreshTokenHash);
        refreshToken.setExpiresAt(refreshExpiresAt);
        refreshToken.setRevoked(false);
        refreshTokenRepository.save(refreshToken);

        return new AuthSessionDto(
                account.getId(),
                account.getRole(),
                accessToken,
                accessExpiresAt,
                refreshTokenValue,
                refreshExpiresAt
        );
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String generateRefreshToken() {
        return UUID.randomUUID().toString().replace("-", "")
                + Long.toHexString(ThreadLocalRandom.current().nextLong())
                + UUID.randomUUID().toString().replace("-", "");
    }
}
