package backend.website.gbcc.logic.managercommission.dto;

import backend.website.gbcc.model.ReferralClientType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ManagerCommissionAdminDto(
        UUID id,
        Instant createdAt,
        UUID orderId,
        Long orderDisplayNumber,
        UUID managerAccountId,
        String managerEmail,
        UUID customerAccountId,
        String customerEmail,
        BigDecimal orderAmount,
        BigDecimal commissionPercent,
        BigDecimal commissionAmount,
        ReferralClientType clientType
) {
}
