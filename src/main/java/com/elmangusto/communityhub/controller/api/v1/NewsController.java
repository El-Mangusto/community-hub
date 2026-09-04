package com.elmangusto.communityhub.controller.api.v1;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.security.CustomUserDetails;
import com.elmangusto.communityhub.service.NewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public Page<NewsResponse> getAll(
            @ParameterObject
            @PageableDefault(size = 10, sort = "login")
            Pageable pageable
    ) {
        return newsService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public NewsResponse getById(@PathVariable Long id) {
        return newsService.getById(id);
    }

}
