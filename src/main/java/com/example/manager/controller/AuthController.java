package com.example.manager.controller;

import com.example.manager.dto.ApiResponse;
import com.example.manager.dto.LoginRequest;
import com.example.manager.dto.LoginResponse;
import com.example.manager.dto.RegisterRequest;
import com.example.manager.entity.User;
import com.example.manager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Authentication APIs (Register, Login)")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register", description = "Register a new user (default role: USER)")
    public ApiResponse<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ApiResponse.success("User registered", user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Login and get JWT token")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success(response);
    }
}
