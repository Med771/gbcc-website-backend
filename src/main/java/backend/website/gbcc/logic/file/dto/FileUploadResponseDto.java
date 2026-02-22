package backend.website.gbcc.logic.file.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileUploadResponseDto {
    private String key;
    private String bucket;
    private String fileName;
    private String mimeType;
    private Long size;
}
