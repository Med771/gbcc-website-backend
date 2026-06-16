package backend.website.gbcc.logic.account;

import backend.website.gbcc.logic.account.dto.AccountResponseDto;
import backend.website.gbcc.logic.account.dto.AccountSearchRequestDto;
import backend.website.gbcc.logic.account.dto.ActivateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.CreateAdminAccountRequestDto;
import backend.website.gbcc.logic.account.dto.RegisterCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.UpdateCustomerAccountRequestDto;
import backend.website.gbcc.logic.account.dto.PatchCustomerManagerRequestDto;
import backend.website.gbcc.logic.customeranalytics.dto.CustomerAnalyticsResponseDto;
import backend.website.gbcc.logic.customeranalytics.dto.UpdateCustomerAnalyticsRequestDto;
import backend.website.gbcc.logic.staff.dto.StaffProfileResponseDto;
import backend.website.gbcc.logic.staff.dto.UpdateStaffProfileRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountService {
    AccountResponseDto createAdmin(CreateAdminAccountRequestDto requestDto);

    AccountResponseDto registerCustomer(RegisterCustomerAccountRequestDto requestDto);

    AccountResponseDto updateCustomer(UUID accountId, UpdateCustomerAccountRequestDto requestDto);

    AccountResponseDto getMyProfile();

    AccountResponseDto updateMyProfile(UpdateCustomerAccountRequestDto requestDto);

    AccountResponseDto activateCustomer(UUID accountId, ActivateCustomerAccountRequestDto requestDto);

    void deleteCustomer(UUID accountId);

    void ensureOwnerExists(String email, String rawPassword);

    AccountResponseDto getById(UUID accountId);

    Page<AccountResponseDto> search(AccountSearchRequestDto requestDto, Pageable pageable);

    void block(UUID accountId);

    void unblock(UUID accountId);

    StaffProfileResponseDto getStaffProfile(UUID accountId);

    StaffProfileResponseDto updateStaffProfile(UUID accountId, UpdateStaffProfileRequestDto requestDto);

    CustomerAnalyticsResponseDto getCustomerAnalytics(UUID accountId);

    CustomerAnalyticsResponseDto updateCustomerAnalytics(UUID accountId, UpdateCustomerAnalyticsRequestDto requestDto);

    AccountResponseDto patchCustomerManagerFields(UUID accountId, PatchCustomerManagerRequestDto requestDto);
}
