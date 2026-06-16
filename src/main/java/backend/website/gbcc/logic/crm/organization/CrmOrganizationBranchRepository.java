package backend.website.gbcc.logic.crm.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CrmOrganizationBranchRepository extends JpaRepository<CrmOrganizationBranchEntity, UUID> {

    List<CrmOrganizationBranchEntity> findByOrganization_IdOrderByIsDefaultDescCreatedAtAsc(UUID organizationId);

    long countByOrganization_Id(UUID organizationId);
}
