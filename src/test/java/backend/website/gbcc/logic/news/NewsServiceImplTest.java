package backend.website.gbcc.logic.news;

import backend.website.gbcc.helper.SecurityContextHelper;
import backend.website.gbcc.logic.news.dto.CreateNewsRequestDto;
import backend.website.gbcc.logic.news.dto.NewsResponseDto;
import backend.website.gbcc.logic.news.dto.NewsSearchRequestDto;
import backend.website.gbcc.logic.news.dto.UpdateNewsRequestDto;
import backend.website.gbcc.model.AccountPrincipal;
import backend.website.gbcc.model.AccountRole;
import backend.website.gbcc.model.NewsCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ExtendWith(MockitoExtension.class)
class NewsServiceImplTest {

    @Mock
    private NewsRepository newsRepository;
    @Mock
    private NewsMapper newsMapper;

    @Spy
    private SecurityContextHelper securityContextHelper = new SecurityContextHelper();

    @InjectMocks
    private NewsServiceImpl newsService;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void create_shouldThrowForbidden_forCustomer() {
        UUID accountId = UUID.randomUUID();
        setPrincipal(accountId, AccountRole.CUSTOMER);

        CreateNewsRequestDto request = new CreateNewsRequestDto(
                "Title",
                "Preview",
                "Content",
                NewsCategory.PRODUCTS,
                null,
                true,
                Instant.now()
        );

        assertThatThrownBy(() -> newsService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(FORBIDDEN.value());
                });
    }

    @Test
    void create_shouldSetPublishedAt_whenPublishedAndMissingDate() {
        UUID accountId = UUID.randomUUID();
        UUID newsId = UUID.randomUUID();
        setPrincipal(accountId, AccountRole.ADMIN);

        NewsEntity saved = new NewsEntity();
        saved.setId(newsId);
        saved.setTitle("Title");
        saved.setPreviewText("Preview");
        saved.setContent("Content");
        saved.setIsPublished(true);
        saved.setPublishedAt(Instant.now());
        saved.setCreatedByAccountId(accountId);
        saved.setUpdatedByAccountId(accountId);

        when(newsRepository.save(any(NewsEntity.class))).thenReturn(saved);
        when(newsMapper.toResponse(saved)).thenReturn(new NewsResponseDto(
                newsId, "Title", "Preview", "Content", null, NewsCategory.PRODUCTS, true, saved.getPublishedAt(),
                accountId, accountId, null, null
        ));

        CreateNewsRequestDto request = new CreateNewsRequestDto(
                "Title", "Preview", "Content", NewsCategory.PRODUCTS, null, true, null
        );

        NewsResponseDto response = newsService.create(request);
        assertThat(response.isPublished()).isTrue();
        assertThat(response.publishedAt()).isNotNull();
    }

    @Test
    void getById_shouldHideUnpublished_forPublic() {
        SecurityContextHolder.clearContext();
        UUID newsId = UUID.randomUUID();

        NewsEntity entity = new NewsEntity();
        entity.setId(newsId);
        entity.setIsPublished(false);

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> newsService.getById(newsId))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    ResponseStatusException ex = (ResponseStatusException) error;
                    assertThat(ex.getStatusCode().value()).isEqualTo(NOT_FOUND.value());
                });
    }

    @Test
    void search_shouldReturnPage_forManager() {
        UUID accountId = UUID.randomUUID();
        setPrincipal(accountId, AccountRole.OWNER);

        when(newsRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0));

        assertThat(newsService.search(new NewsSearchRequestDto("query", null, null), PageRequest.of(0, 20)).getContent())
                .isEqualTo(List.of());
    }

    @Test
    void update_shouldUnpublishAndClearPublishedAt() {
        UUID accountId = UUID.randomUUID();
        UUID newsId = UUID.randomUUID();
        setPrincipal(accountId, AccountRole.ADMIN);

        NewsEntity entity = new NewsEntity();
        entity.setId(newsId);
        entity.setIsPublished(true);
        entity.setPublishedAt(Instant.now());
        entity.setTitle("Old");
        entity.setPreviewText("Old");
        entity.setContent("Old");

        when(newsRepository.findById(newsId)).thenReturn(Optional.of(entity));
        when(newsRepository.save(entity)).thenReturn(entity);
        when(newsMapper.toResponse(entity)).thenReturn(new NewsResponseDto(
                newsId, "New", "Preview", "Content", null, NewsCategory.PRODUCTS, false, null,
                accountId, accountId, null, null
        ));

        UpdateNewsRequestDto request = new UpdateNewsRequestDto(
                "New", "Preview", "Content", NewsCategory.PRODUCTS, null, false, null
        );

        NewsResponseDto response = newsService.update(newsId, request);
        assertThat(response.isPublished()).isFalse();
        assertThat(response.publishedAt()).isNull();
    }

    private void setPrincipal(UUID accountId, AccountRole role) {
        AccountPrincipal principal = new AccountPrincipal(accountId, role);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList())
        );
    }
}
