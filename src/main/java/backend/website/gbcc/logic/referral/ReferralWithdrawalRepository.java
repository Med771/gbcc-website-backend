package backend.website.gbcc.logic.referral;

import backend.website.gbcc.model.ReferralWithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface ReferralWithdrawalRepository extends JpaRepository<ReferralWithdrawalEntity, UUID> {

    @Query("SELECT COALESCE(SUM(w.amount), 0) FROM ReferralWithdrawalEntity w "
            + "WHERE w.account.id = :accountId AND w.status = :status")
    BigDecimal sumAmountByAccountAndStatus(
            @Param("accountId") UUID accountId,
            @Param("status") ReferralWithdrawalStatus status
    );

    @EntityGraph(attributePaths = {"account", "processedByAccount"})
    Page<ReferralWithdrawalEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
