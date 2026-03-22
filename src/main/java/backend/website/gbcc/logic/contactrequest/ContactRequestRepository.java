package backend.website.gbcc.logic.contactrequest;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ContactRequestRepository extends JpaRepository<ContactRequestEntity, UUID>,
        JpaSpecificationExecutor<ContactRequestEntity> {
}
