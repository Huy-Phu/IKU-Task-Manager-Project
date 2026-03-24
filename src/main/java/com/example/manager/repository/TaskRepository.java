package com.example.manager.repository;

import com.example.manager.entity.Project;
import com.example.manager.entity.Task;
import com.example.manager.entity.TaskStatus;
import com.example.manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssignee(User assignee);

    List<Task> findByProject(Project project);

    List<Task> findByProjectAndAssignee(Project project, User assignee);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByStatusAndAssignee(TaskStatus status, User assignee);

    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
}
