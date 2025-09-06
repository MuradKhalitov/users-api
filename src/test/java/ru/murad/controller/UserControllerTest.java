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
import ru.murad.exception.UserNotFoundException;
import ru.murad.service.UserService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean UserService userService;

    @Test
    void create_ok_201() throws Exception {
        UUID id = UUID.randomUUID();
        Mockito.when(userService.createUser(any(UserCreateRequestDto.class)))
                .thenReturn(new UserResponseDto(id, "FIO", "+79001234567", "https://img", "ROLE_USER"));

        var body = new UserCreateRequestDto("FIO","+79001234567","https://img","ROLE_USER");

        mvc.perform(post("/api/createNewUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location","/api/users?userID=" + id))
                .andExpect(jsonPath("$.uuid").value(id.toString()))
                .andExpect(jsonPath("$.role").value("ROLE_USER"));
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
        var req = new UserUpdateRequestDto(id, "New FIO","+79005557788","https://img2","ROLE_ADMIN");
        Mockito.when(userService.updateUser(any(UserUpdateRequestDto.class)))
                .thenReturn(new UserResponseDto(id, req.fio(), req.phoneNumber(), req.avatar(), req.role()));

        mvc.perform(put("/api/userDetailsUpdate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fio").value("New FIO"))
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }
}
