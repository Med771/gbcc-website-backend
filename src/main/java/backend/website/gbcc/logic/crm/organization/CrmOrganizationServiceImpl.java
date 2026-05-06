package backend.website.gbcc.logic.crm.organization;

import backend.website.gbcc.logic.account.AccountEntity;
import backend.website.gbcc.logic.account.AccountRepository;
import backend.website.gbcc.logic.contactrequest.ContactRequestRepository;
import backend.website.gbcc.logic.cooperationrequest.CooperationRequestRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CreateCrmOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactHistoryResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationContactResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.logic.crm.organization.dto.CrmReassignOrganizationRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationContactRequestDto;
import backend.website.gbcc.logic.crm.organization.dto.UpdateCrmOrganizationRequestDto;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmOrganizationServiceImpl implements CrmOrganizationService {

    private final CrmOrganizationRepository organizationRepository;
    private final CrmOrganizationContactRepository contactRepository;
    private final CrmOrganizationContactHistoryRepository historyRepository;
    private final AccountRepository accountRepository;
    private final ContactRequestRepository contactRequestRepository;
    private final CooperationRequestRepository cooperationRequestRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional
    public CrmOrganizationResponseDto create(CreateCrmOrganizationRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity entity = new CrmOrganizationEntity();
        applyOrgFields(entity, dto.name(), dto.externalNumber(), dto.legalAddress(), dto.deliveryAddress(),
                dto.floorNote(), dto.commentGeneral(), dto.productTypesNote(), dto.supplyVolumeNote(),
                dto.supplyScheduleNote(), dto.cooperationUntil(), dto.clientStatus(), dto.latitude(), dto.longitude());

        if (principal.role() == AccountRole.ADMIN) {
            AccountEntity self = accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
            entity.setAssignedTo(self);
        } else {
            if (dto.assignedToAccountId() != null) {
                AccountEntity assignee = accountRepository.findById(dto.assignedToAccountId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
                if (assignee.getRole() != AccountRole.ADMIN) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CRM assignee must be an administrator");
                }
                entity.setAssignedTo(assignee);
            }
        }

        if (dto.convertedFromContactRequestId() != null) {
            entity.setConvertedFromContactRequest(
                    contactRequestRepository.findById(dto.convertedFromContactRequestId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contact request not found")));
        }
        if (dto.convertedFromCooperationRequestId() != null) {
            entity.setConvertedFromCooperationRequest(
                    cooperationRequestRepository.findById(dto.convertedFromCooperationRequestId())
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cooperation request not found")));
        }

        CrmOrganizationEntity saved = organizationRepository.save(entity);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public CrmOrganizationResponseDto update(UUID id, UpdateCrmOrganizationRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity entity = findOrg(id);
        crmAccessPolicy.assertCanModifyOrganization(assignedId(entity));

        applyOrgFields(entity, dto.name(), dto.externalNumber(), dto.legalAddress(), dto.deliveryAddress(),
                dto.floorNote(), dto.commentGeneral(), dto.productTypesNote(), dto.supplyVolumeNote(),
                dto.supplyScheduleNote(), dto.cooperationUntil(), dto.clientStatus(), dto.latitude(), dto.longitude());
        return toResponse(organizationRepository.save(entity));
    }

    @Override
    @Transactional
    public CrmOrganizationResponseDto reassign(UUID id, CrmReassignOrganizationRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        crmAccessPolicy.assertCanReassignOrganization();
        CrmOrganizationEntity entity = findOrg(id);
        AccountEntity assignee = accountRepository.findById(dto.assignedToAccountId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignee not found"));
        if (assignee.getRole() != AccountRole.ADMIN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CRM assignee must be an administrator");
        }
        entity.setAssignedTo(assignee);
        return toResponse(organizationRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public CrmOrganizationResponseDto getById(UUID id) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity entity = findOrg(id);
        crmAccessPolicy.assertCanReadOrganization(assignedId(entity));
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmOrganizationResponseDto> search(String query, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmOrganizationEntity> spec = Specification.allOf(
                CrmOrganizationSpecification.accessibleBy(principal),
                CrmOrganizationSpecification.queryLike(query)
        );
        Page<CrmOrganizationResponseDto> page = organizationRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public CrmOrganizationContactResponseDto addContact(UUID organizationId, CreateCrmOrganizationContactRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = findOrg(organizationId);
        crmAccessPolicy.assertCanModifyOrganization(assignedId(org));

        CrmOrganizationContactEntity c = new CrmOrganizationContactEntity();
        c.setOrganization(org);
        c.setFullName(trimToNull(dto.fullName()));
        c.setDepartment(trimToNull(dto.department()));
        c.setPhone(trimToNull(dto.phone()));
        c.setEmail(trimToNull(dto.email()));
        c.setExtraNote(trimToNull(dto.extraNote()));
        return toContactResponse(contactRepository.save(c));
    }

    @Override
    @Transactional
    public CrmOrganizationContactResponseDto updateContact(UUID organizationId, UUID contactId, UpdateCrmOrganizationContactRequestDto dto) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = findOrg(organizationId);
        crmAccessPolicy.assertCanModifyOrganization(assignedId(org));
        CrmOrganizationContactEntity c = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        if (!c.getOrganization().getId().equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
        }

        String prev = snapshot(c);
        c.setFullName(trimToNull(dto.fullName()));
        c.setDepartment(trimToNull(dto.department()));
        c.setPhone(trimToNull(dto.phone()));
        c.setEmail(trimToNull(dto.email()));
        c.setExtraNote(trimToNull(dto.extraNote()));
        CrmOrganizationContactEntity saved = contactRepository.save(c);
        String next = snapshot(saved);
        if (!prev.equals(next)) {
            AccountEntity changer = accountRepository.findById(principal.accountId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Account not found"));
            CrmOrganizationContactHistoryEntity h = new CrmOrganizationContactHistoryEntity();
            h.setContact(saved);
            h.setChangedBy(changer);
            h.setPreviousSnapshot(prev);
            h.setNewSnapshot(next);
            historyRepository.save(h);
        }
        return toContactResponse(saved);
    }

    @Override
    @Transactional
    public void deleteContact(UUID organizationId, UUID contactId) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = findOrg(organizationId);
        crmAccessPolicy.assertCanModifyOrganization(assignedId(org));
        CrmOrganizationContactEntity c = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        if (!c.getOrganization().getId().equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
        }
        contactRepository.delete(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmOrganizationContactResponseDto> listContacts(UUID organizationId) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = findOrg(organizationId);
        crmAccessPolicy.assertCanReadOrganization(assignedId(org));
        return contactRepository.findByOrganization_IdOrderByCreatedAtAsc(organizationId).stream()
                .map(this::toContactResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmOrganizationContactHistoryResponseDto> listContactHistory(UUID organizationId, UUID contactId) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = findOrg(organizationId);
        crmAccessPolicy.assertCanReadOrganization(assignedId(org));
        CrmOrganizationContactEntity c = contactRepository.findById(contactId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found"));
        if (!c.getOrganization().getId().equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Contact not found");
        }
        return historyRepository.findByContact_IdOrderByCreatedAtDesc(contactId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    private CrmOrganizationEntity findOrg(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));
    }

    private UUID assignedId(CrmOrganizationEntity entity) {
        return entity.getAssignedTo() != null ? entity.getAssignedTo().getId() : null;
    }

    private void applyOrgFields(CrmOrganizationEntity entity, String name, String externalNumber,
                                String legalAddress, String deliveryAddress, String floorNote, String commentGeneral,
                                String productTypesNote, String supplyVolumeNote, String supplyScheduleNote,
                                java.time.LocalDate cooperationUntil, CrmClientStatus clientStatus,
                                Double latitude, Double longitude) {
        entity.setName(name.trim());
        entity.setExternalNumber(trimToNull(externalNumber));
        entity.setLegalAddress(trimToNull(legalAddress));
        entity.setDeliveryAddress(trimToNull(deliveryAddress));
        entity.setFloorNote(trimToNull(floorNote));
        entity.setCommentGeneral(trimToNull(commentGeneral));
        entity.setProductTypesNote(trimToNull(productTypesNote));
        entity.setSupplyVolumeNote(trimToNull(supplyVolumeNote));
        entity.setSupplyScheduleNote(trimToNull(supplyScheduleNote));
        entity.setCooperationUntil(cooperationUntil);
        entity.setClientStatus(clientStatus);
        entity.setLatitude(latitude);
        entity.setLongitude(longitude);
    }

    private String trimToNull(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private String snapshot(CrmOrganizationContactEntity c) {
        return "fullName=" + n(c.getFullName())
                + ";department=" + n(c.getDepartment())
                + ";phone=" + n(c.getPhone())
                + ";email=" + n(c.getEmail())
                + ";extraNote=" + n(c.getExtraNote());
    }

    private String n(String s) {
        return s == null ? "" : s.replace(";", ",");
    }

    private CrmOrganizationResponseDto toResponse(CrmOrganizationEntity e) {
        AccountEntity assigned = e.getAssignedTo();
        return new CrmOrganizationResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getName(),
                e.getExternalNumber(),
                e.getLegalAddress(),
                e.getDeliveryAddress(),
                e.getFloorNote(),
                e.getCommentGeneral(),
                e.getProductTypesNote(),
                e.getSupplyVolumeNote(),
                e.getSupplyScheduleNote(),
                e.getCooperationUntil(),
                e.getClientStatus(),
                e.getLatitude(),
                e.getLongitude(),
                assigned != null ? assigned.getId() : null,
                assigned != null ? displayName(assigned) : null,
                e.getConvertedFromContactRequest() != null ? e.getConvertedFromContactRequest().getId() : null,
                e.getConvertedFromCooperationRequest() != null ? e.getConvertedFromCooperationRequest().getId() : null
        );
    }

    private CrmOrganizationContactResponseDto toContactResponse(CrmOrganizationContactEntity c) {
        return new CrmOrganizationContactResponseDto(
                c.getId(),
                c.getCreatedAt(),
                c.getUpdatedAt(),
                c.getOrganization().getId(),
                c.getFullName(),
                c.getDepartment(),
                c.getPhone(),
                c.getEmail(),
                c.getExtraNote()
        );
    }

    private CrmOrganizationContactHistoryResponseDto toHistoryResponse(CrmOrganizationContactHistoryEntity h) {
        return new CrmOrganizationContactHistoryResponseDto(
                h.getId(),
                h.getCreatedAt(),
                h.getContact().getId(),
                h.getChangedBy().getId(),
                displayName(h.getChangedBy()),
                h.getPreviousSnapshot(),
                h.getNewSnapshot()
        );
    }

    private String displayName(AccountEntity a) {
        if (StringUtils.hasText(a.getName())) {
            return a.getName();
        }
        String fn = a.getFirstName() != null ? a.getFirstName() : "";
        String ln = a.getLastName() != null ? a.getLastName() : "";
        String combined = (fn + " " + ln).trim();
        return combined.isEmpty() ? a.getEmail() : combined;
    }
}
