package backend.website.gbcc.logic.account.dto;

import java.util.UUID;

public record PatchCustomerManagerRequestDto(
        UUID crmOrganizationId,
        UUID broughtByManagerId
) {
}
