package backend.website.gbcc.logic.file;

import backend.website.gbcc.logic.file.dto.FileDownloadResponseDto;
import backend.website.gbcc.logic.file.dto.FileKeyRequestDto;
import backend.website.gbcc.logic.file.dto.FileUploadResponseDto;
import backend.website.gbcc.logic.file.dto.UploadFileRequestDto;
public interface FileService {
    FileUploadResponseDto upload(UploadFileRequestDto requestDto);

    FileDownloadResponseDto download(FileKeyRequestDto requestDto);

    void delete(FileKeyRequestDto requestDto);
}
