package backend.website.gbcc.logic.account;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
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
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private SecurityContextHelper securityContextHelper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAdmin_shouldHashPasswordAndNormalizeEmail() {
        CreateAdminAccountRequestDto request = new CreateAdminAccountRequestDto(
                "Admin",
                "Test@Mail.COM",
                "Password123"
        );

        AccountEntity mappedEntity = new AccountEntity();
        mappedEntity.setEmail("test@mail.com");
        mappedEntity.setRole(AccountRole.ADMIN);

        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setEmail("test@mail.com");
        savedEntity.setRole(AccountRole.ADMIN);
        savedEntity.setIsBlocked(false);

        AccountResponseDto responseDto = new AccountResponseDto(
                savedEntity.getId(),
                "Admin",
                null,
                "test@mail.com",
                AccountRole.ADMIN,
                false,
                Instant.now(),
                Instant.now()
        );

        when(accountMapper.toAdminEntity(
                org.mockito.ArgumentMatchers.any(CreateAdminAccountRequestDto.class),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(mappedEntity);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hash");
        when(accountRepository.save(mappedEntity)).thenReturn(savedEntity);
        when(accountMapper.toResponse(savedEntity)).thenReturn(responseDto);

        AccountResponseDto result = accountService.createAdmin(request);

        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(accountMapper).toAdminEntity(
                org.mockito.ArgumentMatchers.any(CreateAdminAccountRequestDto.class),
                hashCaptor.capture()
        );
        assertThat(hashCaptor.getValue()).startsWith("$2");
        assertThat(result.email()).isEqualTo("test@mail.com");
    }

    @Test
    void ensureOwnerExists_shouldDoNothing_whenOwnerAlreadyExists() {
        when(accountRepository.existsByRole(AccountRole.OWNER)).thenReturn(true);

        accountService.ensureOwnerExists("owner@mail.com", "Password123");
    }

    @Test
    void getById_shouldThrowNotFound_whenAccountMissing() {
        UUID requesterId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AccountEntity requester = new AccountEntity();
        requester.setId(requesterId);
        requester.setRole(AccountRole.OWNER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(requesterId);
        when(accountRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> accountService.getById(accountId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
                    assertThat(ex.getReason()).isEqualTo("Account not found");
                });
    }

    @Test
    void block_shouldMarkAccountAsBlocked() {
        UUID accountId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);
        entity.setIsBlocked(false);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));

        accountService.block(accountId);

        assertThat(entity.getIsBlocked()).isTrue();
        verify(accountRepository).save(entity);
    }

    @Test
    void ensureOwnerExists_shouldCreateOwner_whenMissing() {
        when(accountRepository.existsByRole(AccountRole.OWNER)).thenReturn(false);

        AccountEntity mappedEntity = new AccountEntity();
        mappedEntity.setEmail("owner@mail.com");
        mappedEntity.setRole(AccountRole.OWNER);

        when(accountMapper.toOwnerEntity(
                org.mockito.ArgumentMatchers.eq("owner@mail.com"),
                org.mockito.ArgumentMatchers.anyString()
        )).thenReturn(mappedEntity);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hash");
        when(accountRepository.save(mappedEntity)).thenReturn(mappedEntity);

        accountService.ensureOwnerExists("OWNER@MAIL.COM", "Password123");

        verify(accountMapper).toOwnerEntity(
                org.mockito.ArgumentMatchers.eq("owner@mail.com"),
                org.mockito.ArgumentMatchers.anyString()
        );
        verify(accountRepository).save(mappedEntity);
    }

    @Test
    void getById_shouldThrowForbidden_whenAdminReadsOwner() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();

        AccountEntity requester = new AccountEntity();
        requester.setId(requesterId);
        requester.setRole(AccountRole.ADMIN);

        AccountEntity target = new AccountEntity();
        target.setId(targetId);
        target.setRole(AccountRole.OWNER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(requesterId);
        when(accountRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> accountService.getById(targetId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(403);
                    assertThat(ex.getReason()).isEqualTo("Admin cannot access owner account");
                });
    }
}
