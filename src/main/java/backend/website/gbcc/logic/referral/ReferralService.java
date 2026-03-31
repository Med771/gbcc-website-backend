package backend.website.gbcc.logic.referral;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.referral.dto.ReferralApplyInviteCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralClickRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralCommissionAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralMeResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralRefereeRowDto;
import backend.website.gbcc.logic.referral.dto.ReferralRegistrationAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralUpdateMyCodeRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawRequestDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalAdminDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalCreatedResponseDto;
import backend.website.gbcc.logic.referral.dto.ReferralWithdrawalRejectRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReferralService {

    /**
     * Выдаёт реферальный код новому клиенту сразу после регистрации (чтобы им можно было делиться без входа в раздел программы).
     */
    void assignReferralCodeIfMissing(UUID customerAccountId);

    void bindInviterForNewCustomer(AccountEntity newCustomer, String rawInviteCode);

    void recordClick(ReferralClickRequestDto requestDto);

    ReferralMeResponseDto getMyReferralDashboard();

    Page<ReferralRefereeRowDto> getMyReferees(Pageable pageable);

    void applyInviteCode(ReferralApplyInviteCodeRequestDto requestDto);

    void updateMyReferralCode(ReferralUpdateMyCodeRequestDto requestDto);

    ReferralWithdrawalCreatedResponseDto requestWithdrawal(ReferralWithdrawRequestDto requestDto);

    void onOrderDelivered(UUID orderId);

    Page<ReferralClickAdminDto> listClicksForAdmin(Pageable pageable);

    Page<ReferralCommissionAdminDto> listCommissionsForAdmin(Pageable pageable);

    Page<ReferralWithdrawalAdminDto> listWithdrawalsForAdmin(Pageable pageable);

    Page<ReferralRegistrationAdminDto> listRegistrationsWithInviterForAdmin(Pageable pageable);

    void markWithdrawalPaid(UUID withdrawalId);

    void rejectWithdrawal(UUID withdrawalId, ReferralWithdrawalRejectRequestDto requestDto);
}
