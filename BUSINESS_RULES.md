# Mô tả nghiệp vụ — IKU Task Manager

## 1. Tổng quan hệ thống

Hệ thống quản lý task nội bộ theo mô hình **User – Project – Task**:
- User đăng nhập, nhận JWT token và thao tác với hệ thống
- MANAGER tạo và quản lý Project, phân quyền thành viên
- USER nhận và thực hiện Task trong Project

---

## 2. Entity chính

### User
| Field         | Kiểu dữ liệu | Mô tả                          |
|---------------|-------------|-------------------------------|
| id            | Long        | Khóa chính, tự tăng           |
| username      | String      | Tên đăng nhập, unique          |
| fullName      | String      | Họ tên đầy đủ                 |
| email         | String      | Email, unique                 |
| passwordHash  | String      | Mật khẩu đã BCrypt hash       |
| status        | String      | ACTIVE / INACTIVE             |
| roles         | Set\<Role\> | Một user có thể nhiều role     |
| createdAt     | LocalDateTime | Thời điểm tạo               |
| updatedAt     | LocalDateTime | Thời điểm cập nhật cuối     |

### Role
| Tên      | Mô tả                                    |
|----------|------------------------------------------|
| USER     | Intern/developer – xem và làm task của mình |
| MANAGER  | Quản lý – tạo project, xem tất cả task  |

### Project
| Field       | Kiểu dữ liệu  | Mô tả                   |
|-------------|--------------|------------------------|
| id          | Long         | Khóa chính              |
| name        | String       | Tên project             |
| code        | String       | Mã project, unique      |
| description | String       | Mô tả                   |
| startDate   | LocalDate    | Ngày bắt đầu            |
| endDate     | LocalDate    | Ngày kết thúc           |
| status      | String       | ACTIVE / CLOSED         |
| createdBy   | User         | MANAGER tạo project     |

### Task
| Field       | Kiểu dữ liệu    | Mô tả                        |
|-------------|----------------|------------------------------|
| id          | Long           | Khóa chính                   |
| project     | Project        | Task thuộc project nào        |
| assignee    | User (nullable)| Người được gán task           |
| title       | String         | Tiêu đề task (max 200 ký tự) |
| description | String         | Mô tả chi tiết                |
| status      | TaskStatus     | Trạng thái (xem bên dưới)    |
| priority    | TaskPriority   | LOW / MEDIUM / HIGH           |
| deadline    | LocalDateTime  | Hạn hoàn thành (phải > now)  |

### Enum TaskStatus
```
TODO  →  IN_PROGRESS  →  DONE
TODO  →  DONE  (fast track)
```
> Một khi task đã DONE, **không thể** thay đổi status hay assign lại.

### Enum TaskPriority
```
LOW | MEDIUM | HIGH
```

---

## 3. Quan hệ giữa các Entity

```
User ──(nhiều)── user_roles ──(nhiều)── Role
User ──(nhiều)── project_members ──(nhiều)── Project
Project ──(1)──< Task
User ──(0..1)──< Task (assignee)
```

---

## 4. Business Rules

### 4.1 Đăng ký / Đăng nhập
- Username và email phải unique
- Mật khẩu được mã hóa bằng **BCrypt** trước khi lưu DB
- User mới đăng ký mặc định nhận Role **USER**
- Đăng nhập thành công → server cấp **JWT token** (hết hạn sau 24h)

### 4.2 Project
- Chỉ **MANAGER** mới được tạo project
- Mã project (code) phải unique

### 4.3 Task — Tạo task
- Chỉ **project member** mới được tạo task
- `projectId` phải tồn tại trong DB
- `title` bắt buộc, tối đa 200 ký tự
- `deadline` nếu có phải là ngày **trong tương lai**
- Task mới luôn có status = **TODO**

### 4.4 Task — Assign task
- Người được assign **phải là member** của project chứa task đó
- Không thể assign task đã có status = **DONE**

### 4.5 Task — Cập nhật status
| Từ           | Sang          | Hợp lệ |
|-------------|--------------|--------|
| TODO        | IN_PROGRESS   | ✅     |
| TODO        | DONE          | ✅ (fast track) |
| IN_PROGRESS | DONE          | ✅     |
| DONE        | (bất kỳ)     | ❌ DONE là trạng thái cuối |
| IN_PROGRESS | TODO          | ❌ không cho quay lui |

### 4.6 Phân quyền xem task
| Role    | Xem task theo user | Xem task theo project |
|---------|-------------------|----------------------|
| MANAGER | Xem của tất cả user | Xem tất cả task trong project |
| USER    | Chỉ xem của chính mình | Chỉ xem task được assign cho mình |

### 4.7 Phân quyền xem task theo status
| Role    | Kết quả GET /api/tasks/by-status?status=TODO |
|---------|----------------------------------------------|
| MANAGER | Tất cả task có status=TODO                    |
| USER    | Chỉ các task status=TODO được assign cho mình |

---

## 5. Error Handling

| Tình huống                        | HTTP Code | Exception                  |
|-----------------------------------|-----------|---------------------------|
| Resource không tìm thấy           | 404       | ResourceNotFoundException  |
| Vi phạm business rule             | 400       | BusinessException          |
| Sai username/password             | 401       | BadCredentialsException    |
| Input không hợp lệ (@Valid fail)  | 400       | MethodArgumentNotValidException |
| Lỗi server                        | 500       | Exception (generic)        |

---

## 6. Response Format chuẩn

```json
{
  "code": 200,
  "message": "Success",
  "data": { ... }
}
```

Lỗi:
```json
{
  "code": 400,
  "message": "Only MANAGER can create projects",
  "data": null
}
```
