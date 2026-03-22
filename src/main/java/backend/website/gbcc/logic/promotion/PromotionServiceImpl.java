package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.promotion.dto.CreatePromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PatchPromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import backend.website.gbcc.logic.promotion.dto.PromotionSearchRequestDto;
import backend.website.gbcc.logic.promotion.dto.UpdatePromotionRequestDto;
import backend.website.gbcc.model.PromotionScopeType;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

    private static final String FORBIDDEN_NOT_MANAGER = "Only admin or owner can manage promotions";

    private final PromotionRepository promotionRepository;
    private final PromotionMapper promotionMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public PromotionResponseDto create(CreatePromotionRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        validateValidityWindow(requestDto.validFrom(), requestDto.validTo());
        validateScope(requestDto.scope(), requestDto.scopeReferenceId());

        PromotionEntity entity = new PromotionEntity();
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setDiscountPercent(requestDto.discountPercent());
        entity.setValidFrom(requestDto.validFrom());
        entity.setValidTo(requestDto.validTo());
        entity.setIsActive(requestDto.isActive());
        entity.setPriority(requestDto.priority());
        entity.setScope(requestDto.scope());
        entity.setScopeReferenceId(requestDto.scope() == PromotionScopeType.ALL ? null : requestDto.scopeReferenceId());

        return promotionMapper.toResponse(promotionRepository.save(entity));
    }

    @Override
    @Transactional
    public PromotionResponseDto update(UUID promotionId, UpdatePromotionRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        validateValidityWindow(requestDto.validFrom(), requestDto.validTo());
        validateScope(requestDto.scope(), requestDto.scopeReferenceId());

        PromotionEntity entity = findOrThrow(promotionId);
        entity.setName(normalizeRequired(requestDto.name(), "name"));
        entity.setDiscountPercent(requestDto.discountPercent());
        entity.setValidFrom(requestDto.validFrom());
        entity.setValidTo(requestDto.validTo());
        entity.setIsActive(requestDto.isActive());
        entity.setPriority(requestDto.priority());
        entity.setScope(requestDto.scope());
        entity.setScopeReferenceId(requestDto.scope() == PromotionScopeType.ALL ? null : requestDto.scopeReferenceId());

        return promotionMapper.toResponse(promotionRepository.save(entity));
    }

    @Override
    @Transactional
    public PromotionResponseDto patch(UUID promotionId, PatchPromotionRequestDto requestDto) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        PromotionEntity entity = findOrThrow(promotionId);

        if (StringUtils.hasText(requestDto.name())) {
            entity.setName(normalizeRequired(requestDto.name(), "name"));
        }
        if (requestDto.discountPercent() != null) {
            entity.setDiscountPercent(requestDto.discountPercent());
        }
        if (requestDto.validFrom() != null) {
            entity.setValidFrom(requestDto.validFrom());
        }
        if (requestDto.validTo() != null) {
            entity.setValidTo(requestDto.validTo());
        }
        if (requestDto.isActive() != null) {
            entity.setIsActive(requestDto.isActive());
        }
        if (requestDto.priority() != null) {
            entity.setPriority(requestDto.priority());
        }
        if (requestDto.scope() != null || requestDto.scopeReferenceId() != null) {
            PromotionScopeType scope = requestDto.scope() != null ? requestDto.scope() : entity.getScope();
            UUID ref = scope == PromotionScopeType.ALL
                    ? null
                    : (requestDto.scopeReferenceId() != null ? requestDto.scopeReferenceId() : entity.getScopeReferenceId());
            validateScope(scope, ref);
            entity.setScope(scope);
            entity.setScopeReferenceId(ref);
        }

        validateValidityWindow(entity.getValidFrom(), entity.getValidTo());

        return promotionMapper.toResponse(promotionRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public PromotionResponseDto getById(UUID promotionId) {
        PromotionEntity entity = findOrThrow(promotionId);
        if (!securityContextHelper.isCurrentPrincipalAdminOrOwner() && !isVisibleAt(entity, Instant.now())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found");
        }
        return promotionMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PromotionResponseDto> search(PromotionSearchRequestDto requestDto, Pageable pageable) {
        PromotionSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new PromotionSearchRequestDto(null, null, null);

        Specification<PromotionEntity> spec = PromotionSpecification.byFilter(safeRequest);
        if (!securityContextHelper.isCurrentPrincipalAdminOrOwner()) {
            spec = spec.and(PromotionSpecification.visibleAt(Instant.now()));
        }

        Page<PromotionResponseDto> page = promotionRepository.findAll(spec, pageable)
                .map(promotionMapper::toResponse);

        return PageResponse.fromPage(page);
    }

    @Override
    @Transactional
    public void delete(UUID promotionId) {
        securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        promotionRepository.delete(findOrThrow(promotionId));
    }

    private PromotionEntity findOrThrow(UUID promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Promotion not found"));
    }

    private void validateScope(PromotionScopeType scope, UUID scopeReferenceId) {
        if (scope == PromotionScopeType.ALL) {
            if (scopeReferenceId != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scopeReferenceId must be null when scope is ALL");
            }
            return;
        }
        if (scopeReferenceId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "scopeReferenceId is required when scope is not ALL");
        }
    }

    private void validateValidityWindow(Instant validFrom, Instant validTo) {
        if (validFrom != null && validTo != null && validFrom.isAfter(validTo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "validFrom must be before or equal to validTo");
        }
    }

    private boolean isVisibleAt(PromotionEntity entity, Instant at) {
        if (!Boolean.TRUE.equals(entity.getIsActive())) {
            return false;
        }
        if (entity.getValidFrom() != null && entity.getValidFrom().isAfter(at)) {
            return false;
        }
        return entity.getValidTo() == null || !entity.getValidTo().isBefore(at);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }
}
