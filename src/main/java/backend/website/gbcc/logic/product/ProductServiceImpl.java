package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
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
    public void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto) {
        productPhotoService.attachPhoto(productId, requestDto);
    }

    private void validateSearchRequest(ProductSearchRequestDto requestDto) {
        if (requestDto.minPrice() != null && requestDto.maxPrice() != null
                && requestDto.minPrice().compareTo(requestDto.maxPrice()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "minPrice must be less than or equal to maxPrice");
        }
    }
}
