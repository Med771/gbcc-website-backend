package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.model.FileType;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FileMapperTest {

    private final FileMapper fileMapper = Mappers.getMapper(FileMapper.class);

    @Test
    void toUploadResponse_shouldContainKeyAndFileId() {
        UUID fileId = UUID.randomUUID();

        FileEntity entity = new FileEntity();
        entity.setId(fileId);
        entity.setKey(UUID.randomUUID().toString());
        entity.setBucket("storage");
        entity.setFileName("image.png");
        entity.setMimeType(FileType.IMAGE_PNG);
        entity.setSize(2048L);

        FileUploadResponseDto response = fileMapper.toUploadResponse(entity);

        assertThat(response.getFileId()).isEqualTo(fileId);
        assertThat(response.getKey()).isEqualTo(entity.getKey());
        assertThat(response.getBucket()).isEqualTo("storage");
        assertThat(response.getMimeType()).isEqualTo("image/png");
    }
}
