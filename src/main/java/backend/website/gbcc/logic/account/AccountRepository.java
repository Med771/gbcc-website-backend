package backend.website.gbcc.logic.account;

import backend.website.gbcc.model.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<AccountEntity, UUID>, JpaSpecificationExecutor<AccountEntity> {
    Optional<AccountEntity> findByEmailIgnoreCase(String email);

    boolean existsByRole(AccountRole role);

    Optional<AccountEntity> findByReferralCode(String referralCode);

    Page<AccountEntity> findByReferredBy_IdOrderByCreatedAtDesc(UUID referredById, Pageable pageable);

    @EntityGraph(attributePaths = "referredBy")
    Page<AccountEntity> findByReferredByIsNotNullOrderByCreatedAtDesc(Pageable pageable);
}
