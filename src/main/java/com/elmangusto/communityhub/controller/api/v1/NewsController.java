package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/news")
public class NewsController {

    private final NewsService newsService;

    @PostMapping
    public NewsResponse createNews(@RequestBody @Valid NewsCreateRequest request,
                            @AuthenticationPrincipal CustomUserDetails principal) {
        return newsService.createNews(request, principal);
    }

}
