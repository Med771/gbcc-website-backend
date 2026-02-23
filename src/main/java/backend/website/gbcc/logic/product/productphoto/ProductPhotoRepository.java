package backend.website.gbcc.logic.product.productphoto;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductPhotoRepository extends JpaRepository<ProductPhotoEntity, UUID> {
    boolean existsByProduct_IdAndSortOrder(UUID productId, Integer sortOrder);

    List<ProductPhotoEntity> findAllByProduct_IdIn(List<UUID> productIds);
}
