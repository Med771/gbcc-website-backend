package backend.website.gbcc.logic.crm.lead;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.lead.dto.CreateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmLeadResponseDto;
import backend.website.gbcc.logic.crm.lead.dto.CrmReassignLeadRequestDto;
import backend.website.gbcc.logic.crm.lead.dto.UpdateCrmLeadRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmClientStatus;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationService;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmLeadServiceImpl implements CrmLeadService {

    private final CrmLeadRepository leadRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final AccountRepository accountRepository;
    private final CrmAccessPolicy crmAccessPolicy;
    private final CrmOrganizationService organizationService;

    @Override
    @Transactional
    public CrmLeadResponseDto create(CreateCrmLeadRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmLeadEntity e = new CrmLeadEntity();
        e.setCompanyName(dto.companyName().trim());
        e.setPhones(trim(dto.phones()));
        e.setPresumedContacts(trim(dto.presumedContacts()));
        e.setManagerComment(trim(dto.managerComment()));
        e.setDepartment(trim(dto.department()));
        e.setStatus(dto.status() != null ? dto.status() : CrmLeadStatus.NEW);

        if (principal.role() == AccountRole.ADMIN) {
            AccountEntity self = accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
            e.setAssignedTo(self);
        } else {
            if (dto.assignedToAccountId() != null) {
                AccountEntity assignee = accountRepository.findById(dto.assignedToAccountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                if (assignee.getRole() != AccountRole.ADMIN) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead assignee must be an administrator");
                }
                e.setAssignedTo(assignee);
            }
        }
        return toResponse(leadRepository.save(e));
    }

    @Override
    @Transactional
    public CrmLeadResponseDto update(UUID id, UpdateCrmLeadRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmLeadEntity e = findLead(id);
        crmAccessPolicy.assertCanModifyLead(assignedId(e));

        if (e.getStatus() == CrmLeadStatus.CONVERTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Converted lead cannot be updated");
        }
        e.setCompanyName(dto.companyName().trim());
        e.setPhones(trim(dto.phones()));
        e.setPresumedContacts(trim(dto.presumedContacts()));
        e.setManagerComment(trim(dto.managerComment()));
        e.setDepartment(trim(dto.department()));
        e.setStatus(dto.status());
        if (principal.role() == AccountRole.OWNER && dto.assignedToAccountId() != null) {
            AccountEntity assignee = accountRepository.findById(dto.assignedToAccountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
            if (assignee.getRole() != AccountRole.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead assignee must be an administrator");
            }
            e.setAssignedTo(assignee);
        }
        return toResponse(leadRepository.save(e));
    }

    @Override
    @Transactional
    public CrmLeadResponseDto reassign(UUID id, CrmReassignLeadRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        crmAccessPolicy.assertCanReassignOrganization();
        CrmLeadEntity e = findLead(id);
        if (e.getStatus() == CrmLeadStatus.CONVERTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Converted lead cannot be reassigned");
        }
        AccountEntity assignee = accountRepository.findById(dto.assignedToAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (assignee.getRole() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead assignee must be an administrator");
        }
        e.setAssignedTo(assignee);
        return toResponse(leadRepository.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public CrmLeadResponseDto getById(UUID id) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmLeadEntity e = findLead(id);
        crmAccessPolicy.assertCanReadLead(assignedId(e));
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmLeadResponseDto> search(String query, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmLeadEntity> spec = Specification.allOf(
                CrmLeadSpecification.accessibleBy(principal),
                CrmLeadSpecification.queryLike(query)
        );
        Page<CrmLeadResponseDto> page = leadRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public CrmOrganizationResponseDto convertToOrganization(UUID leadId) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmLeadEntity lead = findLead(leadId);
        crmAccessPolicy.assertCanModifyLead(assignedId(lead));
        if (lead.getStatus() == CrmLeadStatus.CONVERTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead already converted");
        }

        CrmOrganizationEntity org = new CrmOrganizationEntity();
        org.setName(lead.getCompanyName());
        org.setCommentGeneral(buildOrgCommentFromLead(lead));
        org.setClientStatus(CrmClientStatus.PROSPECT);
        org.setAssignedTo(lead.getAssignedTo());
        organizationRepository.save(org);

        lead.setConvertedOrganization(org);
        lead.setStatus(CrmLeadStatus.CONVERTED);
        leadRepository.save(lead);

        return organizationService.getById(org.getId());
    }

    private String buildOrgCommentFromLead(CrmLeadEntity lead) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(lead.getPhones())) {
            sb.append("Phones: ").append(lead.getPhones()).append("\n");
        }
        if (StringUtils.hasText(lead.getPresumedContacts())) {
            sb.append("Contacts: ").append(lead.getPresumedContacts()).append("\n");
        }
        if (StringUtils.hasText(lead.getManagerComment())) {
            sb.append("Manager: ").append(lead.getManagerComment());
        }
        return sb.isEmpty() ? null : sb.toString();
    }

    private CrmLeadEntity findLead(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lead not found"));
    }

    private UUID assignedId(CrmLeadEntity e) {
        return e.getAssignedTo() != null ? e.getAssignedTo().getId() : null;
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CrmLeadResponseDto toResponse(CrmLeadEntity e) {
        AccountEntity assigned = e.getAssignedTo();
        return new CrmLeadResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getCompanyName(),
                e.getPhones(),
                e.getPresumedContacts(),
                e.getManagerComment(),
                e.getDepartment(),
                e.getStatus(),
                assigned != null ? assigned.getId() : null,
                assigned != null ? displayName(assigned) : null,
                e.getConvertedOrganization() != null ? e.getConvertedOrganization().getId() : null
        );
    }

    private String displayName(AccountEntity a) {
        if (StringUtils.hasText(a.getName())) {
            return a.getName();
        }
        String fn = a.getFirstName() != null ? a.getFirstName() : "";
        String ln = a.getLastName() != null ? a.getLastName() : "";
        String c = (fn + " " + ln).trim();
        return c.isEmpty() ? a.getEmail() : c;
    }
}
