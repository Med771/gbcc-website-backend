package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@ModelAttribute UploadFileRequestDto requestDto) {
        FileUploadResponseDto response = fileService.upload(requestDto);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{key}")
    public ResponseEntity<Resource> download(@PathVariable String key) {
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
    public ResponseEntity<Void> delete(@PathVariable String key) {
        fileService.delete(new FileKeyRequestDto(key));
        return ResponseEntity.noContent().build();
    }
}
