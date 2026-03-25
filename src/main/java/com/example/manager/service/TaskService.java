package com.example.manager.service;

import com.example.manager.dto.AssignTaskRequest;
import com.example.manager.dto.CreateTaskInProjectRequest;
import com.example.manager.dto.CreateTaskRequest;
import com.example.manager.dto.UpdateTaskStatusRequest;
import com.example.manager.entity.*;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;

    public TaskService(TaskRepository taskRepository,
                       ProjectRepository projectRepository,
                       UserRepository userRepository,
                       ProjectMemberRepository projectMemberRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
    }

    public List<Task> getTasksForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return taskRepository.findByAssignee(user);
    }

    public List<Task> getTasksByUserId(Long userId, String requesterUsername) {
        User requester = userRepository.findByUsername(requesterUsername)
                .orElseThrow(() -> new ResourceNotFoundException("Requester not found"));
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        boolean isManager = requester.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager && !requester.getId().equals(targetUser.getId())) {
            throw new BusinessException("Only MANAGER can view tasks of other users");
        }

        return taskRepository.findByAssignee(targetUser);
    }

    public List<Task> getTasksByProjectForUser(Long projectId, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = user.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));

        if (!isManager) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
            if (!isMember) {
                throw new BusinessException("User is not a member of this project");
            }
        }

        if (isManager) {
            return taskRepository.findByProject(project);
        }
        return taskRepository.findByProjectAndAssignee(project, user);
    }

    public List<Task> getTasksByStatus(TaskStatus status, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = user.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));

        if (isManager) {
            return taskRepository.findByStatus(status);
        }
        return taskRepository.findByStatusAndAssignee(status, user);
    }

    public List<Task> getTasksByProjectAndStatus(Long projectId, TaskStatus status, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = user.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));

        if (!isManager) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), user.getId());
            if (!isMember) {
                throw new BusinessException("User is not a member of this project");
            }
        }

        return taskRepository.findByProjectAndStatus(project, status);
    }

    @Transactional
    public Task createTask(CreateTaskRequest request, String username) {
        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = creator.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));

        if (!isManager) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), creator.getId());
            if (!isMember) {
                throw new BusinessException("Only project members can create tasks");
            }
        }

        return buildAndSaveTask(project, request.getTitle(), request.getDescription(),
                request.getPriority(), request.getDeadline());
    }

    /**
     * Tạo task theo projectId từ URL path.
     * MANAGER: tạo trong bất kỳ project nào.
     * USER: phải là member của project.
     */
    @Transactional
    public Task createTaskInProject(Long projectId, CreateTaskInProjectRequest request, String username) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User creator = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = creator.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));

        if (!isManager) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(project.getId(), creator.getId());
            if (!isMember) {
                throw new BusinessException("Only project members can create tasks");
            }
        }

        return buildAndSaveTask(project, request.getTitle(), request.getDescription(),
                request.getPriority(), request.getDeadline());
    }

    private Task buildAndSaveTask(Project project, String title, String description,
                                   TaskPriority priority, java.time.LocalDateTime deadline) {
        Task task = new Task();
        task.setProject(project);
        task.setTitle(title.trim());
        task.setDescription(description != null ? description.trim() : null);
        task.setPriority(priority != null ? priority : TaskPriority.MEDIUM);
        task.setDeadline(deadline);
        task.setStatus(TaskStatus.TODO);
        return taskRepository.save(task);
    }

    @Transactional
    public Task assignTask(Long taskId, AssignTaskRequest request, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException("Cannot assign a task that is already DONE");
        }

        User assignee = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(
                task.getProject().getId(), assignee.getId());
        if (!isMember) {
            throw new BusinessException("User must be a project member to be assigned tasks");
        }

        task.setAssignee(assignee);
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskStatus(Long taskId, UpdateTaskStatusRequest request, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        if (task.getStatus() == TaskStatus.DONE) {
            throw new BusinessException("Cannot update status of a task that is already DONE");
        }

        TaskStatus newStatus = request.getStatus();
        TaskStatus current = task.getStatus();

        if (current == TaskStatus.TODO && (newStatus == TaskStatus.IN_PROGRESS || newStatus == TaskStatus.DONE)) {
            task.setStatus(newStatus);
        } else if (current == TaskStatus.IN_PROGRESS && newStatus == TaskStatus.DONE) {
            task.setStatus(newStatus);
        } else {
            throw new BusinessException("Invalid status transition: " + current + " -> " + newStatus);
        }

        return taskRepository.save(task);
    }

    /**
     * Xóa task. Chỉ MANAGER được xóa.
     */
    @Transactional
    public void deleteTask(Long taskId, String username) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + taskId));

        User actor = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isManager = actor.getRoles().stream()
                .anyMatch(r -> "MANAGER".equals(r.getName()));
        if (!isManager) {
            throw new BusinessException("Only MANAGER can delete tasks");
        }

        taskRepository.delete(task);
    }
}
