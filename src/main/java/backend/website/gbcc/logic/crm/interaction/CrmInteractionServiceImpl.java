package backend.website.gbcc.logic.crm.interaction;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.access.CrmEntityAccess;
import backend.website.gbcc.logic.crm.interaction.dto.CreateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.interaction.dto.CrmInteractionResponseDto;
import backend.website.gbcc.logic.crm.interaction.dto.UpdateCrmInteractionRequestDto;
import backend.website.gbcc.logic.crm.lead.CrmLeadEntity;
import backend.website.gbcc.logic.crm.lead.CrmLeadRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.task.CrmTaskEntity;
import backend.website.gbcc.logic.crm.task.CrmTaskRepository;
import backend.website.gbcc.model.AccountPrincipal;
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
public class CrmInteractionServiceImpl implements CrmInteractionService {

    private final CrmInteractionRepository interactionRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final CrmLeadRepository leadRepository;
    private final CrmTaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final CrmAccessPolicy crmAccessPolicy;
    private final CrmEntityAccess crmEntityAccess;

    @Override
    @Transactional
    public CrmInteractionResponseDto create(CreateCrmInteractionRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Target t = resolveTarget(dto.organizationId(), dto.leadId(), true);
        crmEntityAccess.assertCanModifyInteractionTarget(t.organization(), t.lead());

        CrmInteractionEntity e = new CrmInteractionEntity();
        e.setOrganization(t.organization());
        e.setLead(t.lead());
        e.setOccurredAt(dto.occurredAt());
        e.setAuthor(accountRepository.findById(principal.accountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found")));
        e.setResultNote(trim(dto.resultNote()));
        e.setCommentText(trim(dto.commentText()));
        e.setNextStep(trim(dto.nextStep()));
        e.setNextTask(resolveNextTask(dto.nextTaskId()));
        return toResponse(interactionRepository.save(e));
    }

    @Override
    @Transactional
    public CrmInteractionResponseDto update(UUID id, UpdateCrmInteractionRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmInteractionEntity e = findInteraction(id);
        crmAccessPolicy.assertInteractionAuthorOrOwner(e.getAuthor().getId());

        Target t = resolveTarget(dto.organizationId(), dto.leadId(), true);
        crmEntityAccess.assertCanModifyInteractionTarget(t.organization(), t.lead());
        e.setOrganization(t.organization());
        e.setLead(t.lead());
        e.setOccurredAt(dto.occurredAt());
        e.setResultNote(trim(dto.resultNote()));
        e.setCommentText(trim(dto.commentText()));
        e.setNextStep(trim(dto.nextStep()));
        e.setNextTask(resolveNextTask(dto.nextTaskId()));
        return toResponse(interactionRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmInteractionEntity e = findInteraction(id);
        crmAccessPolicy.assertInteractionAuthorOrOwner(e.getAuthor().getId());
        interactionRepository.delete(e);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmInteractionResponseDto getById(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmInteractionEntity e = findInteraction(id);
        crmEntityAccess.assertCanReadInteractionTarget(e.getOrganization(), e.getLead());
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmInteractionResponseDto> search(UUID organizationId, UUID leadId, UUID authorId,
                                                            java.time.Instant occurredFrom, java.time.Instant occurredTo,
                                                            String resultFragment, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmInteractionEntity> spec = Specification.allOf(
                CrmInteractionVisibility.visibleFor(principal),
                CrmInteractionSpecification.organizationIdEq(organizationId),
                CrmInteractionSpecification.leadIdEq(leadId),
                CrmInteractionSpecification.authorIdEq(authorId),
                CrmInteractionSpecification.occurredBetween(occurredFrom, occurredTo),
                CrmInteractionSpecification.resultContains(resultFragment)
        );
        Page<CrmInteractionResponseDto> page = interactionRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    private CrmInteractionEntity findInteraction(UUID id) {
        return interactionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interaction not found"));
    }

    private Target resolveTarget(UUID organizationId, UUID leadId, boolean requireOne) {
        boolean hasOrg = organizationId != null;
        boolean hasLead = leadId != null;
        if (requireOne && hasOrg == hasLead) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Provide exactly one of organizationId or leadId");
        }
        CrmOrganizationEntity org = null;
        CrmLeadEntity lead = null;
        if (hasOrg) {
            org = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization not found"));
        }
        if (hasLead) {
            lead = leadRepository.findById(leadId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Lead not found"));
        }
        return new Target(org, lead);
    }

    private CrmTaskEntity resolveNextTask(UUID nextTaskId) {
        if (nextTaskId == null) {
            return null;
        }
        CrmTaskEntity task = taskRepository.findById(nextTaskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Task not found"));
        if (task.getOrganization() != null) {
            crmAccessPolicy.assertCanReadOrganization(
                    task.getOrganization().getAssignedTo() != null ? task.getOrganization().getAssignedTo().getId() : null);
        } else if (task.getLead() != null) {
            crmAccessPolicy.assertCanReadLead(
                    task.getLead().getAssignedTo() != null ? task.getLead().getAssignedTo().getId() : null);
        }
        return task;
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CrmInteractionResponseDto toResponse(CrmInteractionEntity e) {
        AccountEntity author = e.getAuthor();
        return new CrmInteractionResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getOrganization() != null ? e.getOrganization().getId() : null,
                e.getLead() != null ? e.getLead().getId() : null,
                e.getOccurredAt(),
                author.getId(),
                displayName(author),
                e.getResultNote(),
                e.getCommentText(),
                e.getNextStep(),
                e.getNextTask() != null ? e.getNextTask().getId() : null
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
