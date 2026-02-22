package backend.website.gbcc.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum FileType {

    // Images
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_WEBP("image/webp"),
    IMAGE_GIF("image/gif"),
    IMAGE_SVG("image/svg+xml"),

    // Video
    VIDEO_MP4("video/mp4"),
    VIDEO_WEBM("video/webm"),
    VIDEO_MOV("video/quicktime"),
    VIDEO_AVI("video/x-msvideo"),

    // 3D Models
    MODEL_GLB("model/gltf-binary"),
    MODEL_GLTF("model/gltf+json"),
    MODEL_OBJ("model/obj"),
    MODEL_STL("model/stl"),
    MODEL_FBX("application/octet-stream"),

    // Documents
    DOCUMENT_PDF("application/pdf"),

    // Preview
    THUMBNAIL("image/jpeg"),

    // fallback
    OTHER("application/octet-stream");


    private final String mimeType;


    FileType(String mimeType) {
        this.mimeType = mimeType;
    }

    public static FileType fromMimeType(String mimeType) {

        if (mimeType == null) {
            return OTHER;
        }

        return Arrays.stream(values())
                .filter(it -> it.mimeType.equalsIgnoreCase(mimeType))
                .findFirst()
                .orElse(OTHER);

    }

}