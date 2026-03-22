package backend.website.gbcc.web;

import backend.website.gbcc.filter.JwtAuthenticationFilter;
import backend.website.gbcc.logic.file.FileController;
import backend.website.gbcc.logic.file.FileService;
import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static backend.website.gbcc.web.WebMvcTestFixtures.UUID_1;
import static backend.website.gbcc.web.WebMvcTestFixtures.sampleFileUploadResponse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@GbccWebMvcTest(controllers = FileController.class)
class FileControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileService fileService;
    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        ControllerTestSupport.stubJwtFilterPassThrough(jwtAuthenticationFilter);
    }

    @Test
    void upload_returns201() throws Exception {
        when(fileService.upload(any())).thenReturn(sampleFileUploadResponse(UUID_1));

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "data".getBytes());

        mockMvc.perform(multipart("/file").file(file))
                .andExpect(status().isCreated());
    }

    @Test
    void search_returns200() throws Exception {
        when(fileService.search(any(), any())).thenReturn(
                new PageImpl<>(List.of(sampleFileUploadResponse(UUID_1)), PageRequest.of(0, 20), 1));

        mockMvc.perform(get("/file").param("page", "0").param("size", "20"))
                .andExpect(status().isOk());
    }

    @Test
    void download_returns200() throws Exception {
        FileDownloadResponseDto dto = new FileDownloadResponseDto();
        dto.setFileId(UUID_1);
        dto.setResource(new ByteArrayResource("x".getBytes()));
        dto.setFileName("a.bin");
        dto.setMimeType("application/octet-stream");
        when(fileService.download(any())).thenReturn(dto);

        mockMvc.perform(get("/file/{key}", UUID_1))
                .andExpect(status().isOk());
    }

    @Test
    void delete_returns204() throws Exception {
        mockMvc.perform(delete("/file/{key}", UUID_1))
                .andExpect(status().isNoContent());

        verify(fileService).delete(any());
    }
}
