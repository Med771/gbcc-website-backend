package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
import backend.website.gbcc.logic.product.productphoto.ProductPhotoService;

import backend.website.gbcc.tool.ProductClassTool;
import backend.website.gbcc.tool.ProductSeriesTool;
import backend.website.gbcc.tool.ProductTypeTool;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductPhotoService productPhotoService;

    private final ProductMapper productMapper;

    private final ProductClassTool productClassTool;
    private final ProductSeriesTool productSeriesTool;
    private final ProductTypeTool productTypeTool;

    @Override
    @Transactional
    public ProductResponseDto create(CreateProductRequestDto requestDto) {
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
        product.setIsActive(requestDto.isActive() != null ? requestDto.isActive() : Boolean.TRUE);

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
        if (requestDto.isActive() != null) {
            product.setIsActive(requestDto.isActive());
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
                : new ProductSearchRequestDto(null, null, null, null, null, null, null);
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
    }
}
