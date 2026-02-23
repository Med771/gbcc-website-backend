package backend.website.gbcc.tool.parent;

public interface TaxonomyNameNormalizer {
    String normalizeRequired(String fieldName, String value);

    String normalizeOptional(String value);
}
