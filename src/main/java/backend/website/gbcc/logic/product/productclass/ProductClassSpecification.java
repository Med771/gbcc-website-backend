package backend.website.gbcc.logic.product.productclass;

import backend.website.gbcc.logic.product.CatalogSearchText;
import org.springframework.data.jpa.domain.Specification;

public final class ProductClassSpecification {

    private ProductClassSpecification() {
    }

    public static Specification<ProductClassEntity> nameContains(String query) {
        return (root, q, cb) -> {
            String pattern = CatalogSearchText.likeContainsPattern(query);
            if (pattern == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }
}
