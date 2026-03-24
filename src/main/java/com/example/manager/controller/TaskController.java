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
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksForUser(username);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "Get tasks by user",
            description = "MANAGER can view tasks of any user. USER can only view their own tasks.")
    public ApiResponse<List<Task>> getTasksByUser(
            @PathVariable Long userId,
            Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByUserId(userId, username);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/by-project/{projectId}")
    @Operation(summary = "Get tasks by project",
            description = "Get tasks of a project. User must be a project member.")
    public ApiResponse<List<Task>> getTasksByProject(
            @PathVariable Long projectId,
            Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByProjectForUser(projectId, username);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/by-status")
    @Operation(summary = "Get tasks by status",
            description = "Filter tasks by status (TODO / IN_PROGRESS / DONE). MANAGER sees all; USER sees only own tasks.")
    public ApiResponse<List<Task>> getTasksByStatus(
            @Parameter(description = "Task status: TODO, IN_PROGRESS, DONE", required = true)
            @RequestParam TaskStatus status,
            Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByStatus(status, username);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/by-project/{projectId}/by-status")
    @Operation(summary = "Get tasks by project and status",
            description = "Filter tasks within a specific project by status. User must be a project member.")
    public ApiResponse<List<Task>> getTasksByProjectAndStatus(
            @PathVariable Long projectId,
            @Parameter(description = "Task status: TODO, IN_PROGRESS, DONE", required = true)
            @RequestParam TaskStatus status,
            Authentication authentication) {
        String username = authentication.getName();
        List<Task> tasks = taskService.getTasksByProjectAndStatus(projectId, status, username);
        return ApiResponse.success(tasks);
    }

    @PostMapping
    @Operation(summary = "Create task",
            description = "Create a new task under a project. Creator must be a project member. Default status: TODO.")
    public ApiResponse<Task> createTask(
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Task task = taskService.createTask(request, username);
        return ApiResponse.success("Task created", task);
    }

    @PutMapping("/{taskId}/assign")
    @Operation(summary = "Assign task",
            description = "Assign a task to a user. The user must be a project member. Cannot assign if status is DONE.")
    public ApiResponse<Task> assignTask(
            @PathVariable Long taskId,
            @Valid @RequestBody AssignTaskRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Task task = taskService.assignTask(taskId, request, username);
        return ApiResponse.success("Task assigned", task);
    }

    @PatchMapping("/{taskId}/status")
    @Operation(summary = "Update task status",
            description = "Valid transitions: TODO→IN_PROGRESS, TODO→DONE (fast track), IN_PROGRESS→DONE. Cannot update if already DONE.")
    public ApiResponse<Task> updateTaskStatus(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskStatusRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Task task = taskService.updateTaskStatus(taskId, request, username);
        return ApiResponse.success("Status updated", task);
    }
}
