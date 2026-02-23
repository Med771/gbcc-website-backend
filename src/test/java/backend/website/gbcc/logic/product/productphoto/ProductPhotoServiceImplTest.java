package backend.website.gbcc.logic.product.productphoto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class ProductPhotoServiceImplTest {

    @Mock
    private ProductPhotoRepository productPhotoRepository;

    @InjectMocks
    private ProductPhotoServiceImpl productPhotoService;

    @Test
    void detachPhotoByFileId_shouldDeleteByProductAndFile_whenRelationExists() {
        UUID productId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(productPhotoRepository.deleteByProduct_IdAndFileId(productId, fileId)).thenReturn(1L);

        productPhotoService.detachPhotoByFileId(productId, fileId);

        verify(productPhotoRepository).deleteByProduct_IdAndFileId(productId, fileId);
    }

    @Test
    void detachPhotoByFileId_shouldThrowNotFound_whenRelationMissing() {
        UUID productId = UUID.randomUUID();
        UUID fileId = UUID.randomUUID();

        when(productPhotoRepository.deleteByProduct_IdAndFileId(productId, fileId)).thenReturn(0L);

        assertThatThrownBy(() -> productPhotoService.detachPhotoByFileId(productId, fileId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
                    assertThat(ex.getReason()).isEqualTo("Product photo relation not found");
                });
    }

    @Test
    void detachPhotoByFileId_shouldThrowBadRequest_whenFileIdIsNull() {
        assertThatThrownBy(() -> productPhotoService.detachPhotoByFileId(UUID.randomUUID(), null))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("File id must not be empty");
                });
    }

    @Test
    void detachPhotoByFileId_shouldThrowBadRequest_whenProductIdIsNull() {
        assertThatThrownBy(() -> productPhotoService.detachPhotoByFileId(null, UUID.randomUUID()))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("Product id must not be empty");
                });
    }
}
