package backend.website.gbcc.logic.customeranalytics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerAnalyticsRepository extends JpaRepository<CustomerAnalyticsEntity, UUID> {
}
