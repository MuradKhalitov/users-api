package ru.murad.service;

import org.junit.jupiter.api.Test;
import ru.murad.dto.UserCreateRequestDto;
import ru.murad.dto.UserUpdateRequestDto;
import ru.murad.exception.UserNotFoundException;
import ru.murad.mapper.UserMapper;
import ru.murad.model.Role;
import ru.murad.model.User;
import ru.murad.repository.RoleRepository;
import ru.murad.repository.UserRepository;
import ru.murad.service.impl.UserServiceImpl;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Test
    void delete_not_found_throws() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper  = mock(UserMapper.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper);
        UUID id = UUID.randomUUID();
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.deleteUser(id));
    }

    @Test
    void create_resolves_or_creates_role() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper  = mock(UserMapper.class);

        when(roleRepo.findByRoleName("ROLE_USER")).thenReturn(Optional.empty());
        when(roleRepo.save(any(Role.class))).thenAnswer(inv -> inv.getArgument(0));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return u;
        });
        // упрощённый маппинг, чтобы не тянуть реальный MapStruct
        when(mapper.toDto(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            return new ru.murad.dto.UserResponseDto(
                    u.getUuid(), u.getFio(), u.getPhoneNumber(), u.getAvatar(), u.getRole().getRoleName());
        });

        var service = new UserServiceImpl(userRepo, roleRepo, mapper);

        var dto = new UserCreateRequestDto("FIO","+79001234567","https://img","ROLE_USER");
        var rs = service.createUser(dto);

        assertEquals("ROLE_USER", rs.role());
        verify(roleRepo).save(any(Role.class)); // роль создана
        verify(userRepo).save(any(User.class));
    }

    @Test
    void update_not_found_throws() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper  = mock(UserMapper.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper);
        UUID id = UUID.randomUUID();
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        var req = new UserUpdateRequestDto(id,"F","+79001234567","https://img","ROLE_USER");
        assertThrows(UserNotFoundException.class, () -> service.updateUser(req));
    }
}
