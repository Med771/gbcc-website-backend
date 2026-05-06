package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.crm.organization.CrmClientStatus;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationController;
import backend.website.gbcc.logic.crm.organization.CrmOrganizationService;
import backend.website.gbcc.logic.crm.organization.dto.CrmOrganizationResponseDto;
import backend.website.gbcc.model.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = CrmOrganizationController.class)
class CrmOrganizationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CrmOrganizationService organizationService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void search_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/crm/organizations").param("page", "0").param("size", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    void search_withAdmin_returns200() throws Exception {
        when(organizationService.search(any(), any())).thenReturn(new PageResponse<>(
                List.of(sampleOrg()), 0, 20, 1, 1));

        mockMvc.perform(get("/crm/organizations")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    private static final Instant T0 = Instant.parse("2025-01-01T00:00:00Z");

    private static CrmOrganizationResponseDto sampleOrg() {
        return new CrmOrganizationResponseDto(
                UUID_1,
                T0,
                T0,
                "Org",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                CrmClientStatus.ACTIVE,
                null,
                null,
                UUID_1,
                "Admin",
                null,
                null
        );
    }
}
