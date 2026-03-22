package backend.website.gbcc.logic.product;

import org.springframework.util.StringUtils;

/**
 * Общая нормализация текста для подстрочного поиска (как в {@link ProductSpecification}).
 */
public final class CatalogSearchText {

    private CatalogSearchText() {
    }

    /**
     * Паттерн {@code %...%} для LIKE по нижнему регистру или {@code null}, если искать нечего.
     */
    public static String likeContainsPattern(String query) {
        if (!StringUtils.hasText(query)) {
            return null;
        }
        String normalized = query.trim().toLowerCase();
        String safe = sanitizeForLikeSubstring(normalized);
        if (safe.isEmpty()) {
            return null;
        }
        return "%" + safe + "%";
    }

    private static String sanitizeForLikeSubstring(String normalizedLowercase) {
        return normalizedLowercase
                .replace("\\", "")
                .replace("%", "")
                .replace("_", "");
    }
}
