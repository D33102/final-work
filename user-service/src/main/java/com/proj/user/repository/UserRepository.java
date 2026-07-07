package com.proj.user.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.user.model.User;

public interface UserRepository extends JpaRepository<User, UUID> {
    
}
