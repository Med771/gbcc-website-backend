package backend.website.gbcc.logic.crm.contract;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmContractRepository extends JpaRepository<CrmContractEntity, UUID>, JpaSpecificationExecutor<CrmContractEntity> {
}
