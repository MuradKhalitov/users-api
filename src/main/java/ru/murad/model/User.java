package ru.murad.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    private UUID uuid;

    @Column(nullable = false)
    private String fio;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column
    private String avatar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}

