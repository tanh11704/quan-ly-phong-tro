# Hướng dẫn cấu hình Sentry

## 1. Cài đặt và cấu hình

### Bước 1: Tạo tài khoản Sentry

1. Truy cập https://sentry.io và tạo tài khoản
2. Tạo một project mới (chọn React)
3. Lấy DSN từ project settings

### Bước 2: Cấu hình biến môi trường

Thêm các biến sau vào file `.env`:

```env
VITE_SENTRY_DSN=https://your-sentry-dsn@sentry.io/project-id
VITE_SENTRY_ENVIRONMENT=production
```

### Bước 3: Backend API

Bạn cần tạo các API endpoints sau để frontend có thể lấy logs từ Sentry:

#### GET `/api/sentry/issues`

Lấy danh sách issues từ Sentry

**Query Parameters:**

- `page`: số trang (default: 1)
- `pageSize`: số items mỗi trang (default: 20)
- `status`: trạng thái (unresolved, resolved, ignored, muted)
- `level`: mức độ (error, warning, info, debug, fatal)
- `query`: từ khóa tìm kiếm

**Response:**

```json
{
  "issues": [
    {
      "id": "string",
      "shortId": "string",
      "title": "string",
      "culprit": "string",
      "level": "error",
      "status": "unresolved",
      "count": 100,
      "userCount": 50,
      "firstSeen": "2024-01-01T00:00:00Z",
      "lastSeen": "2024-01-02T00:00:00Z",
      "permalink": "https://sentry.io/...",
      "metadata": {
        "type": "Error",
        "value": "Error message",
        "filename": "app.tsx",
        "function": "handleClick"
      }
    }
  ],
  "total": 100,
  "page": 1,
  "pageSize": 20
}
```

#### GET `/api/sentry/issues/:issueId`

Lấy chi tiết một issue

**Response:**

```json
{
  "id": "string",
  "shortId": "string",
  "title": "string"
  // ... các fields tương tự như trên
}
```

#### GET `/api/sentry/issues/:issueId/events`

Lấy danh sách events của một issue

**Query Parameters:**

- `limit`: số lượng events (default: 10)

**Response:**

```json
[
  {
    "event": {
      "id": "string",
      "issue": "string",
      "message": "string",
      "level": "error",
      "platform": "javascript",
      "timestamp": "2024-01-01T00:00:00Z",
      "tags": {},
      "user": {
        "id": "string",
        "username": "string",
        "email": "string"
      },
      "contexts": {
        "browser": {
          "name": "Chrome",
          "version": "120.0"
        },
        "os": {
          "name": "Windows",
          "version": "10"
        }
      },
      "exception": {
        "values": [
          {
            "type": "Error",
            "value": "Error message",
            "stacktrace": {
              "frames": []
            }
          }
        ]
      }
    }
  }
]
```

## 2. Sử dụng

### Truy cập giao diện

Sau khi đăng nhập với role `ADMIN`, bạn sẽ thấy menu "Sentry Logs" trong sidebar.

### Tính năng

- ✅ Xem danh sách logs với filter và search
- ✅ Xem chi tiết từng log issue
- ✅ Xem các events gần đây của mỗi issue
- ✅ Thống kê số lượng errors, warnings
- ✅ Phân trang
- ✅ Giao diện đẹp, responsive

## 3. Lưu ý

- Sentry SDK đã được cấu hình tự động trong `src/lib/sentry.ts`
- Các lỗi sẽ được tự động gửi lên Sentry khi có exception
- Backend cần tích hợp với Sentry API để lấy dữ liệu logs
- Chỉ user có role `ADMIN` mới có quyền xem Sentry Logs
