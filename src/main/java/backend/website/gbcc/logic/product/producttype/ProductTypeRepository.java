package backend.website.gbcc.logic.product.producttype;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductTypeRepository extends JpaRepository<ProductTypeEntity, UUID> {
    Optional<ProductTypeEntity> findByNameIgnoreCase(String name);
}
