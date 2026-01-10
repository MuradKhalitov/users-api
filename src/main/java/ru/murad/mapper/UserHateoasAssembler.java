package ru.murad.mapper;

import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;
import ru.murad.controller.UserController;
import ru.murad.dto.UserResponseDto;
import ru.murad.dto.hateoas.UserHateoasDto;

import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class UserHateoasAssembler {

    public UserHateoasDto toModel(UserResponseDto dto) {
        UUID userId = dto.getUuid();

        UserHateoasDto model = new UserHateoasDto(dto);

        model.add(
                WebMvcLinkBuilder.linkTo(
                        methodOn(UserController.class).get(userId)
                ).withSelfRel()
        );

        model.add(
                WebMvcLinkBuilder.linkTo(
                        methodOn(UserController.class).update(null)
                ).withRel("update")
        );

        model.add(
                WebMvcLinkBuilder.linkTo(
                        methodOn(UserController.class).delete(userId)
                ).withRel("delete")
        );

        return model;
    }
}
