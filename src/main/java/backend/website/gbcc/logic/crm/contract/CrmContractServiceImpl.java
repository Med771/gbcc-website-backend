package backend.website.gbcc.logic.crm.contract;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CreateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractLineResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.CrmContractResponseDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractLineRequestDto;
import backend.website.gbcc.logic.crm.contract.dto.UpdateCrmContractRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
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

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmContractServiceImpl implements CrmContractService {

    private final CrmContractRepository contractRepository;
    private final CrmContractLineRepository lineRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional
    public CrmContractResponseDto create(CreateCrmContractRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = organizationRepository.findById(dto.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization not found"));
        assertOrg(org);

        CrmContractEntity e = new CrmContractEntity();
        e.setOrganization(org);
        e.setStartDate(dto.startDate());
        e.setEndDate(dto.endDate());
        e.setCommentText(trim(dto.commentText()));
        return toContractResponse(contractRepository.save(e));
    }

    @Override
    @Transactional
    public CrmContractResponseDto update(UUID id, UpdateCrmContractRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity e = findContract(id);
        assertOrg(e.getOrganization());
        e.setStartDate(dto.startDate());
        e.setEndDate(dto.endDate());
        e.setCommentText(trim(dto.commentText()));
        return toContractResponse(contractRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity e = findContract(id);
        assertOrg(e.getOrganization());
        contractRepository.delete(e);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmContractResponseDto getById(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity e = findContract(id);
        assertOrg(e.getOrganization());
        return toContractResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmContractResponseDto> search(UUID organizationId, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmContractEntity> spec = Specification.allOf(
                CrmContractVisibility.visibleFor(principal),
                CrmContractSpecification.organizationIdEq(organizationId)
        );
        Page<CrmContractResponseDto> page = contractRepository.findAll(spec, pageable).map(this::toContractResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public CrmContractLineResponseDto addLine(UUID contractId, CreateCrmContractLineRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity c = findContract(contractId);
        assertOrg(c.getOrganization());
        CrmContractLineEntity line = new CrmContractLineEntity();
        line.setContract(c);
        line.setPlannedDate(dto.plannedDate());
        line.setPlannedQuantity(dto.plannedQuantity());
        line.setLineStatus(dto.lineStatus());
        line.setCommentText(trim(dto.commentText()));
        return toLineResponse(lineRepository.save(line));
    }

    @Override
    @Transactional
    public CrmContractLineResponseDto updateLine(UUID contractId, UUID lineId, UpdateCrmContractLineRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity c = findContract(contractId);
        assertOrg(c.getOrganization());
        CrmContractLineEntity line = lineRepository.findById(lineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Line not found"));
        if (!line.getContract().getId().equals(contractId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line not found");
        }
        line.setPlannedDate(dto.plannedDate());
        line.setPlannedQuantity(dto.plannedQuantity());
        line.setLineStatus(dto.lineStatus());
        line.setCommentText(trim(dto.commentText()));
        return toLineResponse(lineRepository.save(line));
    }

    @Override
    @Transactional
    public void deleteLine(UUID contractId, UUID lineId) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity c = findContract(contractId);
        assertOrg(c.getOrganization());
        CrmContractLineEntity line = lineRepository.findById(lineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Line not found"));
        if (!line.getContract().getId().equals(contractId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Line not found");
        }
        lineRepository.delete(line);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmContractLineResponseDto> listLines(UUID contractId) {
        crmAccessPolicy.requireCrmUser();
        CrmContractEntity c = findContract(contractId);
        assertOrg(c.getOrganization());
        return lineRepository.findByContract_IdOrderByPlannedDateAsc(contractId).stream()
                .map(this::toLineResponse)
                .toList();
    }

    private CrmContractEntity findContract(UUID id) {
        return contractRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Contract not found"));
    }

    private void assertOrg(CrmOrganizationEntity org) {
        crmAccessPolicy.assertCanReadOrganization(
                org.getAssignedTo() != null ? org.getAssignedTo().getId() : null);
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CrmContractResponseDto toContractResponse(CrmContractEntity e) {
        return new CrmContractResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getOrganization().getId(),
                e.getStartDate(),
                e.getEndDate(),
                e.getCommentText()
        );
    }

    private CrmContractLineResponseDto toLineResponse(CrmContractLineEntity e) {
        return new CrmContractLineResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getContract().getId(),
                e.getPlannedDate(),
                e.getPlannedQuantity(),
                e.getLineStatus(),
                e.getCommentText()
        );
    }
}
