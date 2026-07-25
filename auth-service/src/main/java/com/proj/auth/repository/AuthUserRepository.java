package com.proj.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proj.auth.model.AuthUser;

public interface AuthUserRepository extends JpaRepository<AuthUser, UUID> {
}
