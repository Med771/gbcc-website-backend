package backend.website.gbcc.logic.referral;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReferralCommissionRepository extends JpaRepository<ReferralCommissionEntity, UUID> {

    boolean existsByOrder_Id(UUID orderId);

    long countByReferrerAccount_Id(UUID referrerAccountId);

    long countByReferrerAccount_IdAndRefereeAccount_Id(UUID referrerAccountId, UUID refereeAccountId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM ReferralCommissionEntity c WHERE c.referrerAccount.id = :referrerId")
    BigDecimal sumCommissionByReferrer(@Param("referrerId") UUID referrerId);

    @Query("SELECT COALESCE(SUM(c.commissionAmount), 0) FROM ReferralCommissionEntity c "
            + "WHERE c.referrerAccount.id = :referrerId AND c.refereeAccount.id = :refereeId")
    BigDecimal sumCommissionByReferrerAndReferee(
            @Param("referrerId") UUID referrerId,
            @Param("refereeId") UUID refereeId
    );

    @EntityGraph(attributePaths = {"referrerAccount", "refereeAccount", "order"})
    Page<ReferralCommissionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
