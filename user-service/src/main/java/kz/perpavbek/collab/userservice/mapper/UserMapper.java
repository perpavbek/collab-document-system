package kz.perpavbek.collab.userservice.mapper;

import kz.perpavbek.collab.userservice.dto.request.RegisterRequest;
import kz.perpavbek.collab.userservice.dto.response.UserResponse;
import kz.perpavbek.collab.userservice.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    User toEntity(RegisterRequest request);
}