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
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);
        UUID id = UUID.randomUUID();
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.deleteUser(id));
    }

    @Test
    void create_resolves_or_creates_role() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

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
                    u.getUuid(),
                    u.getFio(),
                    u.getPhoneNumber(),
                    u.getEmail(),
                    u.getAvatar(),
                    u.getRole().getRoleName());
        });

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);

        var dto = new UserCreateRequestDto(
                "FIO",
                "+79001234567",
                "test@example.com",
                "https://img",
                "ROLE_USER");
        var rs = service.createUser(dto);

        assertEquals("ROLE_USER", rs.role());
        assertEquals("test@example.com", rs.email());
        verify(roleRepo).save(any(Role.class)); // роль создана
        verify(userRepo).save(any(User.class));
        verify(kafkaProducerService).sendUserEvent(any()); // проверяем отправку события в Kafka
    }

    @Test
    void update_not_found_throws() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);
        UUID id = UUID.randomUUID();
        when(userRepo.findById(id)).thenReturn(Optional.empty());

        var req = new UserUpdateRequestDto(
                id,
                "F",
                "+79001234567",
                "test@example.com",
                "https://img",
                "ROLE_USER");
        assertThrows(UserNotFoundException.class, () -> service.updateUser(req));
    }

    @Test
    void get_user_returns_correct_response() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);
        UUID id = UUID.randomUUID();

        User user = User.builder()
                .uuid(id)
                .fio("Test User")
                .phoneNumber("+79001234567")
                .email("user@example.com")
                .avatar("https://img")
                .role(Role.builder().roleName("ROLE_USER").build())
                .build();

        when(userRepo.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toDto(user)).thenReturn(new ru.murad.dto.UserResponseDto(
                id, "Test User", "+79001234567", "user@example.com", "https://img", "ROLE_USER"));

        var result = service.getUser(id);

        assertNotNull(result);
        assertEquals(id, result.uuid());
        assertEquals("user@example.com", result.email());
        assertEquals("ROLE_USER", result.role());

        // Проверяем, что KafkaProducerService не вызывается при получении пользователя
        verify(kafkaProducerService, never()).sendUserEvent(any());
    }

    @Test
    void update_user_successfully() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);
        UUID id = UUID.randomUUID();

        User existingUser = User.builder()
                .uuid(id)
                .fio("Old Name")
                .phoneNumber("+79001112233")
                .email("old@example.com")
                .avatar("https://old-img")
                .role(Role.builder().roleName("ROLE_USER").build())
                .build();

        Role newRole = Role.builder()
                .uuid(UUID.randomUUID())
                .roleName("ROLE_ADMIN")
                .build();

        User updatedUser = User.builder()
                .uuid(id)
                .fio("New Name")
                .phoneNumber("+79009998877")
                .email("new@example.com")
                .avatar("https://new-img")
                .role(newRole)
                .build();

        when(userRepo.findById(id)).thenReturn(Optional.of(existingUser));
        when(roleRepo.findByRoleName("ROLE_ADMIN")).thenReturn(Optional.of(newRole));
        when(userRepo.save(any(User.class))).thenReturn(updatedUser);
        when(mapper.toDto(updatedUser)).thenReturn(new ru.murad.dto.UserResponseDto(
                id, "New Name", "+79009998877", "new@example.com", "https://new-img", "ROLE_ADMIN"));

        var req = new UserUpdateRequestDto(
                id,
                "New Name",
                "+79009998877",
                "new@example.com",
                "https://new-img",
                "ROLE_ADMIN");

        var result = service.updateUser(req);

        assertNotNull(result);
        assertEquals(id, result.uuid());
        assertEquals("New Name", result.fio());
        assertEquals("new@example.com", result.email());
        assertEquals("ROLE_ADMIN", result.role());

        // Проверяем, что KafkaProducerService не вызывается при обновлении (если только это не требуется)
        verify(kafkaProducerService, never()).sendUserEvent(any());
    }

    @Test
    void delete_user_successfully() {
        var userRepo = mock(UserRepository.class);
        var roleRepo = mock(RoleRepository.class);
        var mapper = mock(UserMapper.class);
        var kafkaProducerService = mock(KafkaProducerService.class);

        var service = new UserServiceImpl(userRepo, roleRepo, mapper, kafkaProducerService);
        UUID id = UUID.randomUUID();

        Role role = Role.builder()
                .uuid(UUID.randomUUID())
                .roleName("ROLE_USER")
                .build();

        User user = User.builder()
                .uuid(id)
                .fio("Test User")
                .phoneNumber("+79001234567")
                .email("user@example.com")
                .avatar("https://img")
                .role(role)
                .build();

        when(userRepo.findById(id)).thenReturn(Optional.of(user));
        when(userRepo.countByRole(role)).thenReturn(0L); // больше нет пользователей с этой ролью

        service.deleteUser(id);

        verify(userRepo).delete(user);
        verify(roleRepo).delete(role); // роль должна быть удалена, так как больше нет пользователей с этой ролью

        // Проверяем, что KafkaProducerService не вызывается при удалении (если только это не требуется)
        verify(kafkaProducerService, never()).sendUserEvent(any());
    }
}