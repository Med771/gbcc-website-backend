package backend.website.gbcc.helper;

import backend.website.gbcc.model.AccountPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

@Component
public class SecurityContextHelper {

    public Optional<AccountPrincipal> getCurrentAccountPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AccountPrincipal principal)) {
            return Optional.empty();
        }
        return Optional.of(principal);
    }

    public UUID getCurrentAccountIdOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof AccountPrincipal principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        return principal.accountId();
    }

    /**
     * Текущий пользователь — ADMIN или OWNER. Иначе 401 (нет сессии) или 403 с переданным текстом.
     */
    public AccountPrincipal requireAdminOrOwner(String forbiddenMessage) {
        Optional<AccountPrincipal> opt = getCurrentAccountPrincipal();
        if (opt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        AccountPrincipal principal = opt.get();
        if (!principal.isAdminOrOwner()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        return principal;
    }

    public boolean isCurrentPrincipalAdminOrOwner() {
        return getCurrentAccountPrincipal().map(AccountPrincipal::isAdminOrOwner).orElse(false);
    }
}
