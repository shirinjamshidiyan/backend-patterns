package com.shirin.twofactorauthentication.repository;

import com.shirin.twofactorauthentication.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

   Optional<UserEntity> findByEmail(String email);
}
