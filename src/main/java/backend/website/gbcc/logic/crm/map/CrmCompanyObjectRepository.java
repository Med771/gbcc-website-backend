package backend.website.gbcc.logic.crm.map;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CrmCompanyObjectRepository extends JpaRepository<CrmCompanyObjectEntity, UUID> {
}
