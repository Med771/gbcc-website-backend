package backend.website.gbcc.logic.account;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.dto.PatchCustomerManagerRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.customeranalytics.CustomerAnalyticsService;
import backend.website.gbcc.logic.customeranalytics.dto.CustomerAnalyticsResponseDto;
import backend.website.gbcc.logic.customeranalytics.dto.UpdateCustomerAnalyticsRequestDto;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.logic.staff.StaffProfileService;
import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.RegisterCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.UpdateCustomerAccountRequestDto;
import backend.website.gbcc.logic.staff.dto.StaffProfileResponseDto;
import backend.website.gbcc.logic.staff.dto.UpdateStaffProfileRequestDto;
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
    private final ReferralService referralService;
    private final StaffProfileService staffProfileService;
    private final CustomerAnalyticsService customerAnalyticsService;
    private final CrmOrganizationRepository crmOrganizationRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AccountResponseDto createAdmin(CreateAdminAccountRequestDto requestDto) {
        AccountEntity actor = findByIdOrThrow(securityContextHelper.getCurrentAccountIdOrThrow());
        if (actor.getRole() != AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can create administrator accounts");
        }

        String passwordHash = passwordEncoder.encode(requestDto.password());
        String firstName = StringUtils.hasText(requestDto.firstName())
                ? requestDto.firstName().trim()
                : normalizeRequired(requestDto.name(), "name");
        String lastName = StringUtils.hasText(requestDto.lastName()) ? requestDto.lastName().trim() : "";
        CreateAdminAccountRequestDto normalizedRequest = new CreateAdminAccountRequestDto(
                normalizeRequired(requestDto.name(), "name"),
                normalizeEmail(requestDto.email()),
                requestDto.password(),
                StringUtils.hasText(requestDto.phone()) ? requestDto.phone().trim() : null,
                firstName,
                lastName,
                StringUtils.hasText(requestDto.positionTitle()) ? requestDto.positionTitle().trim() : null
        );

        AccountEntity entity = accountMapper.toAdminEntity(normalizedRequest, passwordHash);
        AccountProfileNames.syncLegacyNameField(entity);
        AccountEntity saved = saveAccount(entity);
        staffProfileService.createForAdminAccount(saved, normalizedRequest.positionTitle());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponseDto registerCustomer(RegisterCustomerAccountRequestDto requestDto) {
        AccountEntity entity = new AccountEntity();
        entity.setFirstName(normalizeRequired(requestDto.firstName(), "firstName"));
        entity.setLastName(normalizeRequired(requestDto.lastName(), "lastName"));
        entity.setPatronymic(normalizeOptionalPatronymic(requestDto.patronymic()));
        AccountProfileNames.syncLegacyNameField(entity);
        entity.setPhone(normalizeRequired(requestDto.phone(), "phone"));
        entity.setEmail(normalizeEmail(requestDto.email()));
        entity.setRole(AccountRole.CUSTOMER);
        entity.setRegistrationStatus(AccountRegistrationStatus.ACTIVE);
        entity.setIsPasswordSet(Boolean.TRUE);
        entity.setPasswordHash(passwordEncoder.encode(requestDto.password()));
        entity.setIsBlocked(Boolean.FALSE);
        referralService.bindInviterForNewCustomer(entity, requestDto.inviteCode());
        AccountEntity saved = saveAccount(entity);
        referralService.assignReferralCodeIfMissing(saved.getId());
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponseDto updateCustomer(UUID accountId, UpdateCustomerAccountRequestDto requestDto) {
        UUID requesterId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity requester = findByIdOrThrow(requesterId);
        if (requester.getRole() == AccountRole.CUSTOMER && !requesterId.equals(accountId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);
        entity.setFirstName(normalizeRequired(requestDto.firstName(), "firstName"));
        entity.setLastName(normalizeRequired(requestDto.lastName(), "lastName"));
        entity.setPatronymic(normalizeOptionalPatronymic(requestDto.patronymic()));
        AccountProfileNames.syncLegacyNameField(entity);
        entity.setPhone(normalizeRequired(requestDto.phone(), "phone"));
        entity.setEmail(normalizeEmail(requestDto.email()));
        if (StringUtils.hasText(requestDto.password())) {
            if (requestDto.password().length() < 8) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "password must contain at least 8 characters");
            }
            entity.setPasswordHash(passwordEncoder.encode(requestDto.password()));
            entity.setIsPasswordSet(Boolean.TRUE);
        }
        return accountMapper.toResponse(saveAccount(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponseDto getMyProfile() {
        return getById(securityContextHelper.getCurrentAccountIdOrThrow());
    }

    @Override
    @Transactional
    public AccountResponseDto updateMyProfile(UpdateCustomerAccountRequestDto requestDto) {
        return updateCustomer(securityContextHelper.getCurrentAccountIdOrThrow(), requestDto);
    }

    @Override
    @Transactional
    public AccountResponseDto activateCustomer(UUID accountId, ActivateCustomerAccountRequestDto requestDto) {
        UUID requesterId = securityContextHelper.getCurrentAccountIdOrThrow();
        AccountEntity requester = findByIdOrThrow(requesterId);
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);

        boolean selfService = requesterId.equals(accountId);
        if (!selfService && requester.getRole() != AccountRole.ADMIN && requester.getRole() != AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

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
        AccountEntity requester = findByIdOrThrow(securityContextHelper.getCurrentAccountIdOrThrow());
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureDeleteAccountPermission(requester, entity);
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
        AccountProfileNames.syncLegacyNameField(entity);
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
        if (requester.getRole() == AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers cannot search accounts");
        }

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
        AccountEntity requester = findByIdOrThrow(securityContextHelper.getCurrentAccountIdOrThrow());
        AccountEntity account = findByIdOrThrow(accountId);
        ensureModeratorCanChangeBlockState(requester, account);
        account.setIsBlocked(Boolean.TRUE);
        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void unblock(UUID accountId) {
        AccountEntity requester = findByIdOrThrow(securityContextHelper.getCurrentAccountIdOrThrow());
        AccountEntity account = findByIdOrThrow(accountId);
        ensureModeratorCanChangeBlockState(requester, account);
        account.setIsBlocked(Boolean.FALSE);
        accountRepository.save(account);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponseDto getStaffProfile(UUID accountId) {
        return staffProfileService.getByAccountId(accountId);
    }

    @Override
    @Transactional
    public StaffProfileResponseDto updateStaffProfile(UUID accountId, UpdateStaffProfileRequestDto requestDto) {
        return staffProfileService.update(accountId, requestDto);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerAnalyticsResponseDto getCustomerAnalytics(UUID accountId) {
        return customerAnalyticsService.getForCustomer(accountId);
    }

    @Override
    @Transactional
    public CustomerAnalyticsResponseDto updateCustomerAnalytics(
            UUID accountId,
            UpdateCustomerAnalyticsRequestDto requestDto
    ) {
        return customerAnalyticsService.updateManual(accountId, requestDto);
    }

    @Override
    @Transactional
    public AccountResponseDto patchCustomerManagerFields(UUID accountId, PatchCustomerManagerRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner("Only admin or owner can update customer CRM links");
        AccountEntity entity = findByIdOrThrow(accountId);
        ensureCustomerOrThrow(entity);

        if (requestDto.crmOrganizationId() != null) {
            CrmOrganizationEntity org = crmOrganizationRepository.findById(requestDto.crmOrganizationId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "CRM organization not found"));
            entity.setCrmOrganization(org);
        }
        if (requestDto.broughtByManagerId() != null) {
            AccountEntity manager = findByIdOrThrow(requestDto.broughtByManagerId());
            if (manager.getRole() != AccountRole.ADMIN && manager.getRole() != AccountRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "broughtByManagerId must reference admin or owner");
            }
            entity.setBroughtByManager(manager);
        }

        return accountMapper.toResponse(saveAccount(entity));
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
        if (requester.getRole() == AccountRole.CUSTOMER) {
            if (!target.getId().equals(requester.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            return;
        }

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

    private void ensureDeleteAccountPermission(AccountEntity requester, AccountEntity target) {
        if (target.getRole() == AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner account cannot be deleted via this API");
        }
        if (requester.getRole() == AccountRole.CUSTOMER) {
            if (!requester.getId().equals(target.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            if (target.getRole() != AccountRole.CUSTOMER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
            }
            return;
        }
        if (requester.getRole() == AccountRole.ADMIN) {
            if (target.getRole() != AccountRole.CUSTOMER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin can delete only customer accounts");
            }
            return;
        }
        if (requester.getRole() == AccountRole.OWNER) {
            if (target.getRole() == AccountRole.CUSTOMER || target.getRole() == AccountRole.ADMIN) {
                return;
            }
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
    }

    private void ensureModeratorCanChangeBlockState(AccountEntity requester, AccountEntity target) {
        if (requester.getRole() == AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        if (target.getRole() == AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owner account cannot be blocked this way");
        }
        if (requester.getRole() == AccountRole.ADMIN && target.getRole() != AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin can manage only customer accounts");
        }
    }

    private String normalizeOptionalPatronymic(String patronymic) {
        if (!StringUtils.hasText(patronymic)) {
            return null;
        }
        String t = patronymic.trim();
        return t.length() > 255 ? t.substring(0, 255) : t;
    }
}
