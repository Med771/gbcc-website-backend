package backend.website.gbcc.logic.auth;

import backend.website.gbcc.helper.JwtTokenHelper;
import backend.website.gbcc.helper.TokenHashHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.auth.dto.AuthLoginRequestDto;
import backend.website.gbcc.logic.auth.dto.AuthSessionDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenHelper jwtTokenHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenHashHelper tokenHashHelper;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void login_shouldReturnTokens_whenCredentialsValid() {
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID());
        account.setEmail("admin@mail.com");
        account.setPasswordHash("hash");
        account.setRole(AccountRole.ADMIN);
        account.setIsBlocked(false);
        account.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        account.setIsPasswordSet(true);

        Instant now = Instant.now();
        Instant accessExp = now.plusSeconds(900);
        Instant refreshExp = now.plusSeconds(86400);

        when(accountRepository.findByEmailIgnoreCase("admin@mail.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("Password123", "hash")).thenReturn(true);
        when(jwtTokenHelper.getAccessTokenExpiresAt(org.mockito.ArgumentMatchers.any(Instant.class))).thenReturn(accessExp);
        when(jwtTokenHelper.getRefreshTokenExpiresAt(org.mockito.ArgumentMatchers.any(Instant.class))).thenReturn(refreshExp);
        when(jwtTokenHelper.createAccessToken(account.getId(), AccountRole.ADMIN)).thenReturn("access-token");

        when(tokenHashHelper.sha256(org.mockito.ArgumentMatchers.anyString())).thenReturn("refresh-hash");

        AuthSessionDto response = authService.login(new AuthLoginRequestDto("ADMIN@mail.com", "Password123"));

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.accessTokenExpiresAt()).isEqualTo(accessExp);
        assertThat(response.refreshTokenExpiresAt()).isEqualTo(refreshExp);

        ArgumentCaptor<RefreshTokenEntity> refreshCaptor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenRepository).save(refreshCaptor.capture());
        assertThat(refreshCaptor.getValue().getAccount()).isEqualTo(account);
        assertThat(refreshCaptor.getValue().getTokenHash()).isEqualTo("refresh-hash");
        assertThat(refreshCaptor.getValue().getRevoked()).isFalse();
    }

    @Test
    void login_shouldThrowUnauthorized_whenPasswordInvalid() {
        AccountEntity account = new AccountEntity();
        account.setEmail("admin@mail.com");
        account.setPasswordHash("hash");
        account.setRole(AccountRole.ADMIN);
        account.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        account.setIsPasswordSet(true);

        when(accountRepository.findByEmailIgnoreCase("admin@mail.com")).thenReturn(Optional.of(account));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new AuthLoginRequestDto("admin@mail.com", "bad")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value()).isEqualTo(401));
    }

    @Test
    void refresh_shouldRotateRefreshToken() {
        AccountEntity account = new AccountEntity();
        account.setId(UUID.randomUUID());
        account.setRole(AccountRole.ADMIN);
        account.setIsBlocked(false);
        account.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        account.setIsPasswordSet(true);

        RefreshTokenEntity existing = new RefreshTokenEntity();
        existing.setTokenHash("refresh-hash-old");
        existing.setRevoked(false);
        existing.setExpiresAt(Instant.now().plusSeconds(3600));
        existing.setAccount(account);

        when(tokenHashHelper.sha256("refresh-old")).thenReturn("refresh-hash-old");
        when(tokenHashHelper.sha256(org.mockito.ArgumentMatchers.argThat(v -> !"refresh-old".equals(v))))
                .thenReturn("refresh-hash-new");
        when(refreshTokenRepository.findByTokenHash("refresh-hash-old")).thenReturn(Optional.of(existing));
        when(jwtTokenHelper.getAccessTokenExpiresAt(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(Instant.now().plusSeconds(900));
        when(jwtTokenHelper.getRefreshTokenExpiresAt(org.mockito.ArgumentMatchers.any(Instant.class)))
                .thenReturn(Instant.now().plusSeconds(86400));
        when(jwtTokenHelper.createAccessToken(account.getId(), AccountRole.ADMIN)).thenReturn("access-new");

        AuthSessionDto response = authService.refresh("refresh-old");

        assertThat(existing.getRevoked()).isTrue();
        assertThat(response.accessToken()).isEqualTo("access-new");
        assertThat(response.refreshToken()).isNotEqualTo("refresh-old");
        verify(refreshTokenRepository).save(existing);
    }

    @Test
    void login_shouldThrowForbidden_whenCustomerRegistrationPending() {
        AccountEntity account = new AccountEntity();
        account.setEmail("customer@mail.com");
        account.setPasswordHash("hash");
        account.setRole(AccountRole.CUSTOMER);
        account.setIsBlocked(false);
        account.setRegistrationStatus(AccountRegistrationStatus.PENDING);
        account.setIsPasswordSet(false);

        when(accountRepository.findByEmailIgnoreCase("customer@mail.com")).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> authService.login(new AuthLoginRequestDto("customer@mail.com", "Password123")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(403);
                    assertThat(ex.getReason()).isEqualTo("Account registration is not completed");
                });
    }
}
