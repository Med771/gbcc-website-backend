package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Files", description = "Загрузка файлов (multipart), поиск метаданных, скачивание и удаление по UUID-ключу. Файлы хранятся локально (см. app.file.bucket).")
public class FileController {

    private static final String UUID_PATTERN =
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";

    private final FileService fileService;

    @Operation(
            summary = "Загрузить файл",
            description = "Content-Type: multipart/form-data. Поля см. UploadFileRequestDto (файл + опционально bucket/fileName)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Файл сохранён, в ответе key (UUID) и метаданные",
                    content = @Content(schema = @Schema(implementation = FileUploadResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Некорректный запрос",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileUploadResponseDto> upload(@Valid @ModelAttribute UploadFileRequestDto requestDto) {
        FileUploadResponseDto response = fileService.upload(requestDto);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Поиск файлов по метаданным", description = "Фильтры: key, fileName, bucket. Пагинация Spring.")
    @ApiResponse(responseCode = "200", description = "Страница записей",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public PageResponse<FileUploadResponseDto> search(
            @RequestParam(required = false) String key,
            @RequestParam(required = false) String fileName,
            @RequestParam(required = false) String bucket,
            @Parameter(hidden = true) Pageable pageable
    ) {
        FileSearchRequestDto requestDto = new FileSearchRequestDto(key, fileName, bucket);
        Page<FileUploadResponseDto> result = fileService.search(requestDto, pageable);
        return PageResponse.fromPage(result);
    }

    @Operation(summary = "Скачать файл", description = "key в пути — UUID. Ответ: бинарное тело, Content-Type из сохранённого MIME.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Содержимое файла",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
            @ApiResponse(responseCode = "400", description = "Некорректный UUID",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Файл не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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

    @Operation(summary = "Удалить файл", description = "Удаляет запись и объект с диска по UUID key.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Удалено"),
            @ApiResponse(responseCode = "400", description = "Некорректный UUID",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
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
