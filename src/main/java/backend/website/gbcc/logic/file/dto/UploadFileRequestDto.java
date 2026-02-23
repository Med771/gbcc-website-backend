package backend.website.gbcc.logic.file.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class UploadFileRequestDto {
    @NotNull(message = "file is required")
    private MultipartFile file;
}
