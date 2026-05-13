package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.crm.analytics.CrmSiteAnalyticsController;
import backend.website.gbcc.logic.crm.analytics.CrmSiteAnalyticsService;
import backend.website.gbcc.logic.crm.analytics.dto.CrmSiteAnalyticsSummaryDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_OWNER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = CrmSiteAnalyticsController.class)
class CrmSiteAnalyticsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrmSiteAnalyticsService siteAnalyticsService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void summary_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/crm/analytics/summary")
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-02T00:00:00Z"))
                .andExpect(status().isForbidden());
    }

    @Test
    void summary_withOwner_returns200() throws Exception {
        Instant from = Instant.parse("2026-01-01T00:00:00Z");
        Instant to = Instant.parse("2026-01-02T00:00:00Z");
        when(siteAnalyticsService.summary(eq(from), eq(to)))
                .thenReturn(new CrmSiteAnalyticsSummaryDto(1, 2, List.of(), 0));

        mockMvc.perform(get("/crm/analytics/summary")
                        .with(ControllerTestSupport.principal(PRINCIPAL_OWNER))
                        .param("from", from.toString())
                        .param("to", to.toString()))
                .andExpect(status().isOk());
    }

    @Test
    void events_withOwner_returns200() throws Exception {
        when(siteAnalyticsService.events(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/crm/analytics/events")
                        .with(ControllerTestSupport.principal(PRINCIPAL_OWNER))
                        .param("from", "2026-01-01T00:00:00Z")
                        .param("to", "2026-01-03T00:00:00Z")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
