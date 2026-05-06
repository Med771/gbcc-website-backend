package backend.website.gbcc.logic.crm.organization;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface CrmOrganizationRepository extends JpaRepository<CrmOrganizationEntity, UUID>,
        JpaSpecificationExecutor<CrmOrganizationEntity> {
}
