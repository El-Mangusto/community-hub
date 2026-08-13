package com.elmangusto.communityhub.mapper;

import com.elmangusto.communityhub.dto.request.UserRegisterRequest;
import com.elmangusto.communityhub.dto.response.UserResponse;
import com.elmangusto.communityhub.dto.response.UserSummaryResponse;
import com.elmangusto.communityhub.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserRegisterRequest request);

    UserResponse toResponse(User user);

    UserSummaryResponse toSummaryResponse(User user);
}
