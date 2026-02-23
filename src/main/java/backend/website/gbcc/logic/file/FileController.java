package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
@Validated
public class FileController {

    private static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    private final FileService fileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@Valid @ModelAttribute UploadFileRequestDto requestDto) {
        FileUploadResponseDto response = fileService.upload(requestDto);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    public PageResponse<FileUploadResponseDto> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String bucket,
            Pageable pageable
    ) {
        FileSearchRequestDto requestDto = new FileSearchRequestDto(key, fileName, bucket);
        Page<FileUploadResponseDto> result = fileService.search(requestDto, pageable);
        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @GetMapping("/{key}")
    public ResponseEntity<Resource> download(
            @PathVariable
            @NotBlank(message = "key is required")
            @Pattern(regexp = UUID_PATTERN, message = "key must be a valid UUID")
            String key
    ) {
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
    public void delete(
            @PathVariable
            @NotBlank(message = "key is required")
            @Pattern(regexp = UUID_PATTERN, message = "key must be a valid UUID")
            String key
    ) {
        fileService.delete(new FileKeyRequestDto(key));
    }
}
