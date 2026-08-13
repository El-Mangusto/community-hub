package com.elmangusto.communityhub.mapper;

import com.elmangusto.communityhub.dto.request.NewsCreateRequest;
import com.elmangusto.communityhub.dto.response.NewsResponse;
import com.elmangusto.communityhub.entity.News;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface NewsMapper {

    News toEntity(NewsCreateRequest request);

    NewsResponse toResponse(News news);
}
