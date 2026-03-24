package com.example.manager.service;

import com.example.manager.dto.UpdateUserRequest;
import com.example.manager.entity.User;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public User updateUser(Long id, UpdateUserRequest request) {
        User user = getUserById(id);

        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(normalizedEmail) && userRepository.existsByEmail(normalizedEmail)) {
            throw new BusinessException("Email already exists");
        }

        String normalizedStatus = request.getStatus().trim().toUpperCase();
        if (!"ACTIVE".equals(normalizedStatus) && !"INACTIVE".equals(normalizedStatus)) {
            throw new BusinessException("Status must be ACTIVE or INACTIVE");
        }

        user.setFullName(request.getFullName().trim());
        user.setEmail(normalizedEmail);
        user.setStatus(normalizedStatus);
        user.setUpdatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = getUserById(id);
        user.setStatus("INACTIVE");
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
