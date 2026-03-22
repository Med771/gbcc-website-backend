package backend.website.gbcc.logic.promotion;

import backend.website.gbcc.logic.promotion.dto.CreatePromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PatchPromotionRequestDto;
import backend.website.gbcc.logic.promotion.dto.PromotionResponseDto;
import backend.website.gbcc.logic.promotion.dto.PromotionSearchRequestDto;
import backend.website.gbcc.logic.promotion.dto.UpdatePromotionRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PromotionService {

    PromotionResponseDto create(CreatePromotionRequestDto requestDto);

    PromotionResponseDto update(UUID promotionId, UpdatePromotionRequestDto requestDto);

    PromotionResponseDto patch(UUID promotionId, PatchPromotionRequestDto requestDto);

    PromotionResponseDto getById(UUID promotionId);

    PageResponse<PromotionResponseDto> search(PromotionSearchRequestDto requestDto, Pageable pageable);

    void delete(UUID promotionId);
}
