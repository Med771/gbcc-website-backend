package backend.website.gbcc.logic.product;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"productClass", "productSeries", "productType"})
    Optional<ProductEntity> findById(@NonNull UUID id);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"productClass", "productSeries", "productType"})
    Page<ProductEntity> findAll(@NonNull Specification<ProductEntity> spec, @NonNull Pageable pageable);

    @Query("""
            SELECT DISTINCT p FROM ProductEntity p
            LEFT JOIN FETCH p.productClass
            LEFT JOIN FETCH p.productSeries
            LEFT JOIN FETCH p.productType
            WHERE p.id IN :ids
            """)
    List<ProductEntity> findAllWithTaxonomyByIds(@Param("ids") Collection<UUID> ids);
}
