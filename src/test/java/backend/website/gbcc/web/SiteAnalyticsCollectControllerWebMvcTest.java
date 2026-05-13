package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.analytics.SiteAnalyticsCollectController;
import backend.website.gbcc.logic.analytics.SiteAnalyticsCollectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = SiteAnalyticsCollectController.class)
class SiteAnalyticsCollectControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SiteAnalyticsCollectService collectService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    private static final String BODY = """
            {
              "visitorId": "00000000-0000-4000-8000-000000000099",
              "sessionId": "00000000-0000-4000-8000-000000000088",
              "events": [
                {
                  "occurredAt": "2026-05-13T10:00:00Z",
                  "eventType": "PAGE_VIEW",
                  "path": "/",
                  "referrer": null,
                  "metadata": null
                }
              ]
            }
            """;

    @Test
    void collect_withoutAuth_returns204() throws Exception {
        mockMvc.perform(post("/analytics/collect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isNoContent());
        verify(collectService).collect(any(), any(), any());
    }
}
