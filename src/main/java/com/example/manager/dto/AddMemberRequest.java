package com.example.manager.dto;

import jakarta.validation.constraints.NotNull;

public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Vai trò trong project. Ví dụ: DEV, QA, MANAGER
     * Mặc định là DEV nếu không truyền.
     */
    private String roleInProject = "DEV";

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRoleInProject() {
        return roleInProject;
    }

    public void setRoleInProject(String roleInProject) {
        this.roleInProject = roleInProject;
    }
}
