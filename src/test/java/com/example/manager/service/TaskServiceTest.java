package com.example.manager.service;

import com.example.manager.dto.AssignTaskRequest;
import com.example.manager.dto.CreateTaskRequest;
import com.example.manager.dto.UpdateTaskStatusRequest;
import com.example.manager.entity.*;
import com.example.manager.exception.BusinessException;
import com.example.manager.exception.ResourceNotFoundException;
import com.example.manager.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProjectMemberRepository projectMemberRepository;

    @InjectMocks
    private TaskService taskService;

    private Project project;
    private User creator;
    private Task task;
    private User assignee;

    @BeforeEach
    void setUp() {
        creator = new User();
        creator.setId(1L);
        creator.setUsername("user1");

        project = new Project();
        project.setId(1L);
        project.setName("Test Project");

        task = new Task();
        task.setId(1L);
        task.setProject(project);
        task.setTitle("Test Task");
        task.setStatus(TaskStatus.TODO);

        assignee = new User();
        assignee.setId(2L);
        assignee.setUsername("user2");
    }

    @Test
    void createTask_projectNotFound_throwsResourceNotFoundException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(999L);
        request.setTitle("New Task");

        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> taskService.createTask(request, "user1"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void createTask_userNotMember_throwsBusinessException() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setProjectId(1L);
        request.setTitle("New Task");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(project));
        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(creator));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 1L)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> taskService.createTask(request, "user1"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_taskDone_throwsBusinessException() {
        task.setStatus(TaskStatus.DONE);
        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class,
                () -> taskService.assignTask(1L, request, "user1"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void assignTask_userNotInProject_throwsBusinessException() {
        AssignTaskRequest request = new AssignTaskRequest();
        request.setUserId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(projectMemberRepository.existsByProjectIdAndUserId(1L, 2L)).thenReturn(false);

        assertThrows(BusinessException.class,
                () -> taskService.assignTask(1L, request, "user1"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_taskDone_throwsBusinessException() {
        task.setStatus(TaskStatus.DONE);
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.IN_PROGRESS);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class,
                () -> taskService.updateTaskStatus(1L, request, "user1"));
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatus_invalidTransition_throwsBusinessException() {
        UpdateTaskStatusRequest request = new UpdateTaskStatusRequest();
        request.setStatus(TaskStatus.TODO); // Invalid: TODO -> TODO

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(BusinessException.class,
                () -> taskService.updateTaskStatus(1L, request, "user1"));
    }
}
