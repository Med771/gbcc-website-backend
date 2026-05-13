package backend.website.gbcc.logic.account;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.RegisterCustomerAccountRequestDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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

    @Mock
    private ReferralService referralService;

    @InjectMocks
    private AccountServiceImpl accountService;

    @Test
    void createAdmin_shouldHashPasswordAndNormalizeEmail() {
        UUID ownerId = UUID.randomUUID();
        AccountEntity owner = new AccountEntity();
        owner.setId(ownerId);
        owner.setRole(AccountRole.OWNER);
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(ownerId);
        when(accountRepository.findById(ownerId)).thenReturn(Optional.of(owner));

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
                "",
                null,
                "Admin",
                null,
                "test@mail.com",
                AccountRole.ADMIN,
                AccountRegistrationStatus.ACTIVE,
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
    void createAdmin_shouldThrowForbidden_whenActorIsNotOwner() {
        UUID adminId = UUID.randomUUID();
        AccountEntity admin = new AccountEntity();
        admin.setId(adminId);
        admin.setRole(AccountRole.ADMIN);
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(adminId);
        when(accountRepository.findById(adminId)).thenReturn(Optional.of(admin));

        CreateAdminAccountRequestDto request = new CreateAdminAccountRequestDto(
                "Admin",
                "Test@Mail.COM",
                "Password123"
        );

        assertThatThrownBy(() -> accountService.createAdmin(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(FORBIDDEN.value());
                });
        verify(accountMapper, never()).toAdminEntity(any(), any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void search_shouldThrowForbidden_whenRequesterIsCustomer() {
        UUID customerId = UUID.randomUUID();
        AccountEntity customer = new AccountEntity();
        customer.setId(customerId);
        customer.setRole(AccountRole.CUSTOMER);
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(customerId);
        when(accountRepository.findById(customerId)).thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> accountService.search(null, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(FORBIDDEN.value());
                });
    }

    @Test
    void activateCustomer_shouldThrowForbidden_whenCustomerActivatesAnother() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AccountEntity requester = new AccountEntity();
        requester.setId(requesterId);
        requester.setRole(AccountRole.CUSTOMER);
        AccountEntity target = new AccountEntity();
        target.setId(targetId);
        target.setRole(AccountRole.CUSTOMER);
        target.setIsBlocked(false);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(requesterId);
        when(accountRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> accountService.activateCustomer(
                targetId,
                new ActivateCustomerAccountRequestDto("Password123")
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(FORBIDDEN.value()));
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void deleteCustomer_shouldThrowForbidden_whenCustomerDeletesAnother() {
        UUID requesterId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        AccountEntity requester = new AccountEntity();
        requester.setId(requesterId);
        requester.setRole(AccountRole.CUSTOMER);
        AccountEntity target = new AccountEntity();
        target.setId(targetId);
        target.setRole(AccountRole.CUSTOMER);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(requesterId);
        when(accountRepository.findById(requesterId)).thenReturn(Optional.of(requester));
        when(accountRepository.findById(targetId)).thenReturn(Optional.of(target));

        assertThatThrownBy(() -> accountService.deleteCustomer(targetId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(FORBIDDEN.value()));
        verify(accountRepository, never()).delete(org.mockito.ArgumentMatchers.<AccountEntity>any());
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
        UUID moderatorId = UUID.randomUUID();
        UUID accountId = UUID.randomUUID();
        AccountEntity moderator = new AccountEntity();
        moderator.setId(moderatorId);
        moderator.setRole(AccountRole.ADMIN);
        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);
        entity.setRole(AccountRole.CUSTOMER);
        entity.setIsBlocked(false);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(moderatorId);
        when(accountRepository.findById(moderatorId)).thenReturn(Optional.of(moderator));
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

    @Test
    void registerCustomer_shouldThrowConflict_whenEmailOrPhoneAlreadyExists() {
        RegisterCustomerAccountRequestDto request = new RegisterCustomerAccountRequestDto(
                "Customer",
                "Customer",
                null,
                "+123456789",
                "dup@mail.com",
                "Password123",
                null
        );

        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("$2a$hash");
        doThrow(new DuplicateKeyException("unique violation"))
                .when(accountRepository).save(org.mockito.ArgumentMatchers.any(AccountEntity.class));

        assertThatThrownBy(() -> accountService.registerCustomer(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(CONFLICT.value());
                    assertThat(ex.getReason()).contains("already exists");
                });
    }

    @Test
    void createAdmin_shouldThrowConflict_whenEmailAlreadyExists() {
        UUID ownerId = UUID.randomUUID();
        AccountEntity owner = new AccountEntity();
        owner.setId(ownerId);
        owner.setRole(AccountRole.OWNER);
        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(ownerId);
        when(accountRepository.findById(ownerId)).thenReturn(Optional.of(owner));

        CreateAdminAccountRequestDto request = new CreateAdminAccountRequestDto(
                "Admin",
                "dup@mail.com",
                "Password123"
        );

        AccountEntity mappedEntity = new AccountEntity();
        mappedEntity.setEmail("dup@mail.com");
        mappedEntity.setRole(AccountRole.ADMIN);

        when(accountMapper.toAdminEntity(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.anyString()))
                .thenReturn(mappedEntity);
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$hash");
        doThrow(new DuplicateKeyException("unique violation"))
                .when(accountRepository).save(mappedEntity);

        assertThatThrownBy(() -> accountService.createAdmin(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(CONFLICT.value()));
    }

    @Test
    void registerCustomer_shouldCreateActiveCustomerWithPasswordAccess() {
        RegisterCustomerAccountRequestDto request = new RegisterCustomerAccountRequestDto(
                "Customer",
                "Customer",
                null,
                "+123456789",
                "CUSTOMER@mail.com",
                "Password123",
                null
        );
        AccountEntity savedEntity = new AccountEntity();
        savedEntity.setId(UUID.randomUUID());
        savedEntity.setFirstName("Customer");
        savedEntity.setLastName("Customer");
        savedEntity.setName("Customer Customer");
        savedEntity.setPhone("+123456789");
        savedEntity.setEmail("customer@mail.com");
        savedEntity.setRole(AccountRole.CUSTOMER);
        savedEntity.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        savedEntity.setIsPasswordSet(true);

        AccountResponseDto responseDto = new AccountResponseDto(
                savedEntity.getId(),
                savedEntity.getFirstName(),
                savedEntity.getLastName(),
                null,
                savedEntity.getName(),
                savedEntity.getPhone(),
                savedEntity.getEmail(),
                savedEntity.getRole(),
                savedEntity.getRegistrationStatus(),
                false,
                Instant.now(),
                Instant.now()
        );

        when(accountRepository.save(org.mockito.ArgumentMatchers.any(AccountEntity.class))).thenReturn(savedEntity);
        when(accountMapper.toResponse(savedEntity)).thenReturn(responseDto);
        when(passwordEncoder.encode(org.mockito.ArgumentMatchers.anyString())).thenReturn("$2a$hash");

        AccountResponseDto result = accountService.registerCustomer(request);

        verify(referralService).bindInviterForNewCustomer(any(AccountEntity.class), eq(null));
        verify(referralService).assignReferralCodeIfMissing(savedEntity.getId());

        assertThat(result.role()).isEqualTo(AccountRole.CUSTOMER);
        assertThat(result.registrationStatus()).isEqualTo(AccountRegistrationStatus.ACTIVE);
        assertThat(result.email()).isEqualTo("customer@mail.com");
    }

    @Test
    void activateCustomer_shouldSetPasswordAndActiveStatus() {
        UUID accountId = UUID.randomUUID();
        AccountEntity entity = new AccountEntity();
        entity.setId(accountId);
        entity.setRole(AccountRole.CUSTOMER);
        entity.setIsBlocked(false);
        entity.setRegistrationStatus(AccountRegistrationStatus.PENDING);
        entity.setIsPasswordSet(false);

        AccountResponseDto responseDto = new AccountResponseDto(
                accountId,
                "Customer",
                "",
                null,
                "Customer",
                "+123456789",
                "customer@mail.com",
                AccountRole.CUSTOMER,
                AccountRegistrationStatus.ACTIVE,
                false,
                Instant.now(),
                Instant.now()
        );

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));
        when(passwordEncoder.encode("Password123")).thenReturn("$2a$new");
        when(accountRepository.save(entity)).thenReturn(entity);
        when(accountMapper.toResponse(entity)).thenReturn(responseDto);

        when(securityContextHelper.getCurrentAccountIdOrThrow()).thenReturn(accountId);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(entity));

        AccountResponseDto result = accountService.activateCustomer(
                accountId,
                new ActivateCustomerAccountRequestDto("Password123")
        );

        assertThat(entity.getIsPasswordSet()).isTrue();
        assertThat(entity.getRegistrationStatus()).isEqualTo(AccountRegistrationStatus.ACTIVE);
        assertThat(entity.getPasswordHash()).isEqualTo("$2a$new");
        assertThat(result.registrationStatus()).isEqualTo(AccountRegistrationStatus.ACTIVE);
    }
}
