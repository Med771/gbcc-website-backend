package backend.website.gbcc.logic.crm.access;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * CRM доступ: только ADMIN и OWNER; OWNER видит всё, ADMIN — только сущности, назначенные на него
 * ({@code assignedToAccountId}), кроме случаев без назначения — их видит только OWNER.
 */
@Component
@RequiredArgsConstructor
public class CrmAccessPolicy {

    public static final String FORBIDDEN_CRM = "CRM is only for administrators";
    public static final String FORBIDDEN_NOT_ASSIGNED = "Not assigned to this record";
    public static final String FORBIDDEN_UNASSIGNED = "Unassigned records are visible only to owner";

    private final SecurityContextHelper securityContextHelper;

    public AccountPrincipal requireCrmUser() {
        AccountPrincipal principal = securityContextHelper.getCurrentAccountPrincipal()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
        if (!principal.isAdminOrOwner()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_CRM);
        }
        return principal;
    }

    public boolean isOwner(AccountPrincipal principal) {
        return principal.role() == AccountRole.OWNER;
    }

    /**
     * Чтение организации: OWNER — всегда; ADMIN — только если назначен на него.
     */
    public void assertCanReadOrganization(UUID assignedToAccountId) {
        AccountPrincipal principal = requireCrmUser();
        if (isOwner(principal)) {
            return;
        }
        if (assignedToAccountId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_UNASSIGNED);
        }
        if (!assignedToAccountId.equals(principal.accountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_NOT_ASSIGNED);
        }
    }

    public void assertCanModifyOrganization(UUID assignedToAccountId) {
        assertCanReadOrganization(assignedToAccountId);
    }

    public void assertCanReassignOrganization() {
        AccountPrincipal principal = requireCrmUser();
        if (!isOwner(principal)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only owner can reassign CRM records");
        }
    }

    public void assertCanReadLead(UUID assignedToAccountId) {
        assertCanReadOrganization(assignedToAccountId);
    }

    public void assertCanModifyLead(UUID assignedToAccountId) {
        assertCanReadOrganization(assignedToAccountId);
    }

    public void assertForOrganizationAssignment(UUID organizationAssignedToId) {
        assertCanReadOrganization(organizationAssignedToId);
    }

    public void assertTaskAssigneeOrOwner(UUID assigneeAccountId) {
        AccountPrincipal principal = requireCrmUser();
        if (isOwner(principal)) {
            return;
        }
        if (assigneeAccountId != null && assigneeAccountId.equals(principal.accountId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, FORBIDDEN_NOT_ASSIGNED);
    }

    public void assertInteractionAuthorOrOwner(UUID authorAccountId) {
        AccountPrincipal principal = requireCrmUser();
        if (isOwner(principal)) {
            return;
        }
        if (authorAccountId != null && authorAccountId.equals(principal.accountId())) {
            return;
        }
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only author or owner can modify this interaction");
    }
}
