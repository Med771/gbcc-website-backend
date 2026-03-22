package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.GroupedCatalogSearchResponseDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productclass.ProductClassRepository;
import backend.website.gbcc.logic.product.productclass.ProductClassSpecification;
import backend.website.gbcc.logic.product.productphoto.ProductPhotoService;

import backend.website.gbcc.tool.ProductClassTool;
import backend.website.gbcc.tool.ProductSeriesTool;
import backend.website.gbcc.tool.ProductTypeTool;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductClassRepository productClassRepository;

    private final ProductPhotoService productPhotoService;

    private final ProductMapper productMapper;

    private final ProductClassTool productClassTool;
    private final ProductSeriesTool productSeriesTool;
    private final ProductTypeTool productTypeTool;

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDto getById(UUID productId) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(List.of(productId));
        return productMapper.toResponse(product, photoIdsByProductId.getOrDefault(productId, Collections.emptyList()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponseDto> findSimilar(UUID productId, int limit, Boolean onlyActive) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        UUID classId = product.getProductClass().getId();
        Specification<ProductEntity> spec = ProductSpecification.sameCategoryExcluding(classId, productId);
        if (onlyActive == null || Boolean.TRUE.equals(onlyActive)) {
            spec = spec.and(ProductSpecification.activeProductsOnly());
        }
        Page<ProductEntity> page = productRepository.findAll(
                spec,
                PageRequest.of(0, limit, Sort.by(Sort.Order.desc("popularityScore"), Sort.Order.asc("id")))
        );
        List<UUID> ids = page.getContent().stream().map(ProductEntity::getId).toList();
        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(ids);
        return page.getContent().stream()
                .map(p -> productMapper.toResponse(p, photoIdsByProductId.getOrDefault(p.getId(), Collections.emptyList())))
                .toList();
    }

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductRequestDto requestDto) {
        validateDiscountPercent(requestDto.discountPercent());
        var productClass = productClassTool.getOrCreate(requestDto.className());
        var productSeries = productSeriesTool.getOrCreate(requestDto.seriesName());
        var productType = productTypeTool.getOrCreate(requestDto.typeName());

        ProductEntity entity = productMapper.toEntity(requestDto, productClass, productSeries, productType);
        ProductEntity saved = productRepository.save(entity);
        return productMapper.toResponse(saved, Collections.emptyList());
    }

    @Override
    @Transactional
    public ProductResponseDto update(UUID productId, UpdateProductRequestDto requestDto) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        validateDiscountPercent(requestDto.discountPercent());

        var productClass = productClassTool.getOrCreate(requestDto.className());
        var productSeries = productSeriesTool.getOrCreate(requestDto.seriesName());
        var productType = productTypeTool.getOrCreate(requestDto.typeName());

        product.setProductClass(productClass);
        product.setProductSeries(productSeries);
        product.setProductType(productType);
        product.setBrand(requestDto.brand());
        product.setDescription(requestDto.description());
        product.setHeightMm(requestDto.heightMm());
        product.setWidthMm(requestDto.widthMm());
        product.setLengthMm(requestDto.lengthMm());
        product.setWeightKg(requestDto.weightKg());
        product.setPrice(requestDto.price());
        product.setDiscountPercent(requestDto.discountPercent() != null ? requestDto.discountPercent() : BigDecimal.ZERO);
        product.setIsActive(requestDto.isActive() != null ? requestDto.isActive() : Boolean.TRUE);
        product.setTagline(normalizeOptional(requestDto.tagline()));
        product.setTags(normalizeOptional(requestDto.tags()));
        product.setDeliveryText(normalizeOptional(requestDto.deliveryText()));
        product.setLicensesText(normalizeOptional(requestDto.licensesText()));
        if (requestDto.interestCount() != null) {
            validateInterestCount(requestDto.interestCount());
            product.setInterestCount(requestDto.interestCount());
        }

        ProductEntity saved = productRepository.save(product);

        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(List.of(saved.getId()));
        return productMapper.toResponse(saved, photoIdsByProductId.getOrDefault(saved.getId(), Collections.emptyList()));
    }

    @Override
    @Transactional
    public ProductResponseDto patch(UUID productId, PatchProductRequestDto requestDto) {
        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (requestDto.className() != null) {
            if (!StringUtils.hasText(requestDto.className())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "className must not be blank");
            }
            product.setProductClass(productClassTool.getOrCreate(requestDto.className()));
        }

        if (requestDto.seriesName() != null) {
            product.setProductSeries(StringUtils.hasText(requestDto.seriesName())
                    ? productSeriesTool.getOrCreate(requestDto.seriesName())
                    : null);
        }

        if (requestDto.typeName() != null) {
            product.setProductType(StringUtils.hasText(requestDto.typeName())
                    ? productTypeTool.getOrCreate(requestDto.typeName())
                    : null);
        }

        if (requestDto.brand() != null) {
            if (!StringUtils.hasText(requestDto.brand())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "brand must not be blank");
            }
            product.setBrand(requestDto.brand().trim());
        }

        if (requestDto.description() != null) {
            product.setDescription(StringUtils.hasText(requestDto.description()) ? requestDto.description().trim() : null);
        }
        if (requestDto.heightMm() != null) {
            product.setHeightMm(requestDto.heightMm());
        }
        if (requestDto.widthMm() != null) {
            product.setWidthMm(requestDto.widthMm());
        }
        if (requestDto.lengthMm() != null) {
            product.setLengthMm(requestDto.lengthMm());
        }
        if (requestDto.weightKg() != null) {
            product.setWeightKg(requestDto.weightKg());
        }
        if (requestDto.price() != null) {
            product.setPrice(requestDto.price());
        }
        if (requestDto.discountPercent() != null) {
            validateDiscountPercent(requestDto.discountPercent());
            product.setDiscountPercent(requestDto.discountPercent());
        }
        if (requestDto.isActive() != null) {
            product.setIsActive(requestDto.isActive());
        }
        if (requestDto.tagline() != null) {
            product.setTagline(normalizeOptional(requestDto.tagline()));
        }
        if (requestDto.tags() != null) {
            product.setTags(normalizeOptional(requestDto.tags()));
        }
        if (requestDto.deliveryText() != null) {
            product.setDeliveryText(normalizeOptional(requestDto.deliveryText()));
        }
        if (requestDto.licensesText() != null) {
            product.setLicensesText(normalizeOptional(requestDto.licensesText()));
        }
        if (requestDto.interestCount() != null) {
            validateInterestCount(requestDto.interestCount());
            product.setInterestCount(requestDto.interestCount());
        }

        ProductEntity saved = productRepository.save(product);
        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(List.of(saved.getId()));
        return productMapper.toResponse(saved, photoIdsByProductId.getOrDefault(saved.getId(), Collections.emptyList()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponseDto> search(ProductSearchRequestDto requestDto, Pageable pageable) {
        ProductSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new ProductSearchRequestDto(
                        null, null, null, null, null, null, null, null, null, null, null, null, null);
        validateSearchRequest(safeRequest);

        Page<ProductEntity> products = productRepository.findAll(ProductSpecification.byFilter(safeRequest), pageable);
        List<UUID> productIds = products.getContent().stream()
                .map(ProductEntity::getId)
                .toList();

        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(productIds);

        return products.map(product ->
                productMapper.toResponse(product, photoIdsByProductId.getOrDefault(product.getId(), Collections.emptyList()))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public GroupedCatalogSearchResponseDto searchGrouped(String query, int categoryLimit, int productLimit, Boolean isActive) {
        if (!StringUtils.hasText(query) || CatalogSearchText.likeContainsPattern(query) == null) {
            return new GroupedCatalogSearchResponseDto(0, 0, 0, Collections.emptyList(), Collections.emptyList());
        }
        String q = query.trim();
        boolean activeOnly = isActive != null ? isActive : Boolean.TRUE;
        ProductSearchRequestDto productFilter = new ProductSearchRequestDto(
                q, null, null, null, null, null, null, null, null, null, null, null, activeOnly
        );
        validateSearchRequest(productFilter);

        Specification<ProductClassEntity> classSpec = ProductClassSpecification.nameContains(q);
        long totalCategories = productClassRepository.count(classSpec);
        Page<ProductClassEntity> classPage = productClassRepository.findAll(
                classSpec,
                PageRequest.of(0, categoryLimit, Sort.by("name"))
        );
        List<ProductClassResponseDto> categories = classPage.getContent().stream()
                .map(c -> new ProductClassResponseDto(c.getId(), c.getName()))
                .toList();

        Specification<ProductEntity> productSpec = ProductSpecification.byFilter(productFilter);
        long totalProducts = productRepository.count(productSpec);
        Page<ProductEntity> productPage = productRepository.findAll(
                productSpec,
                PageRequest.of(0, productLimit, Sort.by("brand", "id"))
        );
        List<UUID> productIds = productPage.getContent().stream()
                .map(ProductEntity::getId)
                .toList();
        Map<UUID, List<UUID>> photoIdsByProductId = productPhotoService.getPhotoIdsByProductIds(productIds);
        List<ProductResponseDto> products = productPage.getContent().stream()
                .map(p -> productMapper.toResponse(p, photoIdsByProductId.getOrDefault(p.getId(), Collections.emptyList())))
                .toList();

        return new GroupedCatalogSearchResponseDto(
                totalCategories,
                totalProducts,
                totalCategories + totalProducts,
                categories,
                products
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductClassResponseDto> getClasses() {
        return productClassTool.getClasses();
    }

    @Override
    public void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto) {
        productPhotoService.attachPhoto(productId, requestDto);
    }

    @Override
    public void detachPhotoByFileId(UUID productId, UUID fileId) {
        productPhotoService.detachPhotoByFileId(productId, fileId);
    }

    private void validateSearchRequest(ProductSearchRequestDto requestDto) {
        if (requestDto.minPrice() != null && requestDto.maxPrice() != null
                && requestDto.minPrice().compareTo(requestDto.maxPrice()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be less than or equal to maxPrice");
        }
        if (requestDto.minHeightMm() != null && requestDto.maxHeightMm() != null
                && requestDto.minHeightMm() > requestDto.maxHeightMm()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minHeightMm must be less than or equal to maxHeightMm");
        }
    }

    private void validateDiscountPercent(BigDecimal discountPercent) {
        if (discountPercent != null && discountPercent.compareTo(new BigDecimal("100")) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "discountPercent must be less than or equal to 100");
        }
    }

    private void validateInterestCount(int interestCount) {
        if (interestCount < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "interestCount must be greater than or equal to 0");
        }
    }

    private String normalizeOptional(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
