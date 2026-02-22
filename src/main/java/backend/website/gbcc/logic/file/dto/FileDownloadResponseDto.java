package backend.website.gbcc.logic.file.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.core.io.Resource;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FileDownloadResponseDto {
    private Resource resource;
    private String fileName;
    private String mimeType;
}
