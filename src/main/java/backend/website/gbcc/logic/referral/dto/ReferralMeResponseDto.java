package backend.website.gbcc.logic.referral.dto;

import java.math.BigDecimal;

public record ReferralMeResponseDto(
        String referralCode,
        String referralCodeDisplay,
        String referralLink,
        long clickCount,
        long purchaseCount,
        BigDecimal availableBalance,
        BigDecimal minWithdrawalAmount,
        BigDecimal commissionPercent,
        boolean inviteCodeApplied,
        String appliedInviteCodeDisplay
) {
}
