package backend.website.gbcc.logic.crm.task;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.lead.CrmLeadEntity;
import backend.website.gbcc.logic.crm.lead.CrmLeadRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.task.dto.CreateCrmTaskRequestDto;
import backend.website.gbcc.logic.crm.task.dto.CrmTaskResponseDto;
import backend.website.gbcc.logic.crm.task.dto.UpdateCrmTaskRequestDto;
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

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmTaskServiceImpl implements CrmTaskService {

    private final CrmTaskRepository taskRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final CrmLeadRepository leadRepository;
    private final AccountRepository accountRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional
    public CrmTaskResponseDto create(CreateCrmTaskRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Target t = resolveTarget(dto.organizationId(), dto.leadId());
        assertTargetAccess(t, principal);

        AccountEntity assignee = resolveAssignee(dto.assigneeAccountId(), principal);
        if (principal.role() == AccountRole.ADMIN && dto.assigneeAccountId() != null
                && !dto.assigneeAccountId().equals(principal.accountId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator cannot assign task to another user");
        }

        CrmTaskEntity e = new CrmTaskEntity();
        e.setOrganization(t.organization());
        e.setLead(t.lead());
        e.setDueAt(dto.dueAt());
        e.setReason(trim(dto.reason()));
        e.setCommentText(trim(dto.commentText()));
        e.setStatus(dto.status() != null ? dto.status() : CrmTaskStatus.OPEN);
        e.setAssignee(assignee);
        return toResponse(taskRepository.save(e));
    }

    @Override
    @Transactional
    public CrmTaskResponseDto update(UUID id, UpdateCrmTaskRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmTaskEntity e = findTask(id);
        crmAccessPolicy.assertTaskAssigneeOrOwner(e.getAssignee().getId());

        Target t = resolveTarget(dto.organizationId(), dto.leadId());
        assertTargetAccess(t, principal);
        AccountEntity assignee = resolveAssigneeForUpdate(dto.assigneeAccountId(), principal, e.getAssignee());

        e.setOrganization(t.organization());
        e.setLead(t.lead());
        e.setDueAt(dto.dueAt());
        e.setReason(trim(dto.reason()));
        e.setCommentText(trim(dto.commentText()));
        e.setStatus(dto.status());
        e.setAssignee(assignee);
        return toResponse(taskRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmTaskEntity e = findTask(id);
        crmAccessPolicy.assertTaskAssigneeOrOwner(e.getAssignee().getId());
        taskRepository.delete(e);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmTaskResponseDto getById(UUID id) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmTaskEntity e = findTask(id);
        crmAccessPolicy.assertTaskAssigneeOrOwner(e.getAssignee().getId());
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmTaskResponseDto> search(UUID organizationId, UUID leadId, CrmTaskStatus status,
                                                   UUID assigneeId, Instant dueAfter, Instant dueBefore, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmTaskEntity> spec = Specification.allOf(
                CrmTaskVisibility.visibleFor(principal),
                CrmTaskSpecification.organizationIdEq(organizationId),
                CrmTaskSpecification.leadIdEq(leadId),
                CrmTaskSpecification.statusEq(status),
                CrmTaskSpecification.assigneeIdEq(assigneeId),
                CrmTaskSpecification.dueAfter(dueAfter),
                CrmTaskSpecification.dueBefore(dueBefore)
        );
        Page<CrmTaskResponseDto> page = taskRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmTaskResponseDto> overdue() {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Instant now = Instant.now();
        Specification<CrmTaskEntity> spec = Specification.allOf(
                CrmTaskVisibility.visibleFor(principal),
                CrmTaskSpecification.statusEq(CrmTaskStatus.OPEN),
                (root, q, cb) -> cb.lessThan(root.get("dueAt"), now)
        );
        return taskRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    private CrmTaskEntity findTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    }

    private Target resolveTarget(UUID organizationId, UUID leadId) {
        boolean hasOrg = organizationId != null;
        boolean hasLead = leadId != null;
        if (hasOrg == hasLead) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide exactly one of organizationId or leadId");
        }
        if (hasOrg) {
            CrmOrganizationEntity org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization not found"));
            return new Target(org, null);
        }
        CrmLeadEntity lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead not found"));
        return new Target(null, lead);
    }

    private void assertTargetAccess(Target t, AccountPrincipal principal) {
        if (t.organization() != null) {
            crmAccessPolicy.assertCanReadOrganization(
                    t.organization().getAssignedTo() != null ? t.organization().getAssignedTo().getId() : null);
        } else {
            crmAccessPolicy.assertCanReadLead(
                    t.lead().getAssignedTo() != null ? t.lead().getAssignedTo().getId() : null);
        }
    }

    private AccountEntity resolveAssignee(UUID assigneeAccountId, AccountPrincipal principal) {
        if (principal.role() == AccountRole.ADMIN) {
            return accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        }
        if (assigneeAccountId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "assigneeAccountId is required for owner");
        }
        AccountEntity a = accountRepository.findById(assigneeAccountId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (a.getRole() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task assignee must be an administrator");
        }
        return a;
    }

    private AccountEntity resolveAssigneeForUpdate(UUID assigneeAccountId, AccountPrincipal principal, AccountEntity currentAssignee) {
        if (principal.role() == AccountRole.ADMIN) {
            return accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
        }
        UUID id = assigneeAccountId != null ? assigneeAccountId : currentAssignee.getId();
        AccountEntity a = accountRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (a.getRole() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task assignee must be an administrator");
        }
        return a;
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CrmTaskResponseDto toResponse(CrmTaskEntity e) {
        Instant now = Instant.now();
        boolean overdue = e.getStatus() == CrmTaskStatus.OPEN && e.getDueAt().isBefore(now);
        AccountEntity assignee = e.getAssignee();
        return new CrmTaskResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getOrganization() != null ? e.getOrganization().getId() : null,
                e.getLead() != null ? e.getLead().getId() : null,
                e.getDueAt(),
                e.getReason(),
                e.getCommentText(),
                e.getStatus(),
                assignee.getId(),
                displayName(assignee),
                overdue
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

    private record Target(CrmOrganizationEntity organization, CrmLeadEntity lead) {
    }
}
