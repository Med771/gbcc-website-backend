package backend.website.gbcc.logic.product;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"productClass", "productSeries", "productType"})
    Page<ProductEntity> findAll(@NonNull Specification<ProductEntity> spec, @NonNull Pageable pageable);
}
