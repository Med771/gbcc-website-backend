package backend.website.gbcc.logic.crm.task;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmTaskRepository extends JpaRepository<CrmTaskEntity, UUID>, JpaSpecificationExecutor<CrmTaskEntity> {
}
