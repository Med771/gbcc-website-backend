package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import backend.website.gbcc.model.FileType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.core.io.Resource;

@Mapper(componentModel = "spring")
public interface FileMapper {

    @Mapping(target = "key", source = "key")
    @Mapping(target = "bucket", source = "bucket")
    @Mapping(target = "fileName", source = "requestDto.file.originalFilename")
    @Mapping(target = "mimeType", expression = "java(resolveFileType(requestDto))")
    @Mapping(target = "size", expression = "java(resolveFileSize(requestDto))")
    FileEntity toEntity(UploadFileRequestDto requestDto, String key, String bucket);

    @Mapping(target = "mimeType", expression = "java(entity.getMimeType() != null ? entity.getMimeType().getMimeType() : null)")
    FileUploadResponseDto toUploadResponse(FileEntity entity);

    @Mapping(target = "resource", source = "resource")
    @Mapping(target = "fileName", source = "entity.fileName")
    @Mapping(target = "mimeType", expression = "java(entity.getMimeType() != null ? entity.getMimeType().getMimeType() : null)")
    FileDownloadResponseDto toDownloadResponse(FileEntity entity, Resource resource);

    default FileType resolveFileType(UploadFileRequestDto requestDto) {
        if (requestDto == null || requestDto.getFile() == null) {
            return FileType.OTHER;
        }
        return FileType.fromMimeType(requestDto.getFile().getContentType());
    }

    default long resolveFileSize(UploadFileRequestDto requestDto) {
        if (requestDto == null || requestDto.getFile() == null) {
            return 0L;
        }
        return requestDto.getFile().getSize();
    }
}
