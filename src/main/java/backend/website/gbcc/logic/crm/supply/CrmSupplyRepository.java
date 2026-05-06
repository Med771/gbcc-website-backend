package backend.website.gbcc.logic.crm.supply;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmSupplyRepository extends JpaRepository<CrmSupplyEntity, UUID>, JpaSpecificationExecutor<CrmSupplyEntity> {
}
