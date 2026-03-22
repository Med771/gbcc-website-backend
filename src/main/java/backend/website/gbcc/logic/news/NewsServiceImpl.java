package backend.website.gbcc.logic.news;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.news.dto.CreateNewsRequestDto;
import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import backend.website.gbcc.logic.news.dto.NewsSearchRequestDto;
import backend.website.gbcc.logic.news.dto.PatchNewsRequestDto;
import backend.website.gbcc.logic.news.dto.UpdateNewsRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.NewsCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NewsServiceImpl implements NewsService {

    private static final String FORBIDDEN_NOT_MANAGER = "Only admin or owner can manage news";

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;
    private final SecurityContextHelper securityContextHelper;

    @Override
    @Transactional
    public NewsResponseDto create(CreateNewsRequestDto requestDto) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        NewsEntity news = new NewsEntity();
        news.setTitle(normalizeRequired(requestDto.title(), "title"));
        news.setPreviewText(normalizeRequired(requestDto.previewText(), "previewText"));
        news.setContent(normalizeRequired(requestDto.content(), "content"));
        news.setCategory(requestDto.category());
        news.setCoverFileId(requestDto.coverFileId());

        boolean isPublished = Boolean.TRUE.equals(requestDto.isPublished());
        news.setIsPublished(isPublished);
        news.setPublishedAt(resolvePublishedAt(isPublished, requestDto.publishedAt(), null));
        news.setCreatedByAccountId(principal.accountId());
        news.setUpdatedByAccountId(principal.accountId());

        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Override
    @Transactional
    public NewsResponseDto update(UUID newsId, UpdateNewsRequestDto requestDto) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        NewsEntity news = findOrThrow(newsId);

        news.setTitle(normalizeRequired(requestDto.title(), "title"));
        news.setPreviewText(normalizeRequired(requestDto.previewText(), "previewText"));
        news.setContent(normalizeRequired(requestDto.content(), "content"));
        news.setCategory(requestDto.category());
        news.setCoverFileId(requestDto.coverFileId());
        news.setIsPublished(requestDto.isPublished());
        news.setPublishedAt(resolvePublishedAt(requestDto.isPublished(), requestDto.publishedAt(), news.getPublishedAt()));
        news.setUpdatedByAccountId(principal.accountId());

        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Override
    @Transactional
    public NewsResponseDto patch(UUID newsId, PatchNewsRequestDto requestDto) {
        AccountPrincipal principal = securityContextHelper.requireAdminOrOwner(FORBIDDEN_NOT_MANAGER);
        NewsEntity news = findOrThrow(newsId);

        if (requestDto.title() != null) {
            news.setTitle(normalizeRequired(requestDto.title(), "title"));
        }
        if (requestDto.previewText() != null) {
            news.setPreviewText(normalizeRequired(requestDto.previewText(), "previewText"));
        }
        if (requestDto.content() != null) {
            news.setContent(normalizeRequired(requestDto.content(), "content"));
        }
        if (requestDto.category() != null) {
            news.setCategory(requestDto.category());
        }
        if (requestDto.coverFileId() != null) {
            news.setCoverFileId(requestDto.coverFileId());
        }
        if (requestDto.isPublished() != null) {
            boolean isPublished = requestDto.isPublished();
            news.setIsPublished(isPublished);
            news.setPublishedAt(resolvePublishedAt(isPublished, requestDto.publishedAt(), news.getPublishedAt()));
        } else if (requestDto.publishedAt() != null && Boolean.TRUE.equals(news.getIsPublished())) {
            news.setPublishedAt(requestDto.publishedAt());
        }
        news.setUpdatedByAccountId(principal.accountId());

        return newsMapper.toResponse(newsRepository.save(news));
    }

    @Override
    @Transactional(readOnly = true)
    public NewsResponseDto getById(UUID newsId) {
        NewsEntity news = findOrThrow(newsId);
        if (!securityContextHelper.isCurrentPrincipalAdminOrOwner() && !isVisibleForPublic(news)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found");
        }
        return newsMapper.toResponse(news);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NewsResponseDto> search(NewsSearchRequestDto requestDto, Pageable pageable) {
        NewsSearchRequestDto safeRequest = requestDto != null
                ? requestDto
                : new NewsSearchRequestDto(null, null, null);

        Specification<NewsEntity> spec = NewsSpecification.byFilter(safeRequest);
        if (!securityContextHelper.isCurrentPrincipalAdminOrOwner()) {
            spec = spec.and(NewsSpecification.visibleForPublic());
        }

        return newsRepository.findAll(spec, pageable).map(newsMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NewsCategory> listCategories() {
        return Arrays.asList(NewsCategory.values());
    }

    private NewsEntity findOrThrow(UUID newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "News not found"));
    }

    private boolean isVisibleForPublic(NewsEntity news) {
        return Boolean.TRUE.equals(news.getIsPublished())
                && news.getPublishedAt() != null
                && !news.getPublishedAt().isAfter(Instant.now());
    }

    private String normalizeRequired(String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " is required");
        }
        return value.trim();
    }

    private Instant resolvePublishedAt(Boolean isPublished, Instant requestedPublishedAt, Instant currentPublishedAt) {
        if (!Boolean.TRUE.equals(isPublished)) {
            return null;
        }
        if (requestedPublishedAt != null) {
            return requestedPublishedAt;
        }
        if (currentPublishedAt != null) {
            return currentPublishedAt;
        }
        return Instant.now();
    }
}
