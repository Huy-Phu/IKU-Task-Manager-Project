package com.example.manager.service;

import com.example.manager.dto.UpdateUserRequest;
import com.example.manager.entity.Role;
import com.example.manager.entity.User;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.RoleRepository;
import com.example.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

        // Cập nhật roles nếu client có gửi trường "roles"
        if (request.getRoles() != null) {
            if (request.getRoles().isEmpty()) {
                throw new BusinessException("User must have at least one role");
            }

            Set<Role> newRoles = new HashSet<>();
            for (String roleName : request.getRoles()) {
                String normalized = roleName.trim().toUpperCase();
                if (!"USER".equals(normalized) && !"MANAGER".equals(normalized)) {
                    throw new BusinessException("Invalid role: '" + roleName + "'. Allowed values: USER, MANAGER");
                }
                Role role = roleRepository.findByName(normalized)
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + normalized));
                newRoles.add(role);
            }
            user.setRoles(newRoles);
        }

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
