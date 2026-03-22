package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.PatchProductRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.GroupedCatalogSearchResponseDto;
import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import backend.website.gbcc.logic.product.dto.UpdateProductRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDto getById(UUID productId);

    List<ProductResponseDto> findSimilar(UUID productId, int limit, Boolean onlyActive);

    ProductResponseDto create(CreateProductRequestDto requestDto);

    ProductResponseDto update(UUID productId, UpdateProductRequestDto requestDto);

    ProductResponseDto patch(UUID productId, PatchProductRequestDto requestDto);

    Page<ProductResponseDto> search(ProductSearchRequestDto requestDto, Pageable pageable);

    GroupedCatalogSearchResponseDto searchGrouped(String query, int categoryLimit, int productLimit, Boolean isActive);

    List<ProductClassResponseDto> getClasses();

    void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto);

    void detachPhotoByFileId(UUID productId, UUID fileId);
}
