package com.proj.user.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.proj.user.dtos.CreateUserRequest;
import com.proj.user.model.User;
import com.proj.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    public boolean userExistsById(UUID userId) {
        return userRepository.existsById(userId);
    }

    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));
    }

    public void createUser(CreateUserRequest request) {

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(request.password())
                .phoneNumber(request.phoneNumber())
                .status("PENDING")
                .build();

        userRepository.save(user);
        
    }

}
