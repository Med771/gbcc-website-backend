package backend.website.gbcc.logic.file;

import backend.website.gbcc.model.FileType;
import backend.website.gbcc.model.convector.FileTypeConverter;

import jakarta.persistence.*;

import backend.website.gbcc.model.BaseEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "files")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class FileEntity extends BaseEntity {

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private String bucket;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "mime_type", nullable = false)
    @Convert(converter = FileTypeConverter.class)
    private FileType mimeType;

    @Column(nullable = false)
    private Long size = 0L;
}