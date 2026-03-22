package backend.website.gbcc.web;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.support.SupportController;
import backend.website.gbcc.logic.support.SupportService;
import backend.website.gbcc.model.dto.PageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_CUSTOMER;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleCreateSupportConversationResponse;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleSupportDetail;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleSupportSummary;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = SupportController.class)
class SupportControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SupportService supportService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String GUEST_CREATE_JSON = """
            {"subject":null,"message":"hello","guestName":"Guest","guestEmail":"g@g.com"}
            """;

    private static final String ADD_MESSAGE_JSON = "{\"message\":\"reply\"}";

    private static final String PATCH_STATUS_JSON = "{\"status\":\"CLOSED\"}";

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void createGuest_public_returns201() throws Exception {
        when(supportService.createConversation(any())).thenReturn(sampleCreateSupportConversationResponse(UUID_1));

        mockMvc.perform(post("/support/conversations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GUEST_CREATE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void listMine_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/support/conversations/my"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listMine_withCustomer_returns200() throws Exception {
        when(supportService.listMine(any())).thenReturn(
                new PageResponse<>(List.of(sampleSupportSummary(UUID_1)), 0, 20, 1, 1));

        mockMvc.perform(get("/support/conversations/my")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void searchForAdmin_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/support/conversations"))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchForAdmin_withAdmin_returns200() throws Exception {
        when(supportService.searchForAdmin(any(), any())).thenReturn(
                new PageResponse<>(List.of(sampleSupportSummary(UUID_1)), 0, 20, 1, 1));

        mockMvc.perform(get("/support/conversations")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void getConversation_public_returns200() throws Exception {
        when(supportService.getConversation(eq(UUID_1), isNull())).thenReturn(sampleSupportDetail(UUID_1));

        mockMvc.perform(get("/support/conversations/{conversationId}", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void addMessage_public_returns200() throws Exception {
        when(supportService.addMessage(eq(UUID_1), any(), isNull())).thenReturn(sampleSupportDetail(UUID_1));

        mockMvc.perform(post("/support/conversations/{conversationId}/messages", UUID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_MESSAGE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void addMessage_withGuestHeader_returns200() throws Exception {
        when(supportService.addMessage(eq(UUID_1), any(), eq("tok"))).thenReturn(sampleSupportDetail(UUID_1));

        mockMvc.perform(post("/support/conversations/{conversationId}/messages", UUID_1)
                        .header(OpenApiConstants.SUPPORT_TOKEN_HEADER_NAME, "tok")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ADD_MESSAGE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_withoutAuth_returns403() throws Exception {
        mockMvc.perform(patch("/support/conversations/{conversationId}/status", UUID_1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_STATUS_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateStatus_withAdmin_returns200() throws Exception {
        when(supportService.updateStatus(eq(UUID_1), any())).thenReturn(sampleSupportDetail(UUID_1));

        mockMvc.perform(patch("/support/conversations/{conversationId}/status", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_STATUS_JSON))
                .andExpect(status().isOk());
    }
}
