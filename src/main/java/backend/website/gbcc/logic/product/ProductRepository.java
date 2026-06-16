package backend.website.gbcc.logic.product;

import jakarta.persistence.LockModeType;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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
    @EntityGraph(attributePaths = {"productClass", "productSeries"})
    Optional<ProductEntity> findById(@NonNull UUID id);

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"productClass", "productSeries"})
    Page<ProductEntity> findAll(@NonNull Specification<ProductEntity> spec, @NonNull Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT p FROM ProductEntity p
            LEFT JOIN FETCH p.productClass
            LEFT JOIN FETCH p.productSeries
            WHERE p.id IN :ids
            """)
    List<ProductEntity> findAllWithTaxonomyByIdsForUpdate(@Param("ids") Collection<UUID> ids);

    @Query("""
            SELECT DISTINCT p FROM ProductEntity p
            LEFT JOIN FETCH p.productClass
            LEFT JOIN FETCH p.productSeries
            WHERE p.id IN :ids
            """)
    List<ProductEntity> findAllWithTaxonomyByIds(@Param("ids") Collection<UUID> ids);
}
