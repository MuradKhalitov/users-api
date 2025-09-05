package ru.murad.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.murad.dto.UserCreateRequestDto;
import ru.murad.dto.UserResponseDto;
import ru.murad.dto.UserUpdateRequestDto;
import ru.murad.service.UserService;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @PostMapping("/createNewUser")
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateRequestDto request) {
        UserResponseDto created = userService.createUser(request);
        return ResponseEntity
                .created(URI.create("/api/users?userID=" + created.uuid()))
                .body(created);
    }

    @GetMapping("/users")
    public ResponseEntity<UserResponseDto> get(@RequestParam UUID userID) {
        return ResponseEntity.ok(userService.getUser(userID));
    }

    @PutMapping("/userDetailsUpdate")
    public ResponseEntity<UserResponseDto> update(@Valid @RequestBody UserUpdateRequestDto request) {
        return ResponseEntity.ok(userService.updateUser(request));
    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> delete(@RequestParam UUID userID) {
        userService.deleteUser(userID);
        return ResponseEntity.noContent().build();
    }
}

