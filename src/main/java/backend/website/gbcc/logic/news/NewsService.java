package backend.website.gbcc.logic.news;

import backend.website.gbcc.logic.news.dto.CreateNewsRequestDto;
import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import backend.website.gbcc.logic.news.dto.NewsSearchRequestDto;
import backend.website.gbcc.logic.news.dto.PatchNewsRequestDto;
import backend.website.gbcc.logic.news.dto.UpdateNewsRequestDto;
import backend.website.gbcc.model.NewsCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface NewsService {
    NewsResponseDto create(CreateNewsRequestDto requestDto);

    NewsResponseDto update(UUID newsId, UpdateNewsRequestDto requestDto);

    NewsResponseDto patch(UUID newsId, PatchNewsRequestDto requestDto);

    NewsResponseDto getById(UUID newsId);

    Page<NewsResponseDto> search(NewsSearchRequestDto requestDto, Pageable pageable);

    List<NewsCategory> listCategories();
}
