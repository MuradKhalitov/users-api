package ru.murad.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.murad.dto.UserCreateRequestDto;
import ru.murad.dto.UserResponseDto;
import ru.murad.dto.UserUpdateRequestDto;
import ru.murad.dto.hateoas.UserHateoasDto;
import ru.murad.exception.UserNotFoundException;
import ru.murad.mapper.UserHateoasAssembler;
import ru.murad.service.UserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockBean
    UserService userService;
    @MockBean
    UserHateoasAssembler hateoasAssembler;

    @Test
    void create_ok_201() throws Exception {
        UUID id = UUID.randomUUID();

        UserResponseDto response =
                new UserResponseDto(
                        id,
                        "FIO",
                        "+79001234567",
                        "test@example.com",
                        "https://img",
                        "ROLE_USER");

        Mockito.when(userService.createUser(any(UserCreateRequestDto.class)))
                .thenReturn(response);

        Mockito.when(hateoasAssembler.toModel(response))
                .thenReturn(new UserHateoasDto(response));

        var body = new UserCreateRequestDto(
                "FIO",
                "+79001234567",
                "test@example.com",
                "https://img",
                "ROLE_USER");

        mvc.perform(post("/api/createNewUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/users?userID=" + id))
                .andExpect(jsonPath("$.user.uuid").value(id.toString()))
                .andExpect(jsonPath("$.user.role").value("ROLE_USER"));
    }


    @Test
    void create_validation_400() throws Exception {
        // Невалидный телефон и URL
        var body = """
                {"fio":"A","phoneNumber":"invalid","avatar":"not-url","role":"ROLE_USER"}
                """;
        mvc.perform(post("/api/createNewUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void get_not_found_404() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userService.getUser(id)).thenThrow(new UserNotFoundException(id));

        mvc.perform(get("/api/users").param("userID", id.toString()))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_ok_200() throws Exception {
        UUID id = UUID.randomUUID();

        var req = new UserUpdateRequestDto(
                id,
                "New FIO",
                "+79005557788",
                "test@example.com",
                "https://img2",
                "ROLE_ADMIN");

        UserResponseDto response =
                new UserResponseDto(id, req.fio(), req.phoneNumber(), req.email(), req.avatar(), req.role());

        Mockito.when(userService.updateUser(any(UserUpdateRequestDto.class)))
                .thenReturn(response);

        Mockito.when(hateoasAssembler.toModel(response))
                .thenReturn(new UserHateoasDto(response));

        mvc.perform(put("/api/userDetailsUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user.fio").value("New FIO"))
                .andExpect(jsonPath("$.user.role").value("ROLE_ADMIN"));
    }

}
