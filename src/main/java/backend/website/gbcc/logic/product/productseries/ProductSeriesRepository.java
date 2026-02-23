package backend.website.gbcc.logic.product.productseries;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductSeriesRepository extends JpaRepository<ProductSeriesEntity, UUID> {
    Optional<ProductSeriesEntity> findByNameIgnoreCase(String name);
}
