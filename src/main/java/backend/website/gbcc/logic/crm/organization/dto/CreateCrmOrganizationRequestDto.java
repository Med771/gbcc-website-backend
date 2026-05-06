package backend.website.gbcc.logic.crm.organization.dto;

import backend.website.gbcc.logic.crm.organization.CrmClientStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

public record CreateCrmOrganizationRequestDto(
        @NotBlank @Size(max = 512) String name,
        @Size(max = 128) String externalNumber,
        String legalAddress,
        String deliveryAddress,
        @Size(max = 255) String floorNote,
        String commentGeneral,
        String productTypesNote,
        String supplyVolumeNote,
        String supplyScheduleNote,
        LocalDate cooperationUntil,
        @NotNull CrmClientStatus clientStatus,
        Double latitude,
        Double longitude,
        /** Только OWNER может назначить другого администратора; для ADMIN игнорируется (назначается сам). */
        UUID assignedToAccountId,
        UUID convertedFromContactRequestId,
        UUID convertedFromCooperationRequestId
) {
}
