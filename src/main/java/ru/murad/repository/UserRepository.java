package ru.murad.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ru.murad.model.Role;
import ru.murad.model.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    long countByRole(Role role);
}

