package backend.website.gbcc.logic.support;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface SupportConversationRepository extends JpaRepository<SupportConversationEntity, UUID>,
        JpaSpecificationExecutor<SupportConversationEntity> {

    Optional<SupportConversationEntity> findByGuestAccessToken(String guestAccessToken);

    Page<SupportConversationEntity> findAllByCustomer_Id(UUID customerId, Pageable pageable);
}
