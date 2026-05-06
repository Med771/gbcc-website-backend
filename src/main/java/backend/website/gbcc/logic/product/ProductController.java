package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.GroupedCatalogSearchResponseDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/product")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = """
        Каталог: CRUD товара, поиск с фильтрами, сгруппированный поиск /product/search/grouped для мобильного меню,
        привязка фото (файл по UUID). Создание и изменение товара и фото — только JWT ролей ADMIN или OWNER (см. SecurityConfig и ProductServiceImpl).
        """)
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "Создать товар", description = "Создаёт товар с классом/серией/типом (по имени или существующим id), ценой, скидкой и габаритами.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Создан",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Валидация или бизнес-правила",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Конфликт уникальности товара",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(@Valid @RequestBody CreateProductRequestDto requestDto) {
        return productService.create(requestDto);
    }

    @Operation(summary = "Полное обновление товара", description = "PUT — заменяет все обязательные поля согласно DTO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка данных",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PutMapping("/{productId}")
    public ProductResponseDto update(
            @Parameter(description = "UUID товара") @PathVariable UUID productId,
            @Valid @RequestBody UpdateProductRequestDto requestDto
    ) {
        return productService.update(productId, requestDto);
    }

    @Operation(summary = "Частичное обновление товара", description = "PATCH — только переданные поля.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Обновлено",
                    content = @Content(schema = @Schema(implementation = ProductResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Ошибка данных",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PatchMapping("/{productId}")
    public ProductResponseDto patch(
            @PathVariable UUID productId,
            @Valid @RequestBody PatchProductRequestDto requestDto
    ) {
        return productService.patch(productId, requestDto);
    }

    @Operation(summary = "Список классов товаров", description = "Справочник product_class для фильтров и форм.")
    @ApiResponse(responseCode = "200", description = "Список классов",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping("/classes")
    public List<ProductClassResponseDto> getClasses() {
        return productService.getClasses();
    }

    @Operation(
            summary = "Карточка товара",
            description = """
                    Публично. Полная информация для страницы товара: описание, слоган, теги, тексты вкладок «Доставка»/«Лицензии»,
                    счётчик «интересовались», галерея `photoFileIds`, цены, габариты, `isActive` (в наличии / нет).
                    """
    )
    @ApiResponse(responseCode = "200", description = "Товар",
            content = @Content(schema = @Schema(implementation = ProductResponseDto.class)))
    @ApiResponse(responseCode = "404", description = "Не найден",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{productId}")
    public ProductResponseDto getById(
            @Parameter(description = "UUID товара") @PathVariable UUID productId
    ) {
        return productService.getById(productId);
    }

    @Operation(
            summary = "Похожие товары",
            description = "Та же категория (класс), исключая текущий товар. По умолчанию только активные (`onlyActive=true`)."
    )
    @ApiResponse(responseCode = "200", description = "Список товаров",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @ApiResponse(responseCode = "404", description = "Текущий товар не найден",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @GetMapping("/{productId}/similar")
    public List<ProductResponseDto> findSimilar(
            @Parameter(description = "UUID товара") @PathVariable UUID productId,
            @RequestParam(defaultValue = "8") @Min(1) @Max(24) int limit,
            @RequestParam(required = false) Boolean onlyActive
    ) {
        return productService.findSimilar(productId, limit, onlyActive);
    }

    @Operation(
            summary = "Сгруппированный поиск (мобильное меню)",
            description = """
                    Один запрос **q** — совпадения по подстроке (как в обычном поиске товаров) возвращаются в двух блоках:
                    **categories** — классы товаров (категории каталога); **products** — товары (оборудование).
                    Поля **totalCategoryCount** / **totalProductCount** — полное число совпадений; в списках — только первые
                    `categoryLimit` / `productLimit` записей (превью). Полный список товаров — через **GET /product** с тем же `q`.
                    По умолчанию учитываются только активные товары (`isActive=true`).
                    """
    )
    @ApiResponse(responseCode = "200", description = "Категории и товары с количествами",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping("/search/grouped")
    public GroupedCatalogSearchResponseDto searchGrouped(
            @Parameter(description = "Текст поиска (обязателен; пустой или только спецсимволы — пустой ответ)")
            @RequestParam(name = "q") String query,
            @Parameter(description = "Сколько классов в превью (1–50)")
            @RequestParam(defaultValue = "5") @Min(1) @Max(50) int categoryLimit,
            @Parameter(description = "Сколько товаров в превью (1–50)")
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) int productLimit,
            @Parameter(description = "Фильтр активности товара (по умолчанию только активные)")
            @RequestParam(required = false) Boolean isActive
    ) {
        return productService.searchGrouped(query, categoryLimit, productLimit, isActive);
    }

    @Operation(
            summary = "Поиск товаров (каталог)",
            description = """
                    Страница каталога: фильтры комбинируются по **И**.
                    - **q** — подстрока в бренде, описании, названиях класса / серии / типа.
                    - **classId** — UUID категории (из GET /product/classes); альтернатива префиксу **className**.
                    - **seriesIds** / **typeIds** — повторяющиеся параметры (`?typeIds=u1&typeIds=u2`) для чекбоксов «серия / тип»;
                      если заданы, префиксные **seriesName** / **typeName** не используются для этой оси.
                    - **minPrice** / **maxPrice**, **minHeightMm** / **maxHeightMm** — диапазоны (высота — габарит «Размеры» в UI).
                    - **isActive** — для витрины обычно `true` (в наличии / активные карточки).
                    Пагинация: `page`, `size`. Сортировка `sort`: например `sort=popularityScore,desc` (популярность),
                    `sort=price,asc`, `sort=createdAt,desc` (новизна), `sort=isActive,desc` (наличие), `sort=brand,asc`.
                    """
    )
    @ApiResponse(responseCode = "200", description = "Страница товаров",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    @GetMapping
    public PageResponse<ProductResponseDto> search(
            @Parameter(description = "Текстовый запрос: бренд, описание, названия класса/серии/типа")
            @RequestParam(name = "q", required = false) String query,
            @Parameter(description = "Префикс имени класса (категории)")
            @RequestParam(required = false) String className,
            @Parameter(description = "Точная категория по UUID (предпочтительно для боковой колонки)")
            @RequestParam(required = false) UUID classId,
            @Parameter(description = "Фильтр по сериям: повторять параметр для нескольких UUID")
            @RequestParam(required = false) List<UUID> seriesIds,
            @Parameter(description = "Фильтр по типам товара (чекбоксы размеров/модификаций)")
            @RequestParam(required = false) List<UUID> typeIds,
            @RequestParam(required = false) String seriesName,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "minPrice must be greater or equal to 0")
            BigDecimal minPrice,
            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "maxPrice must be greater or equal to 0")
            BigDecimal maxPrice,
            @Parameter(description = "Мин. высота (мм), габарит для фильтра «Размеры»")
            @RequestParam(required = false) Integer minHeightMm,
            @Parameter(description = "Макс. высота (мм)")
            @RequestParam(required = false) Integer maxHeightMm,
            @RequestParam(required = false) Boolean isActive,
            @Parameter(hidden = true) Pageable pageable
    ) {
        ProductSearchRequestDto requestDto = new ProductSearchRequestDto(
                query,
                className,
                classId,
                emptyToNull(seriesIds),
                emptyToNull(typeIds),
                seriesName,
                typeName,
                brand,
                minPrice,
                maxPrice,
                minHeightMm,
                maxHeightMm,
                isActive
        );
        Page<ProductResponseDto> result = productService.search(requestDto, pageable);
        return PageResponse.fromPage(result);
    }

    private static List<UUID> emptyToNull(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            return null;
        }
        return ids;
    }

    @Operation(summary = "Привязать фото к товару", description = "Тело: fileId (UUID загруженного файла). Связь product_photo.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Фото привязано"),
            @ApiResponse(responseCode = "400", description = "Ошибка",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Товар или файл не найден",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @PostMapping("/{productId}/photos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attachPhoto(
            @PathVariable UUID productId,
            @Valid @RequestBody AttachProductPhotoRequestDto requestDto
    ) {
        productService.attachPhoto(productId, requestDto);
    }

    @Operation(summary = "Отвязать фото", description = "Query-параметр fileId — UUID файла.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Отвязано"),
            @ApiResponse(responseCode = "404", description = "Не найдено",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    })
    @DeleteMapping(value = "/{productId}/photos", params = "fileId")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachPhotoByFileId(
            @PathVariable UUID productId,
            @Parameter(description = "UUID файла") @RequestParam UUID fileId
    ) {
        productService.detachPhotoByFileId(productId, fileId);
    }
}
