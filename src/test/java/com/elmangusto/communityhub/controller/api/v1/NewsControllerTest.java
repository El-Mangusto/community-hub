package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.config.SecurityConfig;
import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.dto.response.UserSummaryResponse;
import com.elmangusto.communityhub.entity.User;
import com.elmangusto.communityhub.entity.enums.UserRole;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.security.CustomUserDetailsService;
import com.elmangusto.communityhub.security.JwtAuthFilter;
import com.elmangusto.communityhub.service.NewsService;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsController.class)
@Import(SecurityConfig.class)
class NewsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    private NewsService newsService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthFilter).doFilter(any(), any(), any());
    }

    @Test
    void createNews_returns200AndBody_whenAuthenticatedAndValid() throws Exception {

        NewsCreateRequest request = new NewsCreateRequest("Title", "Content");
        CustomUserDetails principal = getPrincipal();

        NewsResponse response = new NewsResponse(
                10L,
                new UserSummaryResponse(1L, "testUser"),
                "Title",
                "Content",
                LocalDateTime.now()
        );

        when(newsService.createNews(any(NewsCreateRequest.class), any(CustomUserDetails.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/news")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.content").value("Content"))
                .andExpect(jsonPath("$.user.username").value("testUser"));
    }

    @Test
    void createNews_returns401_whenNotAuthenticated() throws Exception {

        NewsCreateRequest request = new NewsCreateRequest("Title", "Content");

        mockMvc.perform(post("/api/v1/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(newsService);
    }

    @Test
    void createNews_returns400_whenTitleBlank() throws Exception {

        NewsCreateRequest request = new NewsCreateRequest("", "Content");
        CustomUserDetails principal = getPrincipal();

        mockMvc.perform(post("/api/v1/news")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(newsService);
    }

    @Test
    void createNews_returns400_whenContentTooLong() throws Exception {

        String tooLong = "a".repeat(10_001);
        NewsCreateRequest request = new NewsCreateRequest("Title", tooLong);
        CustomUserDetails principal = getPrincipal();

        mockMvc.perform(post("/api/v1/news")
                        .with(user(principal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(newsService);
    }

    private static CustomUserDetails getPrincipal() {
        User user = User.builder()
                .id(1L)
                .username("testUser")
                .role(UserRole.USER)
                .build();
        return new CustomUserDetails(user);
    }

}