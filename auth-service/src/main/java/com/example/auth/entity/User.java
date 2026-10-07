package com.example.auth.entity;

import com.example.auth.enums.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")   // "user" is a reserved word in PostgreSQL, so we rename the table
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;
}