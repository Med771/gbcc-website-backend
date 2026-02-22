package backend.website.gbcc.logic.file;

import backend.website.gbcc.config.property.FileProperty;
import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageService implements FileService {

    private final FileProperty fileProperty;
    private final FileRepository fileRepository;
    private final FileMapper fileMapper;

    @Override
    public FileUploadResponseDto upload(UploadFileRequestDto requestDto) {
        MultipartFile file = requestDto.getFile();
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must not be empty");
        }

        String key = uploadToStorage(file);
        FileEntity entity = fileMapper.toEntity(requestDto, key, fileProperty.getBucket());
        FileEntity saved = fileRepository.save(entity);
        return fileMapper.toUploadResponse(saved);
    }

    @Override
    public FileDownloadResponseDto download(FileKeyRequestDto requestDto) {
        FileEntity entity = findByKeyOrThrow(requestDto.getKey());
        Resource resource = downloadFromStorage(entity.getKey());

        if (!resource.exists() || !resource.isReadable()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found in storage");
        }

        return fileMapper.toDownloadResponse(entity, resource);
    }

    @Override
    public void delete(FileKeyRequestDto requestDto) {
        FileEntity entity = findByKeyOrThrow(requestDto.getKey());
        deleteFromStorage(entity.getKey());
        fileRepository.delete(entity);
    }

    private String uploadToStorage(MultipartFile file) {
        String key = UUID.randomUUID().toString();

        try {
            Path path = Paths.get(fileProperty.getBucket(), key);

            Files.copy(
                    file.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return key;
    }

    private Resource downloadFromStorage(String key) {
        Path path = Paths.get(fileProperty.getBucket(), key);
        return new FileSystemResource(path);
    }

    private void deleteFromStorage(String key) {
        try {
            Files.deleteIfExists(Paths.get(fileProperty.getBucket(), key));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FileEntity findByKeyOrThrow(String key) {
        return fileRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File metadata not found"));
    }

}