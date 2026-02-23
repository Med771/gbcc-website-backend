package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@Valid @ModelAttribute UploadFileRequestDto requestDto) {
        FileUploadResponseDto response = fileService.upload(requestDto);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<Resource> download(@PathVariable @NotBlank String key) {
        FileDownloadResponseDto response = fileService.download(new FileKeyRequestDto(key));
        Resource resource = response.getResource();

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String mimeType = response.getMimeType();
        if (StringUtils.hasText(mimeType)) {
            mediaType = MediaType.parseMediaType(mimeType);
        }

        HttpHeaders headers = new HttpHeaders();
        if (StringUtils.hasText(response.getFileName())) {
            headers.setContentDisposition(ContentDisposition.inline().filename(response.getFileName()).build());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(mediaType)
                .body(resource);
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @NotBlank String key) {
        fileService.delete(new FileKeyRequestDto(key));
    }
}
