package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.cooperationrequest.CooperationRequestController;
import backend.website.gbcc.logic.cooperationrequest.CooperationRequestService;
import backend.website.gbcc.model.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleCooperationRequestResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = CooperationRequestController.class)
class CooperationRequestControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CooperationRequestService cooperationRequestService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_JSON = """
            {
              "name": "n",
              "phone": "+1",
              "email": "n@n.com",
              "cooperationType": "t",
              "comment": null,
              "attachmentFileId": null,
              "consentProcessing": true
            }
            """;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void create_public_returns201() throws Exception {
        when(cooperationRequestService.create(any())).thenReturn(sampleCooperationRequestResponse(UUID_1));

        mockMvc.perform(post("/cooperation-requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void searchForAdmin_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/cooperation-requests"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchForAdmin_withAdmin_returns200() throws Exception {
        when(cooperationRequestService.searchForAdmin(any(), any(), any())).thenReturn(
                new PageResponse<>(List.of(sampleCooperationRequestResponse(UUID_1)), 0, 20, 1, 1));

        mockMvc.perform(get("/cooperation-requests")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_withAdmin_returns200() throws Exception {
        when(cooperationRequestService.getByIdForAdmin(UUID_1)).thenReturn(sampleCooperationRequestResponse(UUID_1));

        mockMvc.perform(get("/cooperation-requests/{id}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void take_withAdmin_returns200() throws Exception {
        when(cooperationRequestService.take(UUID_1)).thenReturn(sampleCooperationRequestResponse(UUID_1));

        mockMvc.perform(post("/cooperation-requests/{id}/take", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isOk());

        verify(cooperationRequestService).take(UUID_1);
    }

    @Test
    void release_withAdmin_returns200() throws Exception {
        when(cooperationRequestService.release(UUID_1)).thenReturn(sampleCooperationRequestResponse(UUID_1));

        mockMvc.perform(post("/cooperation-requests/{id}/release", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isOk());

        verify(cooperationRequestService).release(UUID_1);
    }
}
