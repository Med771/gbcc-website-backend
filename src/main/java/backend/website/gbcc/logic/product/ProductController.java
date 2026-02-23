package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
import backend.website.gbcc.model.dto.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDto create(@Valid @RequestBody CreateProductRequestDto requestDto) {
        return productService.create(requestDto);
    }

    @PutMapping("/{productId}")
    public ProductResponseDto update(@PathVariable UUID productId, @Valid @RequestBody UpdateProductRequestDto requestDto) {
        return productService.update(productId, requestDto);
    }

    @PatchMapping("/{productId}")
    public ProductResponseDto patch(@PathVariable UUID productId, @Valid @RequestBody PatchProductRequestDto requestDto) {
        return productService.patch(productId, requestDto);
    }

    @GetMapping("/classes")
    public List<ProductClassResponseDto> getClasses() {
        return productService.getClasses();
    }

    @GetMapping
    public PageResponse<ProductResponseDto> search(
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String seriesName,
            @RequestParam(required = false) String typeName,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "minPrice must be greater or equal to 0")
            BigDecimal minPrice,
            @RequestParam(required = false)
            @DecimalMin(value = "0.0", message = "maxPrice must be greater or equal to 0")
            BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isActive,
            Pageable pageable
    ) {
        ProductSearchRequestDto requestDto = new ProductSearchRequestDto(
                className,
                seriesName,
                typeName,
                brand,
                minPrice,
                maxPrice,
                isActive
        );
        Page<ProductResponseDto> result = productService.search(requestDto, pageable);
        return new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @PostMapping("/{productId}/photos")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void attachPhoto(@PathVariable UUID productId, @Valid @RequestBody AttachProductPhotoRequestDto requestDto) {
        productService.attachPhoto(productId, requestDto);
    }

    @DeleteMapping(value = "/{productId}/photos", params = "fileId")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void detachPhotoByFileId(@PathVariable UUID productId, @RequestParam UUID fileId) {
        productService.detachPhotoByFileId(productId, fileId);
    }
}
