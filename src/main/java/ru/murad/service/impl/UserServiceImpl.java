package ru.murad.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.murad.dto.UserCreateRequestDto;
import ru.murad.dto.UserResponseDto;
import ru.murad.dto.UserUpdateRequestDto;
import ru.murad.exception.UserNotFoundException;
import ru.murad.mapper.UserMapper;
import ru.murad.model.Role;
import ru.murad.model.User;
import ru.murad.repository.RoleRepository;
import ru.murad.repository.UserRepository;
import ru.murad.service.UserService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CachePut(value = "users", key = "#result.uuid()", unless = "#result == null")
    public UserResponseDto createUser(UserCreateRequestDto request) {
        Role role = resolveOrCreateRole(request.role());

        User user = User.builder()
                .uuid(UUID.randomUUID())
                .fio(request.fio())
                .phoneNumber(request.phoneNumber())
                .avatar(request.avatar())
                .role(role)
                .build();

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Cacheable(value = "users", key = "#uuid")
    public UserResponseDto getUser(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#request.uuid()")
    public UserResponseDto updateUser(UserUpdateRequestDto request) {
        User user = userRepository.findById(request.uuid())
                .orElseThrow(() -> new UserNotFoundException(request.uuid()));

        user.setFio(request.fio());
        user.setPhoneNumber(request.phoneNumber());
        user.setAvatar(request.avatar());
        user.setRole(resolveOrCreateRole(request.role()));

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#uuid")
    public void deleteUser(UUID uuid) {
        User user = userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException(uuid));
        userRepository.delete(user);

        if (userRepository.countByRole(user.getRole()) == 0) {
            roleRepository.delete(user.getRole());
        }
    }

    private Role resolveOrCreateRole(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .uuid(UUID.randomUUID())
                                .roleName(roleName)
                                .build()
                ));
    }
}
