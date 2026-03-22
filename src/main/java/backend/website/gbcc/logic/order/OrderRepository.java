package backend.website.gbcc.logic.order;

import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {

    @NonNull
    @Override
    @EntityGraph(attributePaths = {"customer"})
    Page<OrderEntity> findAll(@NonNull Specification<OrderEntity> spec, @NonNull Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"customer"})
    Optional<OrderEntity> findById(UUID id);

    @Query(value = "SELECT nextval('orders_display_number_seq')", nativeQuery = true)
    Long nextDisplayNumber();
}
