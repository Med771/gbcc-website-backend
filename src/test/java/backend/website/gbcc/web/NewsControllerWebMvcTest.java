package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.news.NewsController;
import backend.website.gbcc.logic.news.NewsService;
import backend.website.gbcc.model.NewsCategory;
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
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleNewsResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = NewsController.class)
class NewsControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NewsService newsService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String CREATE_JSON = """
            {
              "title": "t",
              "previewText": "p",
              "content": "c",
              "category": "PRODUCTS",
              "coverFileId": null,
              "isPublished": true,
              "publishedAt": null
            }
            """;

    private static final String UPDATE_JSON = """
            {
              "title": "t",
              "previewText": "p",
              "content": "c",
              "category": "PRODUCTS",
              "coverFileId": null,
              "isPublished": true,
              "publishedAt": null
            }
            """;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void create_withoutAuth_returns403() throws Exception {
        mockMvc.perform(post("/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void create_withAdmin_returns201() throws Exception {
        when(newsService.create(any())).thenReturn(sampleNewsResponse(UUID_1));

        mockMvc.perform(post("/news")
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    void update_withAdmin_returns200() throws Exception {
        when(newsService.update(eq(UUID_1), any())).thenReturn(sampleNewsResponse(UUID_1));

        mockMvc.perform(put("/news/{newsId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void patch_withAdmin_returns200() throws Exception {
        when(newsService.patch(eq(UUID_1), any())).thenReturn(sampleNewsResponse(UUID_1));

        mockMvc.perform(patch("/news/{newsId}", UUID_1)
                        .with(ControllerTestSupport.principal(PRINCIPAL_ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    void listCategories_returns200() throws Exception {
        when(newsService.listCategories()).thenReturn(List.of(NewsCategory.PRODUCTS));

        mockMvc.perform(get("/news/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_returns200() throws Exception {
        when(newsService.getById(UUID_1)).thenReturn(sampleNewsResponse(UUID_1));

        mockMvc.perform(get("/news/{newsId}", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void search_returns200() throws Exception {
        when(newsService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleNewsResponse(UUID_1)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/news").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }
}
