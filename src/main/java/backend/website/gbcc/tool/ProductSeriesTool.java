package backend.website.gbcc.tool;

import backend.website.gbcc.logic.product.productseries.ProductSeriesEntity;
import backend.website.gbcc.logic.product.productseries.ProductSeriesRepository;
import backend.website.gbcc.tool.parent.TaxonomyNameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductSeriesTool {

    private final ProductSeriesRepository productSeriesRepository;
    private final TaxonomyNameNormalizer nameNormalizer;

    public ProductSeriesEntity getOrCreate(String name) {
        String normalized = nameNormalizer.normalizeOptional(name);
        if (normalized == null) {
            return null;
        }

        return productSeriesRepository.findByNameIgnoreCase(normalized)
                .orElseGet(() -> saveSafely(normalized));
    }

    private ProductSeriesEntity saveSafely(String name) {
        try {
            ProductSeriesEntity entity = new ProductSeriesEntity();
            entity.setName(name);
            return productSeriesRepository.save(entity);
        } catch (DataIntegrityViolationException ex) {
            return productSeriesRepository.findByNameIgnoreCase(name)
                    .orElseThrow(() -> ex);
        }
    }
}
