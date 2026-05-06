package backend.website.gbcc.logic.crm.lead;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmLeadRepository extends JpaRepository<CrmLeadEntity, UUID>, JpaSpecificationExecutor<CrmLeadEntity> {
}
