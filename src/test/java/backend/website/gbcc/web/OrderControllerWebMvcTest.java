package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.order.OrderController;
import backend.website.gbcc.logic.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_ADMIN;
import static backend.website.gbcc.web.WebMvcTestFixtures.PRINCIPAL_CUSTOMER;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleOrderResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = OrderController.class)
class OrderControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_ORDER_JSON = """
            {
              "deliveryAddress": "addr",
              "customerComment": null,
              "paymentMethod": "CARD_ONLINE",
              "items": [
                {"productId": "00000000-0000-4000-8000-000000000002", "quantity": 1}
              ]
            }
            """;

    private static final String PATCH_STATUS_JSON = """
            {
              "status": "PROCESSING",
              "estimatedDeliveryAt": null,
              "estimatedDeliveryEnd": null,
              "receiptUrl": null,
              "comment": null
            }
            """;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void create_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withCustomer_returns201() throws Exception {
        when(orderService.create(any())).thenReturn(sampleOrderResponse(UUID_1));

        mockMvc.perform(post("/order")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_ORDER_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void getById_withAuth_returns200() throws Exception {
        when(orderService.getById(UUID_1)).thenReturn(sampleOrderResponse(UUID_1));

        mockMvc.perform(get("/order/{orderId}", UUID_1).with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER)))
                .andExpect(status().isOk());
    }

    @Test
    void search_withAuth_returns200() throws Exception {
        when(orderService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleOrderResponse(UUID_1)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/order")
                        .with(ControllerTestSupport.principal(PRINCIPAL_CUSTOMER))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_withAdmin_returns200() throws Exception {
        when(orderService.updateStatus(eq(UUID_1), any())).thenReturn(sampleOrderResponse(UUID_1));

        mockMvc.perform(patch("/order/{orderId}/status", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PATCH_STATUS_JSON))
                .andExpect(status().isOk());
    }
}
