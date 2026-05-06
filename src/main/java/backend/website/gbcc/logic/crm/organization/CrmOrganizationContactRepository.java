package backend.website.gbcc.logic.crm.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrmOrganizationContactRepository extends JpaRepository<CrmOrganizationContactEntity, UUID> {

    List<CrmOrganizationContactEntity> findByOrganization_IdOrderByCreatedAtAsc(UUID organizationId);
}
