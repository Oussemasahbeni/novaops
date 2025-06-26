package com.novaops.userservice.infrastructure.entity;

import com.novaops.userservice.domain.enums.RoleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_roles")
public class RoleEntity {
    @Id
    @GeneratedValue
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType name;
}