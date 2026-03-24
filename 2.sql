USE intern_task_manager
IF DB_ID('intern_task_manager') IS NULL
BEGIN
    CREATE DATABASE intern_task_manager;
END;
GO

USE intern_task_manager;
GO

IF OBJECT_ID('user_roles', 'U') IS NOT NULL DROP TABLE user_roles;
IF OBJECT_ID('project_members', 'U') IS NOT NULL DROP TABLE project_members;
IF OBJECT_ID('tasks', 'U') IS NOT NULL DROP TABLE tasks;
IF OBJECT_ID('projects', 'U') IS NOT NULL DROP TABLE projects;
IF OBJECT_ID('roles', 'U') IS NOT NULL DROP TABLE roles;
IF OBJECT_ID('users', 'U') IS NOT NULL DROP TABLE users;

CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username        NVARCHAR(50)  NOT NULL UNIQUE,
    full_name       NVARCHAR(100) NOT NULL,
    email           NVARCHAR(100) NOT NULL UNIQUE,
    password_hash   NVARCHAR(255) NOT NULL,
    status          NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at      DATETIME2     NOT NULL DEFAULT GETDATE()
);

CREATE TABLE roles (
    id   BIGINT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

CREATE TABLE projects (
    id          BIGINT IDENTITY(1,1) PRIMARY KEY,
    name        NVARCHAR(100) NOT NULL,
    code        NVARCHAR(50)  NOT NULL UNIQUE,
    description NVARCHAR(MAX),
    start_date  DATE,
    end_date    DATE,
    status      NVARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_by  BIGINT        NOT NULL,
    created_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at  DATETIME2     NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE project_members (
    project_id       BIGINT      NOT NULL,
    user_id          BIGINT      NOT NULL,
    role_in_project  NVARCHAR(50) NOT NULL DEFAULT 'DEV',
    PRIMARY KEY (project_id, user_id),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE tasks (
    id           BIGINT IDENTITY(1,1) PRIMARY KEY,
    project_id   BIGINT       NOT NULL,
    assignee_id  BIGINT           NULL,
    title        NVARCHAR(200) NOT NULL,
    description  NVARCHAR(MAX),
    status       NVARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority     NVARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    deadline     DATETIME2,
    created_at   DATETIME2     NOT NULL DEFAULT GETDATE(),
    updated_at   DATETIME2     NOT NULL DEFAULT GETDATE(),
    FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
    FOREIGN KEY (assignee_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_status ON tasks(status);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);

INSERT INTO roles (name) VALUES ('USER'), ('MANAGER');

-- BCrypt hash của "password": $2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi

INSERT INTO users (username, full_name, email, password_hash, status) VALUES
('manager1', N'Manager One', 'manager1@example.com', '$2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi', 'ACTIVE'),
('user1', N'User One', 'user1@example.com', '$2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi', 'ACTIVE'),
('user2', N'User Two', 'user2@example.com', '$2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi', 'ACTIVE'),
('user3', N'User Three', 'user3@example.com', '$2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi', 'ACTIVE'),
('user4', N'User Four', 'user4@example.com', '$2a$10$MBTYBzZyW9YrFAYK0AenfeAZ2zYmFE9WuZoTg/iGpFa64HSspjzNi', 'ACTIVE');

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u CROSS JOIN roles r
WHERE (u.username = 'manager1' AND r.name = 'MANAGER')
   OR (u.username IN ('user1','user2','user3','user4') AND r.name = 'USER');

INSERT INTO projects (name, code, description, start_date, status, created_by) VALUES
(N'Intern Training Project', 'PRJ001', N'Project đào tạo intern', '2026-03-01', 'ACTIVE', 1),
(N'Client Demo Project', 'PRJ002', N'Project demo cho khách hàng', '2026-03-10', 'ACTIVE', 1);

INSERT INTO project_members (project_id, user_id, role_in_project) VALUES
(1, 1, 'MANAGER'), (1, 2, 'DEV'), (1, 3, 'DEV'),
(2, 1, 'MANAGER'), (2, 3, 'DEV'), (2, 4, 'DEV'), (2, 5, 'QA');

INSERT INTO tasks (project_id, assignee_id, title, description, status, priority, deadline) VALUES
(1, 2, N'Đọc yêu cầu domain', N'Đọc tài liệu dự án và hiểu nghiệp vụ', 'TODO', 'HIGH', '2026-03-20 17:00:00'),
(1, 2, N'Xác định entity', N'Liệt kê các entity chính', 'IN_PROGRESS', 'MEDIUM', '2026-03-22 17:00:00'),
(1, 3, N'Vẽ ERD', N'Vẽ ERD chi tiết User/Task/Project', 'TODO', 'HIGH', '2026-03-23 17:00:00'),
(1, 3, N'Viết class User', N'Tạo class User với validate', 'TODO', 'MEDIUM', '2026-03-24 17:00:00'),
(1, 2, N'Viết class Task', N'Tạo class Task với status flow', 'TODO', 'MEDIUM', '2026-03-25 17:00:00'),
(1, 3, N'Viết class Project', N'Tạo class Project quản lý task', 'TODO', 'LOW', '2026-03-26 17:00:00'),
(1, 2, N'Refactor OOP', N'Tách responsibility cho các class', 'TODO', 'LOW', '2026-03-27 17:00:00'),
(1, 3, N'Viết logic CRUD Task', N'Thêm/sửa/xóa task', 'TODO', 'HIGH', '2026-03-28 17:00:00'),
(1, 2, N'Gán Task cho User', N'Implement logic assign task', 'TODO', 'MEDIUM', '2026-03-29 17:00:00'),
(1, 3, N'Test console', N'Test logic trên console', 'TODO', 'LOW', '2026-03-30 17:00:00'),
(1, 2, N'Thêm validate input', N'Check null/trùng', 'TODO', 'MEDIUM', '2026-03-31 17:00:00'),
(1, 3, N'Ghi log', N'Log kết quả test', 'TODO', 'LOW', '2026-04-01 17:00:00'),
(1, 2, N'Chuẩn hóa entity', N'Mapping entity -> bảng', 'TODO', 'MEDIUM', '2026-04-02 17:00:00'),
(1, 3, N'Viết SQL tạo bảng', N'Tạo script SQL cho DB', 'TODO', 'HIGH', '2026-04-03 17:00:00'),
(1, 2, N'Insert data test', N'Seed >=30 bản ghi mẫu', 'TODO', 'MEDIUM', '2026-04-04 17:00:00'),
(2, 3, N'Phân tích yêu cầu demo', N'Làm rõ yêu cầu khách hàng', 'IN_PROGRESS', 'HIGH', '2026-03-21 17:00:00'),
(2, 4, N'Thiết kế API', N'Thiết kế các endpoint chính', 'TODO', 'HIGH', '2026-03-22 17:00:00'),
(2, 5, N'Viết test plan', N'Chuẩn bị test case cho demo', 'TODO', 'MEDIUM', '2026-03-24 17:00:00'),
(2, 3, N'Implement User API', N'CRUD user + auth', 'TODO', 'HIGH', '2026-03-26 17:00:00'),
(2, 4, N'Implement Project API', N'CRUD project', 'TODO', 'MEDIUM', '2026-03-27 17:00:00'),
(2, 3, N'Implement Task API', N'CRUD + assign + status', 'TODO', 'HIGH', '2026-03-28 17:00:00'),
(2, 4, N'Tích hợp JWT', N'Bảo vệ endpoint bằng JWT', 'TODO', 'HIGH', '2026-03-29 17:00:00'),
(2, 5, N'Viết unit test', N'Test TaskService', 'TODO', 'MEDIUM', '2026-03-30 17:00:00'),
(2, 5, N'Run & fix bug', N'Chạy test và fix lỗi', 'TODO', 'MEDIUM', '2026-03-31 17:00:00'),
(2, 3, N'Tích hợp Swagger', N'Thêm Swagger UI', 'TODO', 'LOW', '2026-04-01 17:00:00'),
(2, 4, N'Viết README', N'Hướng dẫn setup & run', 'TODO', 'LOW', '2026-04-02 17:00:00'),
(2, 3, N'Chuẩn bị slide', N'Slide kiến trúc & flow JWT', 'TODO', 'MEDIUM', '2026-04-03 17:00:00'),
(2, 4, N'Rehearsal demo', N'Chạy thử trước khi demo', 'TODO', 'MEDIUM', '2026-04-04 17:00:00'),
(2, 5, N'Demo chính thức', N'Demo cho khách + Q&A', 'TODO', 'HIGH', '2026-04-05 17:00:00'),
(2, 3, N'Tổng kết & báo cáo', N'Viết report những gì đã học được', 'TODO', 'LOW', '2026-04-06 17:00:00');
