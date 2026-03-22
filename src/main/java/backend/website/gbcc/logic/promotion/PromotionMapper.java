package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import org.springframework.stereotype.Component;

@Component
public class PromotionMapper {

    public PromotionResponseDto toResponse(PromotionEntity entity) {
        return new PromotionResponseDto(
                entity.getId(),
                entity.getName(),
                entity.getDiscountPercent(),
                entity.getValidFrom(),
                entity.getValidTo(),
                entity.getIsActive(),
                entity.getPriority(),
                entity.getScope(),
                entity.getScopeReferenceId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
