package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.referral.ReferralController;
import backend.website.gbcc.logic.referral.ReferralService;
import backend.website.gbcc.logic.referral.dto.ReferralMeResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_CUSTOMER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = ReferralController.class)
class ReferralControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReferralService referralService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CLICK_JSON = """
            {"code":"AB2-CD3F","visitorFingerprint":"v1"}
            """;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void recordClick_public_returns204() throws Exception {
        doNothing().when(referralService).recordClick(any());

        mockMvc.perform(post("/referral/clicks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CLICK_JSON))
                .andExpect(status().isNoContent());

        verify(referralService).recordClick(any());
    }

    @Test
    void getMe_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/referral/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMe_withCustomer_returns200() throws Exception {
        when(referralService.getMyReferralDashboard()).thenReturn(
                new ReferralMeResponseDto(
                        "AB2-CD3F",
                        "#AB2-CD3F",
                        "https://x/ref/AB2-CD3F",
                        1,
                        0,
                        new BigDecimal("0.00"),
                        new BigDecimal("1000"),
                        new BigDecimal("7"),
                        new BigDecimal("2"),
                        false,
                        null
                ));

        mockMvc.perform(get("/referral/me")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER)))
                .andExpect(status().isOk());
    }

    @Test
    void applyInviteCode_withCustomer_returns204() throws Exception {
        doNothing().when(referralService).applyInviteCode(any());

        mockMvc.perform(post("/referral/me/invite-code")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ZZ1-AA99\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateMyCode_withCustomer_returns204() throws Exception {
        doNothing().when(referralService).updateMyReferralCode(any());

        mockMvc.perform(patch("/referral/me/code")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"QQ1-WW88\"}"))
                .andExpect(status().isNoContent());
    }

    @Test
    void listAdminClicks_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/referral/admin/clicks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void listAdminClicks_withAdmin_returns200() throws Exception {
        when(referralService.listClicksForAdmin(any())).thenReturn(
                org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/referral/admin/clicks")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void listAdminRegistrations_withAdmin_returns200() throws Exception {
        when(referralService.listRegistrationsWithInviterForAdmin(any())).thenReturn(
                org.springframework.data.domain.Page.empty());

        mockMvc.perform(get("/referral/admin/registrations")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
