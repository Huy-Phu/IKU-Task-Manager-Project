package com.example.manager.dto;

import jakarta.validation.constraints.NotNull;

public class AssignTaskRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
