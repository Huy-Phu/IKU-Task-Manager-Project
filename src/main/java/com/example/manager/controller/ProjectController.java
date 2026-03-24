package com.example.manager.controller;

import com.example.manager.dto.ApiResponse;
import com.example.manager.dto.CreateProjectRequest;
import com.example.manager.entity.Project;
import com.example.manager.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
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

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @GetMapping
    @Operation(summary = "List all projects")
    public ApiResponse<List<Project>> getAllProjects() {
        List<Project> projects = projectService.getAllProjects();
        return ApiResponse.success(projects);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get project by ID")
    public ApiResponse<Project> getProjectById(@PathVariable Long id) {
        Project project = projectService.getProjectById(id);
        return ApiResponse.success(project);
    }

    @PostMapping
    @Operation(summary = "Create project", description = "Only MANAGER can create projects")
    public ApiResponse<Project> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        Project project = projectService.createProject(request, username);
        return ApiResponse.success("Project created", project);
    }
}
