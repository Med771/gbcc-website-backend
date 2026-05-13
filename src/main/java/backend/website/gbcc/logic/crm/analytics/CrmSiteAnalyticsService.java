package backend.website.gbcc.logic.crm.analytics;

import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsEventRowDto;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface CrmSiteAnalyticsService {

    CrmSiteAnalyticsSummaryDto summary(Instant from, Instant to);

    Page<CrmSiteAnalyticsEventRowDto> events(Instant from, Instant to, Pageable pageable);
}
