package com.elmangusto.communityhub.dto.response;

import java.time.LocalDateTime;

public record NewsResponse(
        Long id,
        UserSummaryResponse user,
        String title,
        String content,
        LocalDateTime dateTime
) {}
