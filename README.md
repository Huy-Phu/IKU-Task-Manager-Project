# 🚀 IKU Task Manager

A backend system for managing tasks using the **User - Project - Task** model with JWT authentication and role-based authorization.

---

## 📌 Overview
- Users have roles: `USER`, `MANAGER`
- Projects contain multiple tasks
- Tasks belong to a project and can be assigned to users
- Task workflow:
  - `TODO → IN_PROGRESS → DONE`
  - `TODO → DONE`
  - Cannot update when status = `DONE`

---

## 🧱 Tech Stack
- Java Spring Boot
- Spring Security + JWT
- SQL Server
- JPA / Hibernate
- Mockito (Unit Test)

---

## 🔐 Authentication & Authorization
- JWT-based authentication
- Roles:
  - `ROLE_USER`
  - `ROLE_MANAGER`

---

## 📡 API Features

### 🔑 Auth
- Register / Login
- Return JWT token

### 👤 User
- Get all users (MANAGER)
- Update / Soft delete user

### 📁 Project
- Create project (MANAGER)
- View projects (by role)

### ✅ Task
- Create task (default: TODO)
- Assign task to user
- Update task status
- Get tasks:
  - by user
  - by project
  - by status

---

## 🔁 Business Rules

### Task Rules
- Cannot update if status = `DONE`
- Valid transitions:
  - TODO → IN_PROGRESS
  - TODO → DONE
  - IN_PROGRESS → DONE

### Create Task
- `projectId` required
- `title` ≤ 200 chars
- `deadline > now`
- No `assigneeId` when creating

### Assign Task
- Task must not be DONE
- User must belong to project

---

## ⚠️ Validation & Error Handling
- Standard HTTP codes: 400, 404, 500
- Unified response:
```json
{
  "code": 200,
  "message": "Success",
  "data": {}
}
