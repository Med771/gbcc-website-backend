package backend.website.gbcc.logic.analytics;

import backend.website.gbcc.config.property.AnalyticsProperty;
import backend.website.gbcc.config.property.JwtProperty;
import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectEventDto;
import backend.website.gbcc.logic.analytics.dto.SiteAnalyticsCollectRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SiteAnalyticsCollectServiceImplTest {

    @Mock
    private SiteAnalyticsEventRepository eventRepository;

    private AnalyticsProperty analyticsProperty;
    private final JwtProperty jwtProperty = new JwtProperty();

    private SiteAnalyticsCollectServiceImpl service;

    @BeforeEach
    void setUp() {
        analyticsProperty = new AnalyticsProperty();
        service = new SiteAnalyticsCollectServiceImpl(eventRepository, analyticsProperty, jwtProperty);
    }

    @Test
    void whenDisabled_doesNotPersist() {
        analyticsProperty.setEnabled(false);
        var body = request(oneEvent(Instant.now()));
        var req = new MockHttpServletRequest();
        var res = new MockHttpServletResponse();
        service.collect(body, req, res);
        verify(eventRepository, never()).saveAll(anyList());
    }

    @Test
    void whenDntHonor_skipsPersist() {
        analyticsProperty.setHonorDnt(true);
        var body = request(oneEvent(Instant.now()));
        var req = new MockHttpServletRequest();
        req.addHeader("DNT", "1");
        var res = new MockHttpServletResponse();
        service.collect(body, req, res);
        verify(eventRepository, never()).saveAll(anyList());
    }

    @Test
    void batchTooLarge_throwsBadRequest() {
        analyticsProperty.setMaxBatchSize(1);
        var body = new SiteAnalyticsCollectRequestDto(
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(oneEvent(Instant.now()), oneEvent(Instant.now()))
        );
        assertThatThrownBy(() -> service.collect(body, new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    @Test
    void savesNormalizedPathAndTrimsUserAgent() {
        Instant occurred = Instant.now();
        UUID visitor = UUID.randomUUID();
        UUID session = UUID.randomUUID();
        var ev = new SiteAnalyticsCollectEventDto(occurred, "PAGE_VIEW", "catalog", null, null);
        var body = new SiteAnalyticsCollectRequestDto(visitor, session, List.of(ev));

        var req = new MockHttpServletRequest();
        req.addHeader("User-Agent", "x".repeat(600));
        var res = new MockHttpServletResponse();

        service.collect(body, req, res);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SiteAnalyticsEventEntity>> cap = ArgumentCaptor.forClass(List.class);
        verify(eventRepository).saveAll(cap.capture());
        SiteAnalyticsEventEntity row = cap.getValue().getFirst();
        assertThat(row.getPath()).isEqualTo("/catalog");
        assertThat(row.getUserAgent()).hasSize(512);
        assertThat(row.getVisitorId()).isEqualTo(visitor);
        assertThat(res.getHeader("Set-Cookie")).contains("GBCC_VISITOR_ID=" + visitor);
    }

    @Test
    void metadataTooLarge_throwsBadRequest() {
        analyticsProperty.setMaxMetadataChars(4);
        var ev = new SiteAnalyticsCollectEventDto(Instant.now(), "CUSTOM", "/", null, Map.of("k", "hello"));
        assertThatThrownBy(() -> service.collect(
                request(ev),
                new MockHttpServletRequest(),
                new MockHttpServletResponse()
        )).isInstanceOf(ResponseStatusException.class);
    }

    private static SiteAnalyticsCollectRequestDto request(SiteAnalyticsCollectEventDto ev) {
        return new SiteAnalyticsCollectRequestDto(UUID.randomUUID(), UUID.randomUUID(), List.of(ev));
    }

    private static SiteAnalyticsCollectEventDto oneEvent(Instant occurredAt) {
        return new SiteAnalyticsCollectEventDto(occurredAt, "PAGE_VIEW", "/", null, null);
    }
}
