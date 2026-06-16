package backend.website.gbcc.logic.staff;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.staff.dto.StaffProfileResponseDto;
import backend.website.gbcc.logic.staff.dto.UpdateStaffProfileRequestDto;
import backend.website.gbcc.model.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StaffProfileServiceImpl implements StaffProfileService {

    private final StaffProfileRepository staffProfileRepository;
    private final AccountRepository accountRepository;
    private final StaffDisplayIdGenerator displayIdGenerator;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional(readOnly = true)
    public StaffProfileResponseDto getByAccountId(UUID accountId) {
        AccountEntity requester = findAccount(securityContextHelper.getCurrentAccountIdOrThrow());
        AccountEntity target = findAccount(accountId);
        assertStaffProfileReadAccess(requester, target);

        StaffProfileEntity profile = staffProfileRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found"));
        return toResponse(target, profile);
    }

    @Override
    @Transactional
    public StaffProfileResponseDto update(UUID accountId, UpdateStaffProfileRequestDto dto) {
        AccountEntity requester = findAccount(securityContextHelper.getCurrentAccountIdOrThrow());
        AccountEntity target = findAccount(accountId);
        assertStaffProfileWriteAccess(requester, target);

        StaffProfileEntity profile = staffProfileRepository.findById(accountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff profile not found"));

        boolean selfUpdate = requester.getId().equals(target.getId());
        if (selfUpdate && requester.getRole() != AccountRole.OWNER) {
            profile.setSocialLink(trim(dto.socialLink()));
            if (StringUtils.hasText(dto.phone())) {
                target.setPhone(dto.phone().trim());
                accountRepository.save(target);
            }
        } else {
            profile.setPositionTitle(trim(dto.positionTitle()));
            profile.setSocialLink(trim(dto.socialLink()));
            profile.setBankAccountDetails(trim(dto.bankAccountDetails()));
            if (StringUtils.hasText(dto.phone())) {
                target.setPhone(dto.phone().trim());
                accountRepository.save(target);
            }
        }

        return toResponse(target, staffProfileRepository.save(profile));
    }

    @Override
    @Transactional
    public void createForAdminAccount(AccountEntity account, String positionTitle) {
        StaffProfileEntity profile = new StaffProfileEntity();
        profile.setAccount(account);
        profile.setPositionTitle(trim(positionTitle));
        profile.setDisplayId(displayIdGenerator.generateUniqueId(staffProfileRepository::existsByDisplayId));
        staffProfileRepository.save(profile);
    }

    private AccountEntity findAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private void assertStaffProfileReadAccess(AccountEntity requester, AccountEntity target) {
        ensureStaffAccount(target);
        if (requester.getId().equals(target.getId())) {
            return;
        }
        if (requester.getRole() == AccountRole.CUSTOMER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        if (requester.getRole() == AccountRole.ADMIN) {
            if (target.getRole() == AccountRole.OWNER) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot access owner account");
            }
            if (target.getRole() == AccountRole.ADMIN && !target.getId().equals(requester.getId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin cannot access other admin accounts");
            }
        }
    }

    private void assertStaffProfileWriteAccess(AccountEntity requester, AccountEntity target) {
        assertStaffProfileReadAccess(requester, target);
        if (requester.getRole() == AccountRole.ADMIN && !requester.getId().equals(target.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can update other staff profiles");
        }
    }

    private void ensureStaffAccount(AccountEntity account) {
        if (account.getRole() != AccountRole.ADMIN && account.getRole() != AccountRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account is not staff");
        }
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private StaffProfileResponseDto toResponse(AccountEntity account, StaffProfileEntity profile) {
        return new StaffProfileResponseDto(
                account.getId(),
                account.getFirstName(),
                account.getLastName(),
                account.getEmail(),
                account.getPhone(),
                profile.getPositionTitle(),
                profile.getSocialLink(),
                profile.getBankAccountDetails(),
                profile.getDisplayId()
        );
    }
}
