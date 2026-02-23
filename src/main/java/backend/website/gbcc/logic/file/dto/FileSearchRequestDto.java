package backend.website.gbcc.logic.file.dto;

public record FileSearchRequestDto(
        String key,
        String fileName,
        String bucket
) {
}
