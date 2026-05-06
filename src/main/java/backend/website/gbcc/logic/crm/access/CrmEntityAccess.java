package backend.website.gbcc.logic.crm.access;

import backend.website.gbcc.logic.crm.lead.CrmLeadEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CrmEntityAccess {

    private final CrmAccessPolicy crmAccessPolicy;

    public void assertCanReadInteractionTarget(CrmOrganizationEntity organization, CrmLeadEntity lead) {
        if (organization != null) {
            crmAccessPolicy.assertCanReadOrganization(
                    organization.getAssignedTo() != null ? organization.getAssignedTo().getId() : null);
        } else if (lead != null) {
            crmAccessPolicy.assertCanReadLead(
                    lead.getAssignedTo() != null ? lead.getAssignedTo().getId() : null);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Interaction must reference organization or lead");
        }
    }

    public void assertCanModifyInteractionTarget(CrmOrganizationEntity organization, CrmLeadEntity lead) {
        assertCanReadInteractionTarget(organization, lead);
    }
}
