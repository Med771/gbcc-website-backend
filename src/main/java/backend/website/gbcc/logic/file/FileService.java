package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileSearchRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FileService {
    FileUploadResponseDto upload(UploadFileRequestDto requestDto);

    FileDownloadResponseDto download(FileKeyRequestDto requestDto);

    Page<FileUploadResponseDto> search(FileSearchRequestDto requestDto, Pageable pageable);

    void delete(FileKeyRequestDto requestDto);
}
