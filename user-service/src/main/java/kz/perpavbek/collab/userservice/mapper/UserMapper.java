package kz.perpavbek.collab.userservice.mapper;

import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserResponse toResponse(User user);

    User toEntity(UserResponse userResponse);
}