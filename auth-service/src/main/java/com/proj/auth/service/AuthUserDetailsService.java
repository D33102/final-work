package com.proj.auth.service;

import java.util.Collections;
import java.util.UUID;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.proj.auth.model.AuthUser;
import com.proj.auth.repository.AuthUserRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID id;
        try {
            id = UUID.fromString(userId);
        } catch (IllegalArgumentException e) {
            throw new UsernameNotFoundException("Invalid user id: " + userId);
        }

        AuthUser authUser = authUserRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("No credentials for user id: " + userId));

        return new User(
                authUser.getUserId().toString(),
                authUser.getPasswordHash(),
                Collections.emptyList());
    }
}
