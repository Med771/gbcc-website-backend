package backend.website.gbcc.logic.crm.analytics;

import backend.website.gbcc.config.property.AnalyticsProperty;
import backend.website.gbcc.logic.analytics.SiteAnalyticsEventRepository;
import backend.website.gbcc.logic.crm.access.CrmAccessPolicy;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsSummaryDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CrmSiteAnalyticsServiceImplTest {

    @Mock
    private SiteAnalyticsEventRepository eventRepository;

    @Mock
    private CrmAccessPolicy crmAccessPolicy;

    private final AnalyticsProperty analyticsProperty = new AnalyticsProperty();

    private CrmSiteAnalyticsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CrmSiteAnalyticsServiceImpl(eventRepository, crmAccessPolicy, analyticsProperty);
    }

    @Test
    void summary_aggregatesRepository() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(java.util.UUID.randomUUID(), AccountRole.OWNER));
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        when(eventRepository.countDistinctVisitors(from, to)).thenReturn(3L);
        when(eventRepository.countEventsInRange(from, to)).thenReturn(10L);
        SiteAnalyticsEventRepository.PathCountProjection row = new SiteAnalyticsEventRepository.PathCountProjection() {
            @Override
            public String getPath() {
                return "/p";
            }

            @Override
            public Long getCnt() {
                return 5L;
            }
        };
        when(eventRepository.findTopPaths(eq(from), eq(to), anyInt())).thenReturn(List.of(row));
        when(eventRepository.countDistinctSessionsSince(any())).thenReturn(2L);

        CrmSiteAnalyticsSummaryDto dto = service.summary(from, to);

        assertThat(dto.uniqueVisitors()).isEqualTo(3);
        assertThat(dto.totalEvents()).isEqualTo(10);
        assertThat(dto.topPaths()).hasSize(1);
        assertThat(dto.topPaths().getFirst().path()).isEqualTo("/p");
        assertThat(dto.topPaths().getFirst().eventCount()).isEqualTo(5);
        assertThat(dto.activeSessionsLast24Hours()).isEqualTo(2);
    }

    @Test
    void summary_invalidRange_throws() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(java.util.UUID.randomUUID(), AccountRole.ADMIN));
        Instant t = Instant.now();
        assertThatThrownBy(() -> service.summary(t, t))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void events_delegatesToRepository() {
        when(crmAccessPolicy.requireCrmUser()).thenReturn(new AccountPrincipal(java.util.UUID.randomUUID(), AccountRole.OWNER));
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-03T00:00:00Z");
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        service.events(from, to, PageRequest.of(0, 10));

        verify(eventRepository).findAll(any(Specification.class), any(Pageable.class));
    }
}
