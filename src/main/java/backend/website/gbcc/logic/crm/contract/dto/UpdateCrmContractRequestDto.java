package backend.website.gbcc.logic.crm.contract.dto;

import java.time.LocalDate;

public record UpdateCrmContractRequestDto(
        LocalDate startDate,
        LocalDate endDate,
        String commentText
) {
}
