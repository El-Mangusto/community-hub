package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.dto.response.UserSummaryResponse;
import com.elmangusto.communityhub.entity.News;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.mapper.NewsMapper;
import com.elmangusto.communityhub.repository.NewsRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long NEWS_ID = 10L;

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

        NewsResponse result = newsService.createNews(request, principal);

        assertThat(result).isEqualTo(expectedResponse);
        assertThat(mappedNews.getUser()).isEqualTo(principal.getUser());
        verify(newsRepository).save(mappedNews);
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
}