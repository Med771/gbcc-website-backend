package backend.website.gbcc.logic.crm.supply;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.contract.CrmContractLineEntity;
import backend.website.gbcc.logic.crm.contract.CrmContractLineRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationBranchEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationBranchRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.supply.dto.CreateCrmSupplyRequestDto;
import backend.website.gbcc.logic.crm.supply.dto.CrmSupplyResponseDto;
import backend.website.gbcc.logic.crm.supply.dto.UpdateCrmSupplyRequestDto;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.logic.product.ProductRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmSupplyServiceImpl implements CrmSupplyService {

    private final CrmSupplyRepository supplyRepository;
    private final CrmOrganizationRepository organizationRepository;
    private final CrmOrganizationBranchRepository branchRepository;
    private final CrmContractLineRepository contractLineRepository;
    private final ProductRepository productRepository;
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
        applyFields(e, dto.supplyAt(), dto.supplyTime(), dto.productDescription(), dto.quantity(), dto.status(),
                dto.commentText(), dto.deliveryAddress(), dto.objectName(), dto.deliveryLatitude(),
                dto.deliveryLongitude(), dto.branchId(), dto.productId(), dto.contractLineId(), org.getId());
        return toResponse(supplyRepository.save(e));
    }

    @Override
    @Transactional
    public CrmSupplyResponseDto update(UUID id, UpdateCrmSupplyRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmSupplyEntity e = findSupply(id);
        assertOrgAccess(e.getOrganization());
        applyFields(e, dto.supplyAt(), dto.supplyTime(), dto.productDescription(), dto.quantity(), dto.status(),
                dto.commentText(), dto.deliveryAddress(), dto.objectName(), dto.deliveryLatitude(),
                dto.deliveryLongitude(), dto.branchId(), dto.productId(), dto.contractLineId(),
                e.getOrganization().getId());
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
    public PageResponse<CrmSupplyResponseDto> search(UUID organizationId, LocalDate supplyAtFrom, LocalDate supplyAtTo,
                                                       CrmSupplyStatus status, Pageable pageable) {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmSupplyEntity> spec = buildSearchSpec(principal, organizationId, supplyAtFrom, supplyAtTo, status);
        Page<CrmSupplyResponseDto> page = supplyRepository.findAll(spec, pageable).map(this::toResponse);
        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmSupplyResponseDto> calendar(LocalDate from, LocalDate to, UUID organizationId, CrmSupplyStatus status) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to dates are required");
        }
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        Specification<CrmSupplyEntity> spec = buildSearchSpec(principal, organizationId, from, to, status);
        return supplyRepository.findAll(spec).stream().map(this::toResponse).toList();
    }

    private Specification<CrmSupplyEntity> buildSearchSpec(AccountPrincipal principal, UUID organizationId,
                                                           LocalDate supplyAtFrom, LocalDate supplyAtTo,
                                                           CrmSupplyStatus status) {
        return Specification.allOf(
                CrmSupplyVisibility.visibleFor(principal),
                CrmSupplySpecification.organizationIdEq(organizationId),
                CrmSupplySpecification.supplyAtBetween(supplyAtFrom, supplyAtTo),
                CrmSupplySpecification.statusEq(status)
        );
    }

    private void applyFields(CrmSupplyEntity e, LocalDate supplyAt, java.time.LocalTime supplyTime,
                             String productDescription, java.math.BigDecimal quantity, CrmSupplyStatus status,
                             String commentText, String deliveryAddress, String objectName,
                             Double deliveryLatitude, Double deliveryLongitude, UUID branchId, UUID productId,
                             UUID contractLineId, UUID organizationId) {
        e.setSupplyAt(supplyAt);
        e.setSupplyTime(supplyTime);
        e.setProductDescription(productDescription.trim());
        e.setQuantity(quantity);
        e.setStatus(status);
        e.setCommentText(trim(commentText));
        e.setDeliveryAddress(trim(deliveryAddress));
        e.setObjectName(trim(objectName));
        e.setDeliveryLatitude(deliveryLatitude);
        e.setDeliveryLongitude(deliveryLongitude);
        e.setBranch(resolveBranch(branchId, organizationId));
        e.setProduct(resolveProduct(productId));
        e.setContractLine(resolveLine(contractLineId));
    }

    private CrmSupplyEntity findSupply(UUID id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Supply not found"));
    }

    private void assertOrgAccess(CrmOrganizationEntity org) {
        crmAccessPolicy.assertCanReadOrganization(
                org.getAssignedTo() != null ? org.getAssignedTo().getId() : null);
    }

    private CrmOrganizationBranchEntity resolveBranch(UUID branchId, UUID organizationId) {
        if (branchId == null) {
            return null;
        }
        CrmOrganizationBranchEntity branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch not found"));
        if (!branch.getOrganization().getId().equals(organizationId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Branch does not belong to organization");
        }
        return branch;
    }

    private ProductEntity resolveProduct(UUID productId) {
        if (productId == null) {
            return null;
        }
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product not found"));
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
                e.getSupplyTime(),
                e.getProductDescription(),
                e.getQuantity(),
                e.getStatus(),
                e.getCommentText(),
                e.getDeliveryAddress(),
                e.getObjectName(),
                e.getDeliveryLatitude(),
                e.getDeliveryLongitude(),
                e.getBranch() != null ? e.getBranch().getId() : null,
                e.getProduct() != null ? e.getProduct().getId() : null,
                e.getContractLine() != null ? e.getContractLine().getId() : null
        );
    }
}
