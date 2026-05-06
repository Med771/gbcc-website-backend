package backend.website.gbcc.logic.crm.map;

import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.map.dto.CrmCompanyObjectResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CrmMapPinResponseDto;
import backend.website.gbcc.logic.crm.map.dto.CrmMapPinType;
import backend.website.gbcc.logic.crm.map.dto.CreateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.map.dto.UpdateCrmCompanyObjectRequestDto;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationEntity;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationHasCoordinates;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationRepository;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationSpecification;
import backend.website.gbcc.logic.crm.supply.CrmSupplyEntity;
import backend.website.gbcc.logic.crm.supply.CrmSupplyHasDeliveryCoordinates;
import backend.website.gbcc.logic.crm.supply.CrmSupplyRepository;
import backend.website.gbcc.logic.crm.supply.CrmSupplyVisibility;
import backend.website.gbcc.model.AccountPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CrmMapServiceImpl implements CrmMapService {

    private final CrmOrganizationRepository organizationRepository;
    private final CrmSupplyRepository supplyRepository;
    private final CrmCompanyObjectRepository companyObjectRepository;
    private final CrmAccessPolicy crmAccessPolicy;

    @Override
    @Transactional(readOnly = true)
    public List<CrmMapPinResponseDto> pins() {
        AccountPrincipal principal = crmAccessPolicy.requireCrmUser();
        List<CrmMapPinResponseDto> out = new ArrayList<>();

        Specification<CrmOrganizationEntity> orgSpec = Specification.allOf(
                CrmOrganizationSpecification.accessibleBy(principal),
                CrmOrganizationHasCoordinates.hasCoordinates()
        );
        organizationRepository.findAll(orgSpec).forEach(o -> out.add(new CrmMapPinResponseDto(
                CrmMapPinType.ORGANIZATION,
                o.getId(),
                o.getLatitude(),
                o.getLongitude(),
                o.getName(),
                abbrev(o.getLegalAddress()),
                o.getClientStatus().name()
        )));

        Specification<CrmSupplyEntity> supSpec = Specification.allOf(
                CrmSupplyVisibility.visibleFor(principal),
                CrmSupplyHasDeliveryCoordinates.hasDeliveryCoordinates()
        );
        supplyRepository.findAll(supSpec).forEach(s -> out.add(new CrmMapPinResponseDto(
                CrmMapPinType.SUPPLY,
                s.getId(),
                s.getDeliveryLatitude(),
                s.getDeliveryLongitude(),
                s.getProductDescription(),
                s.getOrganization().getName() + " · " + s.getSupplyAt(),
                s.getStatus().name()
        )));

        companyObjectRepository.findAll().forEach(c -> out.add(new CrmMapPinResponseDto(
                CrmMapPinType.COMPANY_OBJECT,
                c.getId(),
                c.getLatitude(),
                c.getLongitude(),
                c.getName(),
                abbrev(c.getCommentText()),
                ""
        )));

        return out;
    }

    @Override
    @Transactional
    public CrmCompanyObjectResponseDto createCompanyObject(CreateCrmCompanyObjectRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmCompanyObjectEntity e = new CrmCompanyObjectEntity();
        e.setName(dto.name().trim());
        e.setLatitude(dto.latitude());
        e.setLongitude(dto.longitude());
        e.setCommentText(trim(dto.commentText()));
        return toObjResponse(companyObjectRepository.save(e));
    }

    @Override
    @Transactional
    public CrmCompanyObjectResponseDto updateCompanyObject(UUID id, UpdateCrmCompanyObjectRequestDto dto) {
        crmAccessPolicy.requireCrmUser();
        CrmCompanyObjectEntity e = findObj(id);
        e.setName(dto.name().trim());
        e.setLatitude(dto.latitude());
        e.setLongitude(dto.longitude());
        e.setCommentText(trim(dto.commentText()));
        return toObjResponse(companyObjectRepository.save(e));
    }

    @Override
    @Transactional
    public void deleteCompanyObject(UUID id) {
        crmAccessPolicy.requireCrmUser();
        companyObjectRepository.delete(findObj(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrmCompanyObjectResponseDto> listCompanyObjects() {
        crmAccessPolicy.requireCrmUser();
        return companyObjectRepository.findAll().stream().map(this::toObjResponse).toList();
    }

    private CrmCompanyObjectEntity findObj(UUID id) {
        return companyObjectRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company object not found"));
    }

    private String trim(String s) {
        if (!StringUtils.hasText(s)) {
            return null;
        }
        return s.trim();
    }

    private String abbrev(String s) {
        if (!StringUtils.hasText(s)) {
            return "";
        }
        String t = s.trim();
        return t.length() > 120 ? t.substring(0, 117) + "..." : t;
    }

    private CrmCompanyObjectResponseDto toObjResponse(CrmCompanyObjectEntity e) {
        return new CrmCompanyObjectResponseDto(
                e.getId(),
                e.getCreatedAt(),
                e.getUpdatedAt(),
                e.getName(),
                e.getLatitude(),
                e.getLongitude(),
                e.getCommentText()
        );
    }
}
