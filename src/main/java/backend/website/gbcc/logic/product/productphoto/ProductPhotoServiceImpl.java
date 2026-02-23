package backend.website.gbcc.logic.product.productphoto;

import backend.website.gbcc.logic.file.FileRepository;
import backend.website.gbcc.logic.product.ProductEntity;
import backend.website.gbcc.logic.product.ProductRepository;
import backend.website.gbcc.logic.product.dto.AttachProductPhotoRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductPhotoServiceImpl implements ProductPhotoService {

    private final ProductRepository productRepository;
    private final ProductPhotoRepository productPhotoRepository;
    private final FileRepository fileRepository;

    @Override
    @Transactional
    public void attachPhoto(UUID productId, AttachProductPhotoRequestDto requestDto) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be empty");
        }
        if (requestDto == null || requestDto.fileId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File id must not be empty");
        }

        Integer sortOrder = requestDto.sortOrder() != null ? requestDto.sortOrder() : 0;
        if (sortOrder < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sort order must be greater or equal to 0");
        }

        ProductEntity product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));

        if (!fileRepository.existsById(requestDto.fileId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }

        if (productPhotoRepository.existsByProduct_IdAndSortOrder(productId, sortOrder)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Photo with this sort order already exists");
        }

        ProductPhotoEntity productPhoto = new ProductPhotoEntity();
        productPhoto.setProduct(product);
        productPhoto.setFileId(requestDto.fileId());
        productPhoto.setSortOrder(sortOrder);
        productPhotoRepository.save(productPhoto);
    }

    @Override
    @Transactional
    public void detachPhotoByFileId(UUID productId, UUID fileId) {
        if (productId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Product id must not be empty");
        }
        if (fileId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File id must not be empty");
        }

        long deletedRows = productPhotoRepository.deleteByProduct_IdAndFileId(productId, fileId);
        if (deletedRows == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product photo relation not found");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<UUID, List<UUID>> getPhotoIdsByProductIds(List<UUID> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return productPhotoRepository.findAllByProduct_IdIn(productIds).stream()
                .collect(Collectors.groupingBy(
                        it -> it.getProduct().getId(),
                        Collectors.mapping(ProductPhotoEntity::getFileId, Collectors.toList())
                ));
    }
}
