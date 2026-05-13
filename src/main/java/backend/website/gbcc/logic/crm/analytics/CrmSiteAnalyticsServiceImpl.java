package backend.website.gbcc.logic.crm.analytics;

import backend.website.gbcc.config.property.AnalyticsProperty;
import backend.website.gbcc.logic.analytics.SiteAnalyticsEventEntity;
import backend.website.gbcc.logic.analytics.SiteAnalyticsEventRepository;
import backend.website.gbcc.logic.analytics.SiteAnalyticsEventSpecification;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsEventRowDto;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsPathCountDto;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsSummaryDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CrmSiteAnalyticsServiceImpl implements CrmSiteAnalyticsService {

    private final SiteAnalyticsEventRepository eventRepository;
    private final CrmAccessPolicy crmAccessPolicy;
    private final AnalyticsProperty analyticsProperty;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional(readOnly = true)
    public CrmSiteAnalyticsSummaryDto summary(Instant from, Instant to) {
        crmAccessPolicy.requireCrmUser();
        assertRange(from, to);
        long uniqueVisitors = eventRepository.countDistinctVisitors(from, to);
        long totalEvents = eventRepository.countEventsInRange(from, to);
        int topLimit = Math.max(1, analyticsProperty.getTopPathsLimit());
        List<SiteAnalyticsEventRepository.PathCountProjection> rows =
                eventRepository.findTopPaths(from, to, topLimit);
        List<CrmSiteAnalyticsPathCountDto> topPaths = rows.stream()
                .map(r -> new CrmSiteAnalyticsPathCountDto(
                        r.getPath(),
                        r.getCnt() == null ? 0L : r.getCnt()
                ))
                .toList();
        Instant since24h = Instant.now().minus(24, ChronoUnit.HOURS);
        long activeSessions = eventRepository.countDistinctSessionsSince(since24h);
        return new CrmSiteAnalyticsSummaryDto(uniqueVisitors, totalEvents, topPaths, activeSessions);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CrmSiteAnalyticsEventRowDto> events(Instant from, Instant to, Pageable pageable) {
        crmAccessPolicy.requireCrmUser();
        assertRange(from, to);
        Specification<SiteAnalyticsEventEntity> spec = SiteAnalyticsEventSpecification.receivedBetween(from, to);
        return eventRepository.findAll(spec, pageable).map(this::toDto);
    }

    private static void assertRange(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from and to are required");
        }
        if (!from.isBefore(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must be before to");
        }
    }

    private CrmSiteAnalyticsEventRowDto toDto(SiteAnalyticsEventEntity e) {
        return new CrmSiteAnalyticsEventRowDto(
                e.getId(),
                e.getOccurredAt(),
                e.getReceivedAt(),
                e.getVisitorId(),
                e.getSessionId(),
                e.getEventType(),
                e.getPath(),
                e.getReferrer(),
                e.getUserAgent(),
                metadataNode(e.getMetadataJson())
        );
    }

    private JsonNode metadataNode(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
