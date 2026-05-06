package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.account.AccountController;
import backend.website.gbcc.logic.account.AccountService;
import backend.website.gbcc.model.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_CUSTOMER;
import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_OWNER;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_2;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleAccountResponse;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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

@GbccWebMvcTest(controllers = AccountController.class)
class AccountControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void createAdmin_withOwner_returns201() throws Exception {
        when(accountService.createAdmin(any())).thenReturn(sampleAccountResponse(AccountRole.ADMIN));

        mockMvc.perform(post("/account/admin")
                        .with(ControllerTestSupport.principal(PRINCIPAL_OWNER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin\",\"email\":\"admin@a.com\",\"password\":\"password12\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void createAdmin_withCustomer_returns403() throws Exception {
        when(accountService.createAdmin(any())).thenThrow(new ResponseStatusException(FORBIDDEN));

        mockMvc.perform(post("/account/admin")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin\",\"email\":\"admin@a.com\",\"password\":\"password12\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAdmin_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/account/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Admin\",\"email\":\"admin@a.com\",\"password\":\"password12\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerCustomer_public_returns201() throws Exception {
        when(accountService.registerCustomer(any())).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(post("/account/customer/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"patronymic\":null,\"phone\":\"+1\",\"email\":\"c@c.com\",\"password\":\"password12\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void getMe_withoutAuth_returns403() throws Exception {
        mockMvc.perform(get("/account/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getMe_withCustomer_returns200() throws Exception {
        when(accountService.getMyProfile()).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(get("/account/me").with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER)))
                .andExpect(status().isOk());
    }

    @Test
    void putMe_withCustomer_returns200() throws Exception {
        when(accountService.updateMyProfile(any())).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(put("/account/me")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"patronymic\":null,\"phone\":\"+1\",\"email\":\"c@c.com\",\"password\":null}"))
                .andExpect(status().isOk());
    }

    @Test
    void putCustomer_withAuth_returns200() throws Exception {
        when(accountService.updateCustomer(eq(UUID_2), any())).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(put("/account/customer/{accountId}", UUID_2)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"A\",\"lastName\":\"B\",\"patronymic\":null,\"phone\":\"+1\",\"email\":\"c@c.com\",\"password\":null}"))
                .andExpect(status().isOk());
    }

    @Test
    void activateCustomer_withAuth_returns200() throws Exception {
        when(accountService.activateCustomer(eq(UUID_2), any())).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(patch("/account/customer/{accountId}/activate", UUID_2)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password12\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteCustomer_withAuth_returns204() throws Exception {
        mockMvc.perform(delete("/account/customer/{accountId}", UUID_2)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isNoContent());

        verify(accountService).deleteCustomer(UUID_2);
    }

    @Test
    void getById_withAuth_returns200() throws Exception {
        when(accountService.getById(UUID_2)).thenReturn(sampleAccountResponse(AccountRole.CUSTOMER));

        mockMvc.perform(get("/account/{accountId}", UUID_2).with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void search_withAuth_returns200() throws Exception {
        when(accountService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleAccountResponse(AccountRole.CUSTOMER)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/account")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void block_withAuth_returns204() throws Exception {
        mockMvc.perform(patch("/account/{accountId}/block", UUID_2)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isNoContent());

        verify(accountService).block(UUID_2);
    }

    @Test
    void unblock_withAuth_returns204() throws Exception {
        mockMvc.perform(patch("/account/{accountId}/unblock", UUID_2)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN)))
                .andExpect(status().isNoContent());

        verify(accountService).unblock(UUID_2);
    }
}
