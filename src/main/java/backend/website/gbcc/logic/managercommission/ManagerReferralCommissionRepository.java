package backend.website.gbcc.logic.managercommission;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ManagerReferralCommissionRepository extends JpaRepository<ManagerReferralCommissionEntity, UUID> {

    boolean existsByOrder_Id(UUID orderId);

    Page<ManagerReferralCommissionEntity> findAllByManagerAccount_IdOrderByCreatedAtDesc(
            UUID managerAccountId,
            Pageable pageable
    );

    Page<ManagerReferralCommissionEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
