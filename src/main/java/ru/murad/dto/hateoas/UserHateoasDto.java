package ru.murad.dto.hateoas;

import org.springframework.hateoas.RepresentationModel;
import ru.murad.dto.UserResponseDto;

public class UserHateoasDto extends RepresentationModel<UserHateoasDto> {

    private final UserResponseDto user;

    public UserHateoasDto(UserResponseDto user) {
        this.user = user;
    }

    public UserResponseDto getUser() {
        return user;
    }
}

