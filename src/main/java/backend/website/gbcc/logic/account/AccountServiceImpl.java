package backend.website.gbcc.logic.account;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateGuestCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.UpdateCustomerAccountRequestDto;
import backend.website.gbcc.model.AccountRegistrationStatus;
import backend.website.gbcc.model.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final SecurityContextHelper securityContextHelper;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountResponseDto createAdmin(CreateAdminAccountRequestDto requestDto) {
        String passwordHash = passwordEncoder.encode(requestDto.password());
        CreateAdminAccountRequestDto normalizedRequest = new CreateAdminAccountRequestDto(
                normalizeRequired(requestDto.name(), "name"),
                normalizeEmail(requestDto.email()),
                requestDto.password()
        );

        AccountEntity entity = accountMapper.toAdminEntity(normalizedRequest, passwordHash);
        return accountMapper.toResponse(saveAccount(entity));
    }

    @Override
    @Transactional
    public AccountResponseDto createGuestCustomer(CreateGuestCustomerAccountRequestDto requestDto) {
        AccountEntity entity = new AccountEntity();
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setPhone(normalizeRequired(requestDto.phone(), "phone"));
        entity.setEmail(normalizeEmail(requestDto.email()));
        entity.setRole(AccountRole.CUSTOMER);
        entity.setRegistrationStatus(AccountRegistrationStatus.PENDING);
        entity.setIsPasswordSet(Boolean.FALSE);
        entity.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        entity.setIsBlocked(Boolean.FALSE);
        return accountMapper.toResponse(saveAccount(entity));
    }

    @Override
    @Transactional
    public AccountResponseDto updateCustomer(UUID accountId, UpdateCustomerAccountRequestDto requestDto) {
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setPhone(normalizeRequired(requestDto.phone(), "phone"));
        entity.setEmail(normalizeEmail(requestDto.email()));
        return accountMapper.toResponse(saveAccount(entity));
    }

    @Override
    @Transactional
    public AccountResponseDto activateCustomer(UUID accountId, ActivateCustomerAccountRequestDto requestDto) {
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);

        if (Boolean.TRUE.equals(entity.getIsBlocked())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is blocked");
        }

        entity.setPasswordHash(passwordEncoder.encode(requestDto.password()));
        entity.setIsPasswordSet(Boolean.TRUE);
        entity.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        return accountMapper.toResponse(saveAccount(entity));
    }

    @Override
    @Transactional
    public void deleteCustomer(UUID accountId) {
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);
        accountRepository.delete(entity);
    }

    @Override
    @Transactional
    public void ensureOwnerExists(String email, String rawPassword) {
        if (accountRepository.existsByRole(AccountRole.OWNER)) {
            return;
        }

        String normalizedEmail = normalizeEmail(email);
        if (!StringUtils.hasText(rawPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "owner password is required");
        }
        String passwordHash = passwordEncoder.encode(rawPassword);

        AccountEntity entity = accountMapper.toOwnerEntity(normalizedEmail, passwordHash);
        saveAccount(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getById(UUID accountId) {
        UUID requesterAccountId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity requester = findByIdOrThrow(requesterAccountId);
        AccountEntity target = findByIdOrThrow(accountId);
        validateReadAccess(requester, target);
        return accountMapper.toResponse(target);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponseDto> search(AccountSearchRequestDto requestDto, Pageable pageable) {
        UUID requesterAccountId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity requester = findByIdOrThrow(requesterAccountId);
        AccountSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new AccountSearchRequestDto(null, null, null, null, null);

        Specification<AccountEntity> spec = AccountSpecification.byFilter(safeRequest);
        if (requester.getRole() == AccountRole.ADMIN) {
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.equal(root.get("role"), AccountRole.CUSTOMER),
                    cb.equal(root.get("id"), requester.getId())
            ));
        }

        return accountRepository.findAll(spec, pageable)
                .map(accountMapper::toResponse);
    }

    @Override
    @Transactional
    public void block(UUID accountId) {
        AccountEntity account = findByIdOrThrow(accountId);
        account.setIsBlocked(Boolean.TRUE);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void unblock(UUID accountId) {
        AccountEntity account = findByIdOrThrow(accountId);
        account.setIsBlocked(Boolean.FALSE);
        accountRepository.save(account);
    }

    private AccountEntity saveAccount(AccountEntity entity) {
        try {
            return accountRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account with this email or phone already exists");
        }
    }

    private AccountEntity findByIdOrThrow(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void validateReadAccess(AccountEntity requester, AccountEntity target) {
        if (requester.getRole() != AccountRole.ADMIN) {
            return;
        }

        if (target.getRole() == AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot access owner account");
        }

        if (target.getRole() == AccountRole.ADMIN && !target.getId().equals(requester.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot access other admin accounts");
        }
    }

    private String normalizeEmail(String email) {
        if (!StringUtils.hasText(email)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private void ensureCustomerOrThrow(AccountEntity account) {
        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not customer");
        }
    }
}
