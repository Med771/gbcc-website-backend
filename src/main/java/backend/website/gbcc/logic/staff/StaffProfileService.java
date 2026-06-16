package backend.website.gbcc.logic.staff;

import backend.website.gbcc.logic.staff.dto.StaffProfileResponseDto;
import backend.website.gbcc.logic.staff.dto.UpdateStaffProfileRequestDto;

import java.util.UUID;

public interface StaffProfileService {

    StaffProfileResponseDto getByAccountId(UUID accountId);

    StaffProfileResponseDto update(UUID accountId, UpdateStaffProfileRequestDto dto);

    void createForAdminAccount(backend.website.gbcc.logic.account.AccountEntity account, String positionTitle);
}
