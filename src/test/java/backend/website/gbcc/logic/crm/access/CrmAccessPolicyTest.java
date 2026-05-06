package backend.website.gbcc.logic.crm.access;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmAccessPolicyTest {

    private static final UUID ADMIN_ID = UUID.fromString("00000000-0000-4000-8000-0000000000aa");

    @Mock
    private SecurityContextHelper securityContextHelper;

    @InjectMocks
    private CrmAccessPolicy crmAccessPolicy;

    @Test
    void requireCrmUser_customer_throws403() {
        when(securityContextHelper.getCurrentAccountPrincipal())
                .thenReturn(Optional.of(new AccountPrincipal(ADMIN_ID, AccountRole.CUSTOMER)));

        assertThatThrownBy(() -> crmAccessPolicy.requireCrmUser())
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void assertCanReadOrganization_adminUnassigned_throws403() {
        when(securityContextHelper.getCurrentAccountPrincipal())
                .thenReturn(Optional.of(new AccountPrincipal(ADMIN_ID, AccountRole.ADMIN)));

        assertThatThrownBy(() -> crmAccessPolicy.assertCanReadOrganization(null))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void assertCanReadOrganization_adminAssignedSelf_ok() {
        when(securityContextHelper.getCurrentAccountPrincipal())
                .thenReturn(Optional.of(new AccountPrincipal(ADMIN_ID, AccountRole.ADMIN)));

        crmAccessPolicy.assertCanReadOrganization(ADMIN_ID);
        assertThat(true).isTrue();
    }

    @Test
    void assertCanReadOrganization_ownerUnassigned_ok() {
        when(securityContextHelper.getCurrentAccountPrincipal())
                .thenReturn(Optional.of(new AccountPrincipal(ADMIN_ID, AccountRole.OWNER)));

        crmAccessPolicy.assertCanReadOrganization(null);
        assertThat(true).isTrue();
    }
}
