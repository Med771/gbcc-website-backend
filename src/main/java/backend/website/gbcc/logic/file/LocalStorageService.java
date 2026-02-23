package backend.website.gbcc.logic.file;

import backend.website.gbcc.config.property.FileProperty;
import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements FileService {

    private final FileProperty fileProperty;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    @Override
    @Transactional
    public FileUploadResponseDto upload(UploadFileRequestDto requestDto) {
        MultipartFile file = requestDto.getFile();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String key = UUID.randomUUID().toString();
        FileEntity entity = fileMapper.toEntity(requestDto, key, fileProperty.getBucket());
        FileEntity saved = fileRepository.saveAndFlush(entity);

        uploadToStorage(file, key);

        return fileMapper.toUploadResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownloadResponseDto download(FileKeyRequestDto requestDto) {
        String key = normalizeStorageKey(requestDto.getKey());
        FileEntity entity = findByKeyOrThrow(key);
        Resource resource = downloadFromStorage(entity.getKey());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
        }

        return fileMapper.toDownloadResponse(entity, resource);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FileUploadResponseDto> search(FileSearchRequestDto requestDto, Pageable pageable) {
        FileSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new FileSearchRequestDto(null, null, null);

        return fileRepository.findAll(FileSpecification.byFilter(safeRequest), pageable)
                .map(fileMapper::toUploadResponse);
    }

    @Override
    @Transactional
    public void delete(FileKeyRequestDto requestDto) {
        String key = normalizeStorageKey(requestDto.getKey());
        FileEntity entity = findByKeyOrThrow(key);
        fileRepository.delete(entity);
        fileRepository.flush();
        deleteFromStorage(entity.getKey());
    }

    private void uploadToStorage(MultipartFile file, String key) {
        Path bucketPath = resolveBucketPath();
        ensureBucketExists(bucketPath);
        Path path = resolvePathByKey(bucketPath, key);

        try {
            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot save file to storage", e);
        }
    }

    private Resource downloadFromStorage(String key) {
        Path bucketPath = resolveBucketPath();
        Path path = resolvePathByKey(bucketPath, key);
        return new FileSystemResource(path);
    }

    private void deleteFromStorage(String key) {
        Path bucketPath = resolveBucketPath();
        Path path = resolvePathByKey(bucketPath, key);
        try {
            if (!Files.deleteIfExists(path)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot delete file from storage", e);
        }
    }

    private FileEntity findByKeyOrThrow(String key) {
        return fileRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File metadata not found"));
    }

    private String normalizeStorageKey(String key) {
        if (!StringUtils.hasText(key)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File key must not be empty");
        }

        String normalized = key.trim();
        try {
            return UUID.fromString(normalized).toString();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file key format");
        }
    }

    private Path resolveBucketPath() {
        if (!StringUtils.hasText(fileProperty.getBucket())) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Storage bucket is not configured");
        }
        return Path.of(fileProperty.getBucket()).toAbsolutePath().normalize();
    }

    private void ensureBucketExists(Path bucketPath) {
        try {
            Files.createDirectories(bucketPath);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot initialize storage bucket", e);
        }
    }

    private Path resolvePathByKey(Path bucketPath, String key) {
        Path path = bucketPath.resolve(key).normalize();
        if (!path.startsWith(bucketPath)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file key");
        }
        return path;
    }

}