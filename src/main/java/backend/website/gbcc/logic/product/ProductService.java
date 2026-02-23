package backend.website.gbcc.logic.product;

import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import backend.website.gbcc.logic.product.dto.CreateProductRequestDto;
import backend.website.gbcc.logic.product.dto.ProductResponseDto;
import backend.website.gbcc.logic.product.dto.ProductSearchRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    ProductResponseDto create(CreateProductRequestDto requestDto);

    Page<ProductResponseDto> search(ProductSearchRequestDto requestDto, Pageable pageable);

    void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto);
}
