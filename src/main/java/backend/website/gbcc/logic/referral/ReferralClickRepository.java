package backend.website.gbcc.logic.referral;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReferralClickRepository extends JpaRepository<ReferralClickEntity, UUID> {

    long countByReferrerAccount_Id(UUID referrerAccountId);

    @EntityGraph(attributePaths = "referrerAccount")
    Page<ReferralClickEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
