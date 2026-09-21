package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.dto.response.UserSummaryResponse;
import com.elmangusto.communityhub.entity.News;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.entity.enums.UserStatus;
import com.elmangusto.communityhub.exception.ResourceNotFoundException;
import com.elmangusto.communityhub.mapper.NewsMapper;
import com.elmangusto.communityhub.repository.NewsRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long NEWS_ID = 1L;

    @Mock
    private NewsRepository newsRepository;

    @Mock
    private NewsMapper newsMapper;

    @InjectMocks
    private NewsService newsService;

    @Test
    void createNews_shouldMapSetUserSaveAndReturnResponse() {

        CustomUserDetails principal = getPrincipal();
        NewsCreateRequest request = getRequest();
        News mappedNews = News.builder().build();
        News savedNews = News.builder().id(NEWS_ID).user(principal.getUser()).build();
        NewsResponse expectedResponse = getResponse();

        when(newsMapper.toEntity(request)).thenReturn(mappedNews);
        when(newsRepository.save(mappedNews)).thenReturn(savedNews);
        when(newsMapper.toResponse(savedNews)).thenReturn(expectedResponse);

        NewsResponse result = newsService.create(request, principal);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(mappedNews.getUser()).isEqualTo(principal.getUser());
        verify(newsRepository).save(mappedNews);
    }

    @Test
    void getById_shouldReturnNews_whenNewsExist() {

        User user = getUser();
        News news = getNews(user);

        when(newsRepository.findById(NEWS_ID))
                .thenReturn(Optional.of(news));

        NewsResponse response = getResponse();

        when(newsMapper.toResponse(news))
                .thenReturn(response);

        NewsResponse result = newsService.getById(NEWS_ID);

        assertThat(result).isEqualTo(response);
    }

    @Test
    void getAll_shouldReturnPageOfUser_whenUsersExist() {

        User user = getUser();
        News news = getNews(user);
        NewsResponse newsResponse = getResponse();

        Pageable pageable = PageRequest.of(0, 10);
        Page<News> newsPage = new PageImpl<>(List.of(news));

        when(newsRepository.findAll(pageable))
                .thenReturn(newsPage);

        when(newsMapper.toResponse(news))
                .thenReturn(newsResponse);

        Page<NewsResponse> result = newsService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst()).isEqualTo(newsResponse);
        assertThat(result.getTotalElements()).isEqualTo(1);

        verify(newsRepository).findAll(pageable);
        verify(newsMapper).toResponse(news);
        verifyNoMoreInteractions(newsRepository, newsMapper);
    }

    @Test
    void getAll_shouldReturnEmptyPage_whenNoUsersExist() {

        Pageable pageable = PageRequest.of(0, 10);
        Page<News> emptyPage = Page.empty(pageable);

        when(newsRepository.findAll(pageable))
                .thenReturn(emptyPage);

        Page<NewsResponse> result = newsService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        verify(newsRepository).findAll(pageable);
        verifyNoInteractions(newsMapper);
    }

    @Test
    void getById_shouldThrowResourceNotFoundException_whenNewsDoesNotExist() {

        User user = getUser();
        News news = getNews(user);

        when(newsRepository.findById(NEWS_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.getById(NEWS_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("1");

        verify(newsRepository).findById(NEWS_ID);
    }

    private static NewsCreateRequest getRequest() {
        return new NewsCreateRequest("Title", "Content");
    }

    private static CustomUserDetails getPrincipal() {
        User user = User.builder()
                .id(USER_ID)
                .username("testUser")
                .role(UserRole.USER)
                .build();
        return new CustomUserDetails(user);
    }

    private static NewsResponse getResponse() {
        return new NewsResponse(
                NEWS_ID,
                new UserSummaryResponse(USER_ID, "testUser"),
                "Title",
                "Content",
                LocalDateTime.now()
        );
    }

    private static News getNews(User user) {
        return News.builder()
                .id(NEWS_ID)
                .user(user)
                .title("Title")
                .content("Content")
                .dateTime(LocalDateTime.now())
                .build();
    }

    private static User getUser() {
        return User.builder()
                .id(USER_ID)
                .username("testUser")
                .password("12345678")
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();
    }
}