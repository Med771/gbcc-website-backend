package backend.website.gbcc.logic.crm.contract;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrmContractLineRepository extends JpaRepository<CrmContractLineEntity, UUID> {

    List<CrmContractLineEntity> findByContract_IdOrderByPlannedDateAsc(UUID contractId);
}
