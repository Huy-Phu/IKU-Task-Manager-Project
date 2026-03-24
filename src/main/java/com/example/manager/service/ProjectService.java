package com.example.manager.service;

import com.example.manager.dto.CreateProjectRequest;
import com.example.manager.entity.Project;
import com.example.manager.entity.User;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.ProjectRepository;
import com.example.manager.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));
    }

    @Transactional
    public Project createProject(CreateProjectRequest request, String username) {
        if (projectRepository.existsByCode(request.getCode().trim())) {
            throw new BusinessException("Project code already exists: " + request.getCode());
        }

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = creator.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) {
            throw new BusinessException("Only MANAGER can create projects");
        }

        Project project = new Project();
        project.setName(request.getName().trim());
        project.setCode(request.getCode().trim().toUpperCase());
        project.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setStatus("ACTIVE");
        project.setCreatedBy(creator);
        LocalDateTime now = LocalDateTime.now();
        project.setCreatedAt(now);
        project.setUpdatedAt(now);

        return projectRepository.save(project);
    }
}
