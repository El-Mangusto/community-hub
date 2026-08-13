package com.elmangusto.communityhub.service;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.entity.News;
import com.elmangusto.communityhub.mapper.NewsMapper;
import com.elmangusto.communityhub.repository.NewsRepository;
import com.elmangusto.communityhub.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional
public class NewsService {

    private final NewsRepository newsRepository;
    private final NewsMapper newsMapper;

    public NewsResponse createNews(NewsCreateRequest request, CustomUserDetails principal) {

        log.info("Creating news by a user userId={}", principal.getId());

        News news = newsMapper.toEntity(request);
        news.setUser(principal.getUser());
        News saved = newsRepository.save(news);

        log.info("User created news successfully. newsId={}, userId={}", saved.getId(), principal.getId());

        return newsMapper.toResponse(saved);
    }
}
