package backend.website.gbcc.tool;

import backend.website.gbcc.logic.product.dto.ProductClassResponseDto;
import backend.website.gbcc.logic.product.productclass.ProductClassEntity;
import backend.website.gbcc.logic.product.productclass.ProductClassRepository;
import backend.website.gbcc.tool.parent.TaxonomyNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductClassTool {

    private final ProductClassRepository productClassRepository;
    private final TaxonomyNameNormalizer nameNormalizer;

    public ProductClassEntity getOrCreate(String name) {
        String normalized = nameNormalizer.normalizeRequired("className", name);

        return productClassRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> saveSafely(normalized));
    }

    @Transactional(readOnly = true)
    public List<ProductClassResponseDto> getClasses() {
        return productClassRepository.findAllByOrderByNameAsc().stream()
                .map(entity -> new ProductClassResponseDto(entity.getId(), entity.getName()))
                .toList();
    }

    private ProductClassEntity saveSafely(String name) {
        try {
            ProductClassEntity entity = new ProductClassEntity();
            entity.setName(name);
            return productClassRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            return productClassRepository.findByNameIgnoreCase(name)
                    .orElseThrow(() -> ex);
        }
    }
}
