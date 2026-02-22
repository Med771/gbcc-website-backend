package backend.website.gbcc.logic.file.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UploadFileRequestDto {
    private MultipartFile file;
}
