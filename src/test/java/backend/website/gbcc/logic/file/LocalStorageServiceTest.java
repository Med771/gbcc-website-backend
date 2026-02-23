package backend.website.gbcc.logic.file;

import backend.website.gbcc.config.property.FileProperty;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class LocalStorageServiceTest {

    @Mock
    private FileRepository fileRepository;

    @Mock
    private FileMapper fileMapper;

    private LocalStorageService localStorageService;

    @BeforeEach
    void setUp() {
        FileProperty fileProperty = new FileProperty();
        fileProperty.setBucket("storage");
        localStorageService = new LocalStorageService(fileProperty, fileRepository, fileMapper);
    }

    @Test
    void download_shouldThrowBadRequest_whenKeyIsNotUuid() {
        assertThatThrownBy(() -> localStorageService.download(new FileKeyRequestDto("not-a-uuid")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("Invalid file key format");
                });

        verifyNoInteractions(fileRepository, fileMapper);
    }

    @Test
    void upload_shouldThrowBadRequest_whenFileIsEmpty() {
        MultipartFile multipartFile = org.mockito.Mockito.mock(MultipartFile.class);
        org.mockito.Mockito.when(multipartFile.isEmpty()).thenReturn(true);

        UploadFileRequestDto request = new UploadFileRequestDto();
        request.setFile(multipartFile);

        assertThatThrownBy(() -> localStorageService.upload(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("File must not be empty");
                });

        verifyNoInteractions(fileRepository, fileMapper);
    }

    @Test
    void search_shouldReturnMappedPage() {
        FileEntity entity = new FileEntity();
        entity.setId(UUID.randomUUID());
        entity.setKey(UUID.randomUUID().toString());
        entity.setBucket("storage");

        FileUploadResponseDto dto = new FileUploadResponseDto();
        dto.setFileId(entity.getId());
        dto.setKey(entity.getKey());
        dto.setBucket("storage");

        when(fileRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1));
        when(fileMapper.toUploadResponse(entity)).thenReturn(dto);

        var result = localStorageService.search(new FileSearchRequestDto(null, null, null), PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getFileId()).isEqualTo(entity.getId());
        assertThat(result.getContent().getFirst().getKey()).isEqualTo(entity.getKey());
    }
}
