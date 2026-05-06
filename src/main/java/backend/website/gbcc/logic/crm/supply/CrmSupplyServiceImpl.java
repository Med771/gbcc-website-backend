package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.contract.CrmContractLineEntity;
import backend.website.gbcc.logic.crm.contract.CrmContractLineRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.supply.dto.CreateCrmSupplyRequestDto;
import backend.website.gbcc.logic.crm.supply.dto.CrmSupplyResponseDto;
import backend.website.gbcc.logic.crm.supply.dto.UpdateCrmSupplyRequestDto;
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
public class CrmSupplyServiceImpl implements CrmSupplyService {

    private final CrmSupplyRepository supplyRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final CrmContractLineRepository contractLineRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional
    public CrmSupplyResponseDto create(CreateCrmSupplyRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmOrganizationEntity org = organizationRepository.findById(dto.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organization not found"));
        crmAccessPolicy.assertCanModifyOrganization(
                org.getAssignedTo() != null ? org.getAssignedTo().getId() : null);

        CrmSupplyEntity e = new CrmSupplyEntity();
        e.setOrganization(org);
        e.setSupplyAt(dto.supplyAt());
        e.setProductDescription(dto.productDescription().trim());
        e.setQuantity(dto.quantity());
        e.setStatus(dto.status());
        e.setCommentText(trim(dto.commentText()));
        e.setDeliveryLatitude(dto.deliveryLatitude());
        e.setDeliveryLongitude(dto.deliveryLongitude());
        e.setContractLine(resolveLine(dto.contractLineId()));
        return toResponse(supplyRepository.save(e));
    }

    @Override
    @Transactional
    public CrmSupplyResponseDto update(UUID id, UpdateCrmSupplyRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmSupplyEntity e = findSupply(id);
        assertOrgAccess(e.getOrganization());

        e.setSupplyAt(dto.supplyAt());
        e.setProductDescription(dto.productDescription().trim());
        e.setQuantity(dto.quantity());
        e.setStatus(dto.status());
        e.setCommentText(trim(dto.commentText()));
        e.setDeliveryLatitude(dto.deliveryLatitude());
        e.setDeliveryLongitude(dto.deliveryLongitude());
        e.setContractLine(resolveLine(dto.contractLineId()));
        return toResponse(supplyRepository.save(e));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmSupplyEntity e = findSupply(id);
        assertOrgAccess(e.getOrganization());
        supplyRepository.delete(e);
    }

    @Override
    @Transactional(readOnly = true)
    public CrmSupplyResponseDto getById(UUID id) {
        crmAccessPolicy.requireCrmUser();
        CrmSupplyEntity e = findSupply(id);
        assertOrgAccess(e.getOrganization());
        return toResponse(e);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CrmSupplyResponseDto> search(UUID organizationId, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmSupplyEntity> spec = Specification.allOf(
                CrmSupplyVisibility.visibleFor(principal),
                CrmSupplySpecification.organizationIdEq(organizationId)
        );
        Page<CrmSupplyResponseDto> page = supplyRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    private CrmSupplyEntity findSupply(UUID id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supply not found"));
    }

    private void assertOrgAccess(CrmOrganizationEntity org) {
        crmAccessPolicy.assertCanReadOrganization(
                org.getAssignedTo() != null ? org.getAssignedTo().getId() : null);
    }

    private CrmContractLineEntity resolveLine(UUID lineId) {
        if (lineId == null) {
            return null;
        }
        return contractLineRepository.findById(lineId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Contract line not found"));
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private CrmSupplyResponseDto toResponse(CrmSupplyEntity e) {
        return new CrmSupplyResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getOrganization().getId(),
                e.getSupplyAt(),
                e.getProductDescription(),
                e.getQuantity(),
                e.getStatus(),
                e.getCommentText(),
                e.getDeliveryLatitude(),
                e.getDeliveryLongitude(),
                e.getContractLine() != null ? e.getContractLine().getId() : null
        );
    }
}
