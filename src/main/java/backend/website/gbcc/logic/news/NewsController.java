package backend.website.gbcc.logic.news;

import backend.website.gbcc.config.OpenApiConstants;
import backend.website.gbcc.logic.news.dto.CreateNewsRequestDto;
import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import backend.website.gbcc.logic.news.dto.NewsSearchRequestDto;
import backend.website.gbcc.logic.news.dto.PatchNewsRequestDto;
import backend.website.gbcc.logic.news.dto.UpdateNewsRequestDto;
import backend.website.gbcc.model.NewsCategory;
import backend.website.gbcc.model.dto.PageResponse;
import backend.website.gbcc.model.error.ApiErrorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/news")
@RequiredArgsConstructor
@Validated
@Tag(name = "News", description = "Новости: публичное чтение (с фильтрацией опубликованных для не-менеджеров). Создание и правки — только ADMIN/OWNER с JWT.")
public class NewsController {

    private final NewsService newsService;

    @Operation(summary = "Создать новость", description = "Требует JWT. Только роли ADMIN или OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создана",
                    content = @Content(schema = @Schema(implementation = NewsResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Валидация",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Недостаточно прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NewsResponseDto create(@Valid @RequestBody CreateNewsRequestDto requestDto) {
        return newsService.create(requestDto);
    }

    @Operation(summary = "Полное обновление новости", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = NewsResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{newsId}")
    public NewsResponseDto update(
            @PathVariable UUID newsId,
            @Valid @RequestBody UpdateNewsRequestDto requestDto
    ) {
        return newsService.update(newsId, requestDto);
    }

    @Operation(summary = "Частичное обновление новости", description = "JWT. ADMIN/OWNER.")
    @SecurityRequirement(name = OpenApiConstants.SECURITY_ACCESS_COOKIE)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = NewsResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "Нет JWT",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Нет прав",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{newsId}")
    public NewsResponseDto patch(
            @PathVariable UUID newsId,
            @Valid @RequestBody PatchNewsRequestDto requestDto
    ) {
        return newsService.patch(newsId, requestDto);
    }

    @Operation(summary = "Список рубрик новостей", description = "Значения enum для фильтров на фронте.")
    @GetMapping("/categories")
    public List<NewsCategory> listCategories() {
        return newsService.listCategories();
    }

    @Operation(
            summary = "Получить новость по id",
            description = "Публично. Не-менеджеры видят только опубликованные и с датой публикации не в будущем."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Найдена",
                    content = @Content(schema = @Schema(implementation = NewsResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Нет доступа или не найдена",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @GetMapping("/{newsId}")
    public NewsResponseDto getById(@PathVariable UUID newsId) {
        return newsService.getById(newsId);
    }

    @Operation(summary = "Поиск новостей", description = "Параметры: query (текст), isPublished, category (рубрика). Пагинация Spring.")
    @ApiResponse(responseCode = "200", description = "Страница новостей",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public PageResponse<NewsResponseDto> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Boolean isPublished,
            @RequestParam(required = false) NewsCategory category,
            @Parameter(hidden = true) Pageable pageable
    ) {
        NewsSearchRequestDto requestDto = new NewsSearchRequestDto(query, isPublished, category);
        Page<NewsResponseDto> result = newsService.search(requestDto, pageable);
        return PageResponse.fromPage(result);
    }
}
