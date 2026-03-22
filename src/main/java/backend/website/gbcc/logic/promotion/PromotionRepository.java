package backend.website.gbcc.logic.promotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID>, JpaSpecificationExecutor<PromotionEntity> {

    @Query("""
            SELECT p FROM PromotionEntity p
            WHERE p.isActive = true
              AND (p.validFrom IS NULL OR p.validFrom <= :at)
              AND (p.validTo IS NULL OR p.validTo >= :at)
            """)
    List<PromotionEntity> findAllActiveAt(@Param("at") Instant at);
}
