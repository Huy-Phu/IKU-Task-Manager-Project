package com.example.manager.controller;

import com.example.manager.dto.*;
import com.example.manager.entity.Project;
import com.example.manager.entity.ProjectMember;
import com.example.manager.entity.Task;
import com.example.manager.service.ProjectService;
import com.example.manager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Project", description = "Project management APIs")
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;

    public ProjectController(ProjectService projectService, TaskService taskService) {
        this.projectService = projectService;
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(summary = "List all projects")
    public ApiResponse<List<Project>> getAllProjects() {
        return ApiResponse.success(projectService.getAllProjects());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        return ApiResponse.success(projectService.getProjectById(id));
    }

    @PostMapping
    @Operation(summary = "Create project", description = "Only MANAGER can create projects")
    public ApiResponse<Project> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        Project project = projectService.createProject(request, authentication.getName());
        return ApiResponse.success("Project created", project);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Close (soft-delete) project",
            description = "Only MANAGER can close a project. Status changes to CLOSED, data is kept.")
    public ApiResponse<Void> deleteProject(
            @PathVariable Long id,
            Authentication authentication) {
        projectService.deleteProject(id, authentication.getName());
        return ApiResponse.success("Project closed", null);
    }

    // ───────────────────────── Members ─────────────────────────

    @PostMapping("/{projectId}/members")
    @Operation(summary = "Add user to project",
            description = "Only MANAGER can add members. roleInProject: DEV (default), QA, MANAGER, etc.")
    public ApiResponse<ProjectMember> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            Authentication authentication) {
        ProjectMember member = projectService.addMember(projectId, request, authentication.getName());
        return ApiResponse.success("Member added to project", member);
    }

    @DeleteMapping("/{projectId}/members/{userId}")
    @Operation(summary = "Remove user from project",
            description = "Only MANAGER can remove members.")
    public ApiResponse<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            Authentication authentication) {
        projectService.removeMember(projectId, userId, authentication.getName());
        return ApiResponse.success("Member removed from project", null);
    }

    // ───────────────────────── Tasks ─────────────────────────

    @PostMapping("/{projectId}/tasks")
    @Operation(summary = "Create task in project",
            description = "projectId comes from URL path. MANAGER: any project. USER: must be a project member.")
    public ApiResponse<Task> createTask(
            @Parameter(description = "ID of the project to create task in", required = true)
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskInProjectRequest request,
            Authentication authentication) {
        Task task = taskService.createTaskInProject(projectId, request, authentication.getName());
        return ApiResponse.success("Task created", task);
    }

    @GetMapping("/{projectId}/tasks")
    @Operation(summary = "Get tasks in project",
            description = "MANAGER sees all tasks. USER sees only their own tasks in the project.")
    public ApiResponse<List<Task>> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        List<Task> tasks = taskService.getTasksByProjectForUser(projectId, authentication.getName());
        return ApiResponse.success(tasks);
    }
}
