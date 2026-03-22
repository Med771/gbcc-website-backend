package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.GroupedCatalogSearchResponseDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productclass.ProductClassRepository;
import backend.website.gbcc.logic.product.productphoto.ProductPhotoService;
import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
import backend.website.gbcc.tool.ProductClassTool;
import backend.website.gbcc.tool.ProductSeriesTool;
import backend.website.gbcc.tool.ProductTypeTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductClassRepository productClassRepository;

    @Mock
    private ProductPhotoService productPhotoService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private ProductClassTool productClassTool;

    @Mock
    private ProductSeriesTool productSeriesTool;

    @Mock
    private ProductTypeTool productTypeTool;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void searchGrouped_returnsEmpty_whenQueryBlankWithoutCallingRepositories() {
        GroupedCatalogSearchResponseDto result = productService.searchGrouped("   ", 5, 10, true);

        assertThat(result.totalCategoryCount()).isZero();
        assertThat(result.totalProductCount()).isZero();
        assertThat(result.totalCount()).isZero();
        assertThat(result.categories()).isEmpty();
        assertThat(result.products()).isEmpty();
        verifyNoInteractions(productClassRepository);
        verifyNoInteractions(productRepository);
    }

    @Test
    void search_shouldThrowBadRequest_whenMinPriceGreaterThanMaxPrice() {
        ProductSearchRequestDto request = new ProductSearchRequestDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("100.00"),
                new BigDecimal("10.00"),
                null,
                null,
                null
        );

        assertThatThrownBy(() -> productService.search(request, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("minPrice must be less than or equal to maxPrice");
                });
    }

    @Test
    void search_shouldThrowBadRequest_whenMinHeightGreaterThanMaxHeight() {
        ProductSearchRequestDto request = new ProductSearchRequestDto(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                3000,
                2000,
                null
        );

        assertThatThrownBy(() -> productService.search(request, PageRequest.of(0, 20)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("minHeightMm must be less than or equal to maxHeightMm");
                });
    }

    @Test
    void update_shouldThrowNotFound_whenProductMissing() {
        UUID productId = UUID.randomUUID();
        UpdateProductRequestDto request = new UpdateProductRequestDto(
                "Class A",
                null,
                null,
                "Brand A",
                null,
                null,
                null,
                null,
                null,
                null,
                100,
                100,
                100,
                new BigDecimal("1.000"),
                new BigDecimal("10.00"),
                new BigDecimal("0.00"),
                true
        );

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> productService.update(productId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
                    assertThat(ex.getReason()).isEqualTo("Product not found");
                });
    }

    @Test
    void getClasses_shouldReturnSortedResponse() {
        ProductClassResponseDto classA = new ProductClassResponseDto(UUID.randomUUID(), "A");
        ProductClassResponseDto classB = new ProductClassResponseDto(UUID.randomUUID(), "B");

        when(productClassTool.getClasses()).thenReturn(List.of(classA, classB));

        List<ProductClassResponseDto> result = productService.getClasses();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo(classA.id());
        assertThat(result.get(0).name()).isEqualTo("A");
        assertThat(result.get(1).id()).isEqualTo(classB.id());
        assertThat(result.get(1).name()).isEqualTo("B");
    }

    @Test
    void patch_shouldThrowNotFound_whenProductMissing() {
        UUID productId = UUID.randomUUID();
        PatchProductRequestDto request = new PatchProductRequestDto(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.empty());

        assertThatThrownBy(() -> productService.patch(productId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
                    assertThat(ex.getReason()).isEqualTo("Product not found");
                });
    }

    @Test
    void patch_shouldUpdateOnlyProvidedFields() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = new ProductEntity();
        product.setId(productId);
        product.setBrand("Old Brand");
        product.setDescription("Old Desc");
        product.setHeightMm(100);
        product.setWidthMm(100);
        product.setLengthMm(100);
        product.setWeightKg(new BigDecimal("1.000"));
        product.setPrice(new BigDecimal("10.00"));
        product.setIsActive(true);

        ProductClassEntity existingClass = new ProductClassEntity();
        existingClass.setId(UUID.randomUUID());
        existingClass.setName("Old Class");
        product.setProductClass(existingClass);

        ProductClassEntity newClass = new ProductClassEntity();
        newClass.setId(UUID.randomUUID());
        newClass.setName("New Class");

        ProductSeriesEntity newSeries = new ProductSeriesEntity();
        newSeries.setId(UUID.randomUUID());
        newSeries.setName("Series A");

        PatchProductRequestDto request = new PatchProductRequestDto(
                "New Class",
                "Series A",
                null,
                "New Brand",
                "",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                new BigDecimal("99.99"),
                new BigDecimal("15.00"),
                false
        );

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));
        when(productClassTool.getOrCreate("New Class")).thenReturn(newClass);
        when(productSeriesTool.getOrCreate("Series A")).thenReturn(newSeries);
        when(productRepository.save(product)).thenReturn(product);
        when(productPhotoService.getPhotoIdsByProductIds(List.of(productId))).thenReturn(java.util.Collections.emptyMap());
        when(productMapper.toResponse(product, java.util.Collections.emptyList()))
                .thenReturn(new backend.website.gbcc.logic.product.dto.ProductResponseDto(
                        productId, "New Class", "Series A", null, "New Brand", null,
                        null, null, 0, null, null, 0,
                        100, 100, 100, new BigDecimal("1.000"), new BigDecimal("99.99"),
                        new BigDecimal("15.00"), new BigDecimal("84.99"),
                        false, java.util.Collections.emptyList(), null, null
                ));

        productService.patch(productId, request);

        assertThat(product.getProductClass()).isEqualTo(newClass);
        assertThat(product.getProductSeries()).isEqualTo(newSeries);
        assertThat(product.getBrand()).isEqualTo("New Brand");
        assertThat(product.getDescription()).isNull();
        assertThat(product.getPrice()).isEqualByComparingTo("99.99");
        assertThat(product.getDiscountPercent()).isEqualByComparingTo("15.00");
        assertThat(product.getIsActive()).isFalse();
        assertThat(product.getHeightMm()).isEqualTo(100);
        verify(productRepository).save(product);
    }

    @Test
    void patch_shouldThrowBadRequest_whenBrandBlank() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = new ProductEntity();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        PatchProductRequestDto request = new PatchProductRequestDto(
                null, null, null, "   ", null, null, null, null, null, null,
                null, null, null, null, null, null, null
        );

        assertThatThrownBy(() -> productService.patch(productId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("brand must not be blank");
                });
    }

    @Test
    void patch_shouldThrowBadRequest_whenDiscountPercentGreaterThan100() {
        UUID productId = UUID.randomUUID();
        ProductEntity product = new ProductEntity();
        product.setId(productId);

        when(productRepository.findById(productId)).thenReturn(java.util.Optional.of(product));

        PatchProductRequestDto request = new PatchProductRequestDto(
                null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, new BigDecimal("100.01"), null
        );

        assertThatThrownBy(() -> productService.patch(productId, request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(BAD_REQUEST.value());
                    assertThat(ex.getReason()).isEqualTo("discountPercent must be less than or equal to 100");
                });

        verify(productRepository, never()).save(product);
    }
}
