package backend.website.gbcc.logic.product.productclass;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductClassRepository extends JpaRepository<ProductClassEntity, UUID>,
        JpaSpecificationExecutor<ProductClassEntity> {
    Optional<ProductClassEntity> findByNameIgnoreCase(String name);

    List<ProductClassEntity> findAllByOrderByNameAsc();
}
