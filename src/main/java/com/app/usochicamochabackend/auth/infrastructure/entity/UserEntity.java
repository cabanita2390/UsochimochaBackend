package com.app.usochicamochabackend.auth.infrastructure.entity;

import com.app.usochicamochabackend.auth.domain.enums.RoleEnum;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "full_name")
    private String fullName;
    private String status;
    private String username;
    private String password;
    private String email;
    @Enumerated(EnumType.STRING)
    private RoleEnum role;
}
