package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.promotion.PromotionController;
import backend.website.gbcc.logic.promotion.PromotionService;
import backend.website.gbcc.logic.promotion.dto.PromotionSearchRequestDto;
import backend.website.gbcc.model.PromotionScopeType;
import backend.website.gbcc.model.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.samplePromotionResponse;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = PromotionController.class)
class PromotionControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PromotionService promotionService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_JSON = """
            {
              "name": "Summer",
              "discountPercent": 10,
              "validFrom": null,
              "validTo": null,
              "isActive": true,
              "priority": 1,
              "scope": "ALL",
              "scopeReferenceId": null
            }
            """;

    private static final String UPDATE_JSON = CREATE_JSON;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void search_public_returns200() throws Exception {
        when(promotionService.search(any(), any())).thenReturn(
                new PageResponse<>(List.of(samplePromotionResponse(UUID_1)), 0, 20, 1, 1));

        mockMvc.perform(get("/promotion").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void search_public_passesFilterParamsToService() throws Exception {
        when(promotionService.search(any(), any())).thenReturn(
                new PageResponse<>(List.of(samplePromotionResponse(UUID_1)), 0, 20, 1, 1));

        mockMvc.perform(get("/promotion")
                        .param("page", "0")
                        .param("size", "20")
                        .param("name", "PromoX")
                        .param("isActive", "false")
                        .param("scope", "PRODUCT"))
                .andExpect(status().isOk());

        ArgumentCaptor<PromotionSearchRequestDto> captor = ArgumentCaptor.forClass(PromotionSearchRequestDto.class);
        verify(promotionService).search(captor.capture(), any());
        assertThat(captor.getValue().name()).isEqualTo("PromoX");
        assertThat(captor.getValue().isActive()).isFalse();
        assertThat(captor.getValue().scope()).isEqualTo(PromotionScopeType.PRODUCT);
    }

    @Test
    void getById_public_returns200() throws Exception {
        when(promotionService.getById(UUID_1)).thenReturn(samplePromotionResponse(UUID_1));

        mockMvc.perform(get("/promotion/{promotionId}", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void create_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/promotion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withAdmin_returns201() throws Exception {
        when(promotionService.create(any())).thenReturn(samplePromotionResponse(UUID_1));

        mockMvc.perform(post("/promotion")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void update_withAdmin_returns200() throws Exception {
        when(promotionService.update(eq(UUID_1), any())).thenReturn(samplePromotionResponse(UUID_1));

        mockMvc.perform(put("/promotion/{promotionId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void patch_withAdmin_returns200() throws Exception {
        when(promotionService.patch(eq(UUID_1), any())).thenReturn(samplePromotionResponse(UUID_1));

        mockMvc.perform(patch("/promotion/{promotionId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void delete_withAdmin_returns204() throws Exception {
        mockMvc.perform(delete("/promotion/{promotionId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isNoContent());

        verify(promotionService).delete(UUID_1);
    }
}
