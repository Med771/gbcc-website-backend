package backend.website.gbcc.logic.account.dto;

import backend.website.gbcc.model.AccountRole;

public record AccountSearchRequestDto(
        String name,
        String phone,
        String email,
        AccountRole role,
        Boolean isBlocked
) {
}
