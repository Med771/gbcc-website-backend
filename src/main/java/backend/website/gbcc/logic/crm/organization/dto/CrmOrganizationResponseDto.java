package backend.website.gbcc.logic.crm.organization.dto;

import backend.website.gbcc.logic.crm.organization.CrmClientStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CrmOrganizationResponseDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String name,
        String externalNumber,
        String legalAddress,
        String deliveryAddress,
        String floorNote,
        String commentGeneral,
        String productTypesNote,
        String supplyVolumeNote,
        String supplyScheduleNote,
        LocalDate cooperationUntil,
        CrmClientStatus clientStatus,
        Double latitude,
        Double longitude,
        UUID assignedToAccountId,
        String assignedToDisplayName,
        UUID convertedFromContactRequestId,
        UUID convertedFromCooperationRequestId
) {
}
