package backend.website.gbcc.logic.crm.interaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmInteractionRepository extends JpaRepository<CrmInteractionEntity, UUID>,
        JpaSpecificationExecutor<CrmInteractionEntity> {
}
