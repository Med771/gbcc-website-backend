package backend.website.gbcc.logic.product.productclass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductClassRepository extends JpaRepository<ProductClassEntity, UUID> {
    Optional<ProductClassEntity> findByNameIgnoreCase(String name);
}
