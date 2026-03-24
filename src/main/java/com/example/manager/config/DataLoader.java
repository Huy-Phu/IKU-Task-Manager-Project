package com.example.manager.config;

import com.example.manager.entity.Role;
import com.example.manager.entity.User;
import com.example.manager.repository.RoleRepository;
import com.example.manager.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Seed initial data nếu database trống (chỉ chạy khi chưa có user)
 * Default: manager1 / password
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        Role managerRole = roleRepository.findByName("MANAGER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("MANAGER");
                    return roleRepository.save(r);
                });
        roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role r = new Role();
                    r.setName("USER");
                    return roleRepository.save(r);
                });

        User manager = new User();
        manager.setUsername("manager1");
        manager.setFullName("Manager One");
        manager.setEmail("manager1@example.com");
        manager.setPasswordHash(passwordEncoder.encode("password"));
        manager.setStatus("ACTIVE");
        manager.setCreatedAt(java.time.LocalDateTime.now());
        manager.setUpdatedAt(java.time.LocalDateTime.now());
        manager.setRoles(Set.of(managerRole));
        userRepository.save(manager);
    }
}
