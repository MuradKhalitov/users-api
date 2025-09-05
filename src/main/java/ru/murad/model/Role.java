package ru.murad.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {

    @Id
    private UUID uuid;

    @Column(name = "role_name", nullable = false)
    private String roleName;
}

