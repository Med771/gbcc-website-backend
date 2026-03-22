package backend.website.gbcc.helper;

import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityContextHelperTest {

    private final SecurityContextHelper helper = new SecurityContextHelper();

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAccountPrincipal_shouldReturnEmpty_whenNoAuth() {
        assertThat(helper.getCurrentAccountPrincipal()).isEmpty();
    }

    @Test
    void getCurrentAccountIdOrThrow_shouldThrow401_whenNoAuth() {
        assertThatThrownBy(helper::getCurrentAccountIdOrThrow)
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void getCurrentAccountIdOrThrow_shouldReturnId_whenPrincipalSet() {
        UUID id = UUID.randomUUID();
        AccountPrincipal principal = new AccountPrincipal(id, AccountRole.CUSTOMER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThat(helper.getCurrentAccountIdOrThrow()).isEqualTo(id);
    }

    @Test
    void requireAdminOrOwner_shouldThrow403_whenCustomer() {
        UUID id = UUID.randomUUID();
        AccountPrincipal principal = new AccountPrincipal(id, AccountRole.CUSTOMER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThatThrownBy(() -> helper.requireAdminOrOwner("no access"))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode().value()).isEqualTo(403));
    }

    @Test
    void requireAdminOrOwner_shouldReturnPrincipal_whenAdmin() {
        UUID id = UUID.randomUUID();
        AccountPrincipal principal = new AccountPrincipal(id, AccountRole.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThat(helper.requireAdminOrOwner("no access").accountId()).isEqualTo(id);
    }

    @Test
    void isCurrentPrincipalAdminOrOwner_shouldBeFalse_forCustomer() {
        AccountPrincipal principal = new AccountPrincipal(UUID.randomUUID(), AccountRole.CUSTOMER);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );

        assertThat(helper.isCurrentPrincipalAdminOrOwner()).isFalse();
    }
}
