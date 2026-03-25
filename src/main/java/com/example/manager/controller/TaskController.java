package com.example.manager.controller;

import com.example.manager.dto.*;
import com.example.manager.entity.Task;
import com.example.manager.entity.TaskStatus;
import com.example.manager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Task", description = "Task management APIs")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/my")
    @Operation(summary = "Get my tasks", description = "Get all tasks assigned to the current logged-in user")
    public ApiResponse<List<Task>> getMyTasks(Authentication authentication) {
        return ApiResponse.success(taskService.getTasksForUser(authentication.getName()));
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get tasks by user",
            description = "MANAGER can view tasks of any user. USER can only view their own tasks.")
    public ApiResponse<List<Task>> getTasksByUser(
            @PathVariable Long userId,
            Authentication authentication) {
        return ApiResponse.success(taskService.getTasksByUserId(userId, authentication.getName()));
    }

    @GetMapping("/by-project/{projectId}")
    @Operation(summary = "Get tasks by project",
            description = "MANAGER sees all tasks. USER sees only their own tasks in the project.")
    public ApiResponse<List<Task>> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        return ApiResponse.success(taskService.getTasksByProjectForUser(projectId, authentication.getName()));
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get tasks by status",
            description = "Filter tasks by status (TODO / IN_PROGRESS / DONE). MANAGER sees all; USER sees only own tasks.")
    public ApiResponse<List<Task>> getTasksByStatus(
            @Parameter(description = "Task status: TODO, IN_PROGRESS, DONE", required = true)
            @RequestParam TaskStatus status,
            Authentication authentication) {
        return ApiResponse.success(taskService.getTasksByStatus(status, authentication.getName()));
    }

    @GetMapping("/by-project/{projectId}/by-status")
    @Operation(summary = "Get tasks by project and status",
            description = "Filter tasks within a specific project by status. User must be a project member.")
    public ApiResponse<List<Task>> getTasksByProjectAndStatus(
            @PathVariable Long projectId,
            @Parameter(description = "Task status: TODO, IN_PROGRESS, DONE", required = true)
            @RequestParam TaskStatus status,
            Authentication authentication) {
        return ApiResponse.success(taskService.getTasksByProjectAndStatus(projectId, status, authentication.getName()));
    }

    @PostMapping
    @Operation(summary = "Create task (projectId in body)",
            description = "Requires projectId in request body. Alternatively use POST /api/projects/{projectId}/tasks.")
    public ApiResponse<Task> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        Task task = taskService.createTask(request, authentication.getName());
        return ApiResponse.success("Task created", task);
    }

    @PutMapping("/{taskId}/assign")
    @Operation(summary = "Assign task to user",
            description = "Assignee must be a project member. Cannot assign if task is DONE.")
    public ApiResponse<Task> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        Task task = taskService.assignTask(taskId, request, authentication.getName());
        return ApiResponse.success("Task assigned", task);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status",
            description = "Valid transitions: TODO→IN_PROGRESS, TODO→DONE (fast track), IN_PROGRESS→DONE. Cannot update if already DONE.")
    public ApiResponse<Task> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        Task task = taskService.updateTaskStatus(taskId, request, authentication.getName());
        return ApiResponse.success("Status updated", task);
    }

    @DeleteMapping("/{taskId}")
    @Operation(summary = "Delete task",
            description = "Only MANAGER can delete tasks. Task is permanently removed from the database.")
    public ApiResponse<Void> deleteTask(
            @PathVariable Long taskId,
            Authentication authentication) {
        taskService.deleteTask(taskId, authentication.getName());
        return ApiResponse.success("Task deleted", null);
    }
}
