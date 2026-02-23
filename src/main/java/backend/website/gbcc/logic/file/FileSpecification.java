package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public final class FileSpecification {

    private FileSpecification() {
    }

    public static Specification<FileEntity> byFilter(FileSearchRequestDto filter) {
        return Specification.allOf(
                keyLike(filter.key()),
                fileNameLike(filter.fileName()),
                bucketLike(filter.bucket())
        );
    }

    private static Specification<FileEntity> keyLike(String key) {
        return (root, query, cb) -> {
            String normalized = normalize(key);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("key")), normalized + "%");
        };
    }

    private static Specification<FileEntity> fileNameLike(String fileName) {
        return (root, query, cb) -> {
            String normalized = normalize(fileName);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("fileName")), normalized + "%");
        };
    }

    private static Specification<FileEntity> bucketLike(String bucket) {
        return (root, query, cb) -> {
            String normalized = normalize(bucket);
            if (normalized == null) {
                return null;
            }
            return cb.like(cb.lower(root.get("bucket")), normalized + "%");
        };
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim().toLowerCase();
    }
}
