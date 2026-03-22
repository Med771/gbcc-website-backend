package backend.website.gbcc.model;

import java.util.UUID;

public record AccountPrincipal(
        UUID accountId,
        AccountRole role
) {
    public boolean isAdminOrOwner() {
        return role == AccountRole.ADMIN || role == AccountRole.OWNER;
    }
}
