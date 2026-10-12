package com.shirin.twofactorauthentication.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor

public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    @Column(nullable = false , unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private boolean enabled;

    @Enumerated(EnumType.STRING)
    private Role role;


}



