package backend.website.gbcc.tool;

import backend.website.gbcc.logic.product.producttype.ProductTypeEntity;
import backend.website.gbcc.logic.product.producttype.ProductTypeRepository;
import backend.website.gbcc.tool.parent.TaxonomyNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductTypeTool {

    private final ProductTypeRepository productTypeRepository;
    private final TaxonomyNameNormalizer nameNormalizer;

    public ProductTypeEntity getOrCreate(String name) {
        String normalized = nameNormalizer.normalizeOptional(name);
        if (normalized == null) {
            return null;
        }

        return productTypeRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> saveSafely(normalized));
    }

    private ProductTypeEntity saveSafely(String name) {
        try {
            ProductTypeEntity entity = new ProductTypeEntity();
            entity.setName(name);
            return productTypeRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            return productTypeRepository.findByNameIgnoreCase(name)
                    .orElseThrow(() -> ex);
        }
    }
}
