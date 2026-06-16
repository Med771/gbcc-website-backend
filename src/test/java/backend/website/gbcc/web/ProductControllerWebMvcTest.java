package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.product.ProductController;
import backend.website.gbcc.logic.product.ProductService;
import backend.website.gbcc.logic.product.dto.GroupedCatalogSearchResponseDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
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
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_2;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleProductResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = ProductController.class)
class ProductControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_JSON = """
            {
              "className": "Class",
              "seriesName": null,
              "brand": "Brand",
              "description": null,
              "tagline": null,
              "tags": null,
              "deliveryText": null,
              "licensesText": null,
              "heightMm": 100,
              "widthMm": 100,
              "lengthMm": 100,
              "weightKg": 1.0,
              "price": 100.00,
              "discountPercent": 0,
              "isActive": true
            }
            """;

    private static final String UPDATE_JSON = """
            {
              "className": "Class",
              "seriesName": null,
              "brand": "Brand",
              "description": null,
              "tagline": null,
              "tags": null,
              "deliveryText": null,
              "licensesText": null,
              "interestCount": null,
              "heightMm": 100,
              "widthMm": 100,
              "lengthMm": 100,
              "weightKg": 1.0,
              "price": 100.00,
              "discountPercent": 0,
              "isActive": true
            }
            """;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void create_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/product")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withAdmin_returns201() throws Exception {
        when(productService.create(any())).thenReturn(sampleProductResponse(UUID_1));

        mockMvc.perform(post("/product")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void update_returns200() throws Exception {
        when(productService.update(eq(UUID_1), any())).thenReturn(sampleProductResponse(UUID_1));

        mockMvc.perform(put("/product/{productId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void patch_returns200() throws Exception {
        when(productService.patch(eq(UUID_1), any())).thenReturn(sampleProductResponse(UUID_1));

        mockMvc.perform(patch("/product/{productId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void getClasses_returns200() throws Exception {
        when(productService.getClasses()).thenReturn(List.of(new ProductClassResponseDto(UUID_1, "Cat")));

        mockMvc.perform(get("/product/classes"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returns200() throws Exception {
        when(productService.getById(UUID_1)).thenReturn(sampleProductResponse(UUID_1));

        mockMvc.perform(get("/product/{productId}", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void findSimilar_returns200() throws Exception {
        when(productService.findSimilar(eq(UUID_1), eq(8), isNull()))
                .thenReturn(List.of(sampleProductResponse(UUID_2)));

        mockMvc.perform(get("/product/{productId}/similar", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void searchGrouped_returns200() throws Exception {
        when(productService.searchGrouped(eq("q"), eq(5), eq(10), isNull()))
                .thenReturn(new GroupedCatalogSearchResponseDto(0, 0, 0, List.of(), List.of()));

        mockMvc.perform(get("/product/search/grouped").param("q", "q"))
                .andExpect(status().isOk());
    }

    @Test
    void search_returns200() throws Exception {
        when(productService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleProductResponse(UUID_1)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/product").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void attachPhoto_returns204() throws Exception {
        mockMvc.perform(post("/product/{productId}/photos", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fileId\":\"00000000-0000-4000-8000-000000000002\",\"sortOrder\":0}"))
                .andExpect(status().isNoContent());

        verify(productService).attachPhoto(eq(UUID_1), any());
    }

    @Test
    void detachPhoto_returns204() throws Exception {
        mockMvc.perform(delete("/product/{productId}/photos", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .param("fileId", UUID_2.toString()))
                .andExpect(status().isNoContent());

        verify(productService).detachPhotoByFileId(eq(UUID_1), eq(UUID_2));
    }
}
