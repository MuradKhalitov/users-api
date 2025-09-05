package ru.murad.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.murad.dto.UserResponseDto;
import ru.murad.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "role.roleName", target = "role")
    UserResponseDto toDto(User user);
}

