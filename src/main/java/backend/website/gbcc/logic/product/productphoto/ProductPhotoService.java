package backend.website.gbcc.logic.product.productphoto;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProductPhotoService {
    void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto);

    Map<UUID, List<UUID>> getPhotoIdsByProductIds(List<UUID> productIds);
}
