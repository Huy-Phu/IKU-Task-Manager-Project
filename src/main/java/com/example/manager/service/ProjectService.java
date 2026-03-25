package com.example.manager.service;

import com.example.manager.dto.AddMemberRequest;
import com.example.manager.dto.CreateProjectRequest;
import com.example.manager.entity.Project;
import com.example.manager.entity.ProjectMember;
import com.example.manager.entity.User;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.ProjectMemberRepository;
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
    private final ProjectMemberRepository projectMemberRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          ProjectMemberRepository projectMemberRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
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

    /**
     * Đóng (soft delete) một project. Chỉ MANAGER được thực hiện.
     * Status chuyển thành CLOSED, dữ liệu task vẫn giữ nguyên.
     */
    @Transactional
    public void deleteProject(Long projectId, String username) {
        Project project = getProjectById(projectId);

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) {
            throw new BusinessException("Only MANAGER can delete projects");
        }

        if ("CLOSED".equals(project.getStatus())) {
            throw new BusinessException("Project is already closed");
        }

        project.setStatus("CLOSED");
        project.setUpdatedAt(LocalDateTime.now());
        projectRepository.save(project);
    }

    /**
     * Thêm user vào project. Chỉ MANAGER được thực hiện.
     */
    @Transactional
    public ProjectMember addMember(Long projectId, AddMemberRequest request, String username) {
        Project project = getProjectById(projectId);

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) {
            throw new BusinessException("Only MANAGER can add members to a project");
        }

        User newMember = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, newMember.getId())) {
            throw new BusinessException("User is already a member of this project");
        }

        String roleInProject = (request.getRoleInProject() != null && !request.getRoleInProject().isBlank())
                ? request.getRoleInProject().trim().toUpperCase()
                : "DEV";

        ProjectMember member = new ProjectMember();
        member.setProjectId(project.getId());
        member.setUserId(newMember.getId());
        member.setRoleInProject(roleInProject);

        return projectMemberRepository.save(member);
    }

    /**
     * Xóa user khỏi project. Chỉ MANAGER được thực hiện.
     */
    @Transactional
    public void removeMember(Long projectId, Long userId, String username) {
        getProjectById(projectId);

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) {
            throw new BusinessException("Only MANAGER can remove members from a project");
        }

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("User is not a member of this project");
        }

        projectMemberRepository.deleteById(new com.example.manager.entity.ProjectMemberId(projectId, userId));
    }
}
