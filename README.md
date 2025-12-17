# 🏠 Ứng dụng Quản lý Phòng Trọ - Room Management System

## 📋 Mục lục

- [Tổng quan](#tổng-quan)
- [Tính năng chính](#tính-năng-chính)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Tech Stack](#tech-stack)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)
- [Cài đặt và chạy](#cài-đặt-và-chạy)
- [Hướng dẫn sử dụng](#hướng-dẫn-sử-dụng)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Quy trình phát triển](#quy-trình-phát-triển)

---

## 🎯 Tổng quan

**Room Management System** là ứng dụng Zalo Mini App được thiết kế để giúp chủ nhà trọ quản lý các phòng cho thuê, khách thuê, hóa đơn điện/nước một cách hiệu quả. Ứng dụng tự động nhắc nhở thanh toán và cung cấp các báo cáo chi tiết về tình hình kinh tế.

**Đối tượng sử dụng:** Chủ nhà trọ (quản lý từ 10-30 phòng)

**Platform chính:** Zalo Mini App (với giao diện quản lý riêng cho admin)

---

## ✨ Tính năng chính

### 1. 🏢 Quản lý Tòa nhà & Phòng

- **CRUD Tòa nhà:** Thêm, sửa, xóa thông tin tòa nhà (tên, chủ nhà, số điện thoại)
- **CRUD Phòng:** Quản lý các phòng (số phòng, giá thuê, trạng thái)
- **Trạng thái phòng:** VACANT (trống), OCCUPIED (đã thuê), MAINTENANCE (bảo trì)
- **Hiển thị danh sách:** Lọc và xem phòng theo trạng thái

### 2. 👥 Quản lý Khách thuê

- **Thêm khách thuê:** Ghi nhận thông tin khách (tên, SĐT, phòng, ngày bắt đầu/kết thúc)
- **Lịch sử khách:** Xem tất cả khách thuê từng phòng qua các thời kỳ
- **Chuyển phòng:** Hỗ trợ chuyển khách sang phòng khác
- **Kết thúc hợp đồng:** Ghi nhận ngày khách trả phòng

### 3. ⚡ Ghi chỉ số Điện/Nước

- **Nhập chỉ số định kỳ:** Cập nhật chỉ số điện/nước theo tháng
- **Tính tự động:** Hệ thống tự động tính lượng tiêu thụ (chỉ số mới - chỉ số cũ)
- **Cấu hình hệ số giá:** Thay đổi giá điện (VNĐ/kWh) và giá nước (VNĐ)
- **Phương pháp tính nước:** Hỗ trợ tính theo đồng hồ hoặc theo đầu người

### 4. 💰 Hóa đơn & Thanh toán

- **Tạo hóa đơn tự động:** Hệ thống tự động sinh hóa đơn mỗi kỳ với:
  - Tiền thuê phòng cơ bản
  - Chi phí điện
  - Chi phí nước
- **Theo dõi thanh toán:** Đánh dấu trạng thái (DRAFT, UNPAID, PAID, OVERDUE, VOID)
- **Ghi ngày thanh toán:** Lưu lại thời điểm khách thanh toán thực tế
- **Lịch sử giao dịch:** Xem tất cả hóa đơn quá khứ của khách

### 5. 🔔 Nhắc nhở Tự động

- **Thông báo in-app:** Gửi nhắc nhở khách trước hạn đóng tiền (3 ngày)
- **Job tự động:** Chạy hàng ngày kiểm tra hóa đơn sắp đến hạn
- **Zalo Notification Service (ZNS):** Tích hợp gửi thông báo qua Zalo (gói Pro)
- **Cấu hình linh hoạt:** Có thể tùy chỉnh khoảng thời gian nhắc nhở

### 6. 📊 Báo cáo & Thống kê

- **Báo cáo tài chính:** Thu/chi theo tháng, năm
- **Thống kê phòng:** Số phòng trống, đã thuê, bảo trì
- **Doanh thu:** Tổng doanh thu từ tiền phòng, điện, nước
- **Xuất báo cáo:** Hỗ trợ export Excel (gói Pro)

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (Zalo Mini App)                  │
│            - React 18, React Router, Redux Toolkit           │
├─────────────────────────────────────────────────────────────┤
│              Frontend Admin (Web Dashboard)                   │
│            - React 19, Ant Design, React Query               │
├─────────────────────────────────────────────────────────────┤
│                    API Gateway / Backend                      │
│        Spring Boot 4.0 + Spring Security (JWT Auth)          │
├─────────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │ PostgreSQL   │  │    Redis     │  │  File Storage   │   │
│  │   Database   │  │   (Caching)  │  │  (Email Assets) │   │
│  └──────────────┘  └──────────────┘  └─────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend

- **Framework:** Spring Boot 4.0.0
- **Java Version:** 17
- **Database:** PostgreSQL 14
- **Cache:** Redis (Alpine)
- **ORM:** Spring Data JPA + Hibernate
- **Security:** Spring Security + JWT (Nimbus JOSE)
- **Email:** Spring Mail
- **API Documentation:** Springdoc OpenAPI (Swagger)
- **Migration:** Flyway
- **Code Quality:** Checkstyle, Spotless, JaCoCo (80% coverage)
- **Build Tool:** Gradle

### Frontend - Admin Dashboard

- **Framework:** React 19
- **UI Library:** Ant Design 6.1
- **HTTP Client:** Axios
- **State Management:** React Query (@tanstack/react-query)
- **Routing:** React Router 7
- **Styling:** Tailwind CSS + CSS Modules
- **Build Tool:** Vite 7
- **Package Manager:** npm

### Frontend - Zalo Mini App

- **Framework:** React 18
- **UI Library:** Zalo Mini App UI + Zmp-UI
- **State Management:** React Query
- **HTTP Client:** Axios
- **Routing:** React Router 7
- **Styling:** SCSS + Tailwind CSS
- **Build Tool:** Vite
- **Package Manager:** npm

### DevOps

- **Containerization:** Docker + Docker Compose
- **CI/CD:** (Chuẩn bị để tích hợp)
- **Deployment:** VPS (Linux)

---

## 📁 Cấu trúc thư mục

```
room-management/
│
├── backend/                          # Spring Boot API
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/tpanh/backend/
│   │   │   │   ├── controller/       # REST Controllers
│   │   │   │   │   ├── AuthenticationController.java
│   │   │   │   │   ├── BuildingController.java
│   │   │   │   │   ├── RoomController.java
│   │   │   │   │   ├── TenantController.java
│   │   │   │   │   ├── InvoiceController.java
│   │   │   │   │   ├── UserController.java
│   │   │   │   │   └── AdminController.java
│   │   │   │   ├── service/         # Business Logic
│   │   │   │   │   ├── AuthenticationService.java
│   │   │   │   │   ├── JwtService.java
│   │   │   │   │   ├── RegistrationService.java
│   │   │   │   │   ├── EmailService.java
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   ├── BuildingService.java
│   │   │   │   │   ├── RoomService.java
│   │   │   │   │   ├── TenantService.java
│   │   │   │   │   └── InvoiceService.java
│   │   │   │   ├── repository/      # Data Access Layer
│   │   │   │   ├── entity/          # JPA Entities
│   │   │   │   ├── dto/             # Data Transfer Objects
│   │   │   │   ├── mapper/          # MapStruct Mappers
│   │   │   │   ├── config/          # Spring Configurations
│   │   │   │   ├── exception/       # Custom Exceptions
│   │   │   │   ├── security/        # Security Components
│   │   │   │   ├── enums/           # Enumerations
│   │   │   │   └── BackendApplication.java
│   │   │   ├── resources/
│   │   │   │   ├── application.yml  # Main config
│   │   │   │   ├── application-local.yml
│   │   │   │   ├── db/migration/    # Flyway migrations
│   │   │   │   ├── templates/       # Email templates
│   │   │   │   └── static/          # Static files
│   │   │   └── test/                # Unit & Integration Tests
│   ├── build.gradle                 # Gradle build file
│   ├── gradle/                      # Gradle wrapper
│   └── config/                      # Code quality configs

├── frontend-admin/                   # Admin Dashboard (React + Ant Design)
│   ├── src/
│   │   ├── components/              # Reusable UI components
│   │   ├── features/                # Feature modules
│   │   │   ├── auth/                # Authentication
│   │   │   └── buildings/           # Building management
│   │   ├── pages/                   # Page components
│   │   ├── hooks/                   # Custom React hooks
│   │   ├── layouts/                 # Layout components
│   │   ├── lib/                     # Utilities
│   │   │   ├── axios.ts             # API client
│   │   │   └── queryClient.ts       # React Query setup
│   │   ├── config/                  # Configuration
│   │   └── App.tsx
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   └── eslint.config.js

├── frontend-zalo-app/                # Zalo Mini App (React)
│   ├── src/
│   │   ├── components/              # UI components
│   │   ├── pages/                   # App pages
│   │   ├── lib/                     # Utilities
│   │   │   ├── axios.ts             # API client
│   │   │   └── axiosBaseQuery.ts
│   │   ├── css/                     # Global styles
│   │   └── app.ts
│   ├── package.json
│   ├── vite.config.mts
│   ├── zmp-cli.json                 # Zalo Mini App config
│   ├── tailwind.config.js
│   └── tsconfig.json

├── docker-compose.yml               # Docker services (PostgreSQL + Redis)
├── requirement.md                   # Business requirements
└── README.md                        # This file
```

---

## 🚀 Cài đặt và chạy

### 📋 Yêu cầu hệ thống

- **Java 17** trở lên
- **Node.js 18+** và npm
- **PostgreSQL 14+**
- **Redis 7+** (hoặc dùng Docker)
- **Docker & Docker Compose** (tùy chọn)
- **Gradle** (hoặc dùng gradlew)

### 🔧 Hướng dẫn cài đặt

#### 1. Clone Repository

```bash
git clone <repository-url>
cd room-management
```

#### 2. Khởi động Database và Cache (với Docker)

```bash
docker-compose up -d
```

Điều này sẽ khởi động:

- PostgreSQL trên port **5432** (username: postgres, password: 123456, database: phongtro)
- Redis trên port **6379**

Nếu không dùng Docker, hãy cài đặt PostgreSQL và Redis thủ công, sau đó cập nhật `application.yml`.

#### 3. Cài đặt và chạy Backend

```bash
cd backend

# Nếu dùng Linux/Mac
./gradlew bootRun

# Hoặc dùng Gradle đã cài đặt
gradle bootRun

# Hoặc build JAR
./gradlew build
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar
```

Backend sẽ chạy trên http://localhost:8080

**API Documentation:** http://localhost:8080/swagger-ui.html

#### 4. Cài đặt và chạy Frontend Admin

```bash
cd frontend-admin

# Cài đặt dependencies
npm install

# Chạy dev server
npm run dev
```

Ứng dụng sẽ chạy trên http://localhost:5173

#### 5. Cài đặt và chạy Frontend Zalo Mini App

```bash
cd frontend-zalo-app

# Cài đặt dependencies
npm install

# Đăng nhập vào Zalo
npm run login

# Chạy dev server
npm run start

# Deploy (khi sẵn sàng)
npm run deploy
```

---

## 📖 Hướng dẫn sử dụng

### Tài khoản mặc định

Backend được cấu hình với tài khoản admin mặc định:

- **Username:** admin
- **Password:** admin123

### Quy trình sử dụng chính

#### 1. Đăng ký/Đăng nhập

- Nhập username và password
- Hệ thống trả về JWT token
- Token được lưu trong localStorage (admin) hoặc sessionStorage (Zalo app)

#### 2. Tạo Tòa nhà

- Tại giao diện admin, tạo tòa nhà mới
- Nhập thông tin: tên, chủ nhà, SĐT, giá điện, giá nước

#### 3. Tạo Phòng

- Thêm phòng vào tòa nhà
- Nhập: số phòng, giá thuê, trạng thái ban đầu

#### 4. Quản lý Khách thuê

- Thêm khách thuê cho phòng
- Xác định người đại diện ký hợp đồng
- Ghi nhận ngày bắt đầu/kết thúc

#### 5. Ghi chỉ số

- Mỗi tháng, nhập chỉ số điện/nước
- Hệ thống tự động tính tiêu thụ và chi phí

#### 6. Tạo hóa đơn

- Hệ thống tự động sinh hóa đơn mỗi kỳ
- Hoặc tạo thủ công nếu cần

#### 7. Theo dõi thanh toán

- Đánh dấu hóa đơn đã thanh toán
- Ghi ngày thanh toán thực tế
- Nhận thông báo nhắc nhở trước hạn

---

## 🔌 API Documentation

### Phân loại Endpoint

#### 🔐 Authentication (`/api/v1/auth`)

- `POST /register` - Đăng ký tài khoản mới
- `POST /login` - Đăng nhập
- `POST /refresh-token` - Làm mới token
- `POST /logout` - Đăng xuất

#### 🏢 Buildings (`/api/v1/buildings`)

- `GET /` - Lấy danh sách tòa nhà
- `GET /{id}` - Lấy chi tiết tòa nhà
- `POST /` - Tạo tòa nhà mới
- `PUT /{id}` - Cập nhật tòa nhà
- `DELETE /{id}` - Xóa tòa nhà

#### 🛏️ Rooms (`/api/v1/rooms`)

- `GET /building/{buildingId}` - Lấy phòng của tòa nhà
- `GET /{id}` - Lấy chi tiết phòng
- `POST /` - Tạo phòng mới
- `PUT /{id}` - Cập nhật phòng
- `DELETE /{id}` - Xóa phòng
- `PATCH /{id}/status` - Cập nhật trạng thái phòng

#### 👥 Tenants (`/api/v1/tenants`)

- `GET /room/{roomId}` - Lấy danh sách khách của phòng
- `GET /{id}` - Lấy chi tiết khách
- `POST /` - Thêm khách mới
- `PUT /{id}` - Cập nhật thông tin khách
- `DELETE /{id}` - Xóa khách

#### 💰 Invoices (`/api/v1/invoices`)

- `GET /` - Lấy danh sách hóa đơn
- `GET /room/{roomId}` - Lấy hóa đơn của phòng
- `GET /{id}` - Lấy chi tiết hóa đơn
- `POST /` - Tạo hóa đơn mới
- `PUT /{id}` - Cập nhật hóa đơn
- `PATCH /{id}/status` - Cập nhật trạng thái thanh toán

#### 👤 Users (`/api/v1/users`)

- `GET /profile` - Lấy thông tin user hiện tại
- `PUT /profile` - Cập nhật thông tin cá nhân
- `PUT /password` - Đổi mật khẩu

#### 🛠️ Admin (`/api/v1/admin`)

- `GET /dashboard` - Lấy thống kê tổng quát
- `GET /reports/monthly` - Báo cáo hàng tháng
- `GET /reports/revenue` - Thống kê doanh thu

### Ví dụ Request/Response

#### Đăng nhập

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

**Response:**

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "...",
  "expiresIn": 3600
}
```

#### Lấy danh sách tòa nhà

```bash
curl -X GET http://localhost:8080/api/v1/buildings \
  -H "Authorization: Bearer {accessToken}"
```

**Response:**

```json
{
  "data": [
    {
      "id": 1,
      "name": "Nhà A",
      "ownerName": "Nguyễn Văn A",
      "ownerPhone": "0901234567",
      "elecUnitPrice": 3500,
      "waterUnitPrice": 5000,
      "waterCalcMethod": "BY_METER"
    }
  ],
  "pagination": {
    "total": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

---

## 🗄️ Database Schema

### Entities chính

#### Buildings (Tòa nhà)

```sql
CREATE TABLE buildings (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200),
  owner_name VARCHAR(200),
  owner_phone VARCHAR(20),
  elec_unit_price INT,
  water_unit_price INT,
  water_calc_method VARCHAR(20),
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);
```

#### Rooms (Phòng)

```sql
CREATE TABLE rooms (
  id BIGSERIAL PRIMARY KEY,
  building_id BIGINT REFERENCES buildings(id),
  room_no VARCHAR(50),
  price INT,
  status VARCHAR(20) DEFAULT 'VACANT',
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now(),
  INDEX idx_building_status (building_id, status)
);
```

#### Tenants (Khách thuê)

```sql
CREATE TABLE tenants (
  id BIGSERIAL PRIMARY KEY,
  room_id BIGINT REFERENCES rooms(id),
  name VARCHAR(200),
  phone VARCHAR(20),
  is_contract_holder BOOLEAN DEFAULT false,
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMP DEFAULT now(),
  INDEX idx_room (room_id)
);
```

#### Meter Records (Chỉ số điện/nước)

```sql
CREATE TABLE meter_records (
  id BIGSERIAL PRIMARY KEY,
  room_id BIGINT REFERENCES rooms(id),
  type VARCHAR(10),   -- 'ELEC' hoặc 'WATER'
  period VARCHAR(20), -- '2025-12'
  old_value INT,
  new_value INT,
  consumption INT,
  unit_price INT,
  total_cost INT,
  created_at TIMESTAMP DEFAULT now()
);
```

#### Invoices (Hóa đơn)

```sql
CREATE TABLE invoices (
  id BIGSERIAL PRIMARY KEY,
  room_id BIGINT REFERENCES rooms(id),
  tenant_id BIGINT REFERENCES tenants(id),
  period VARCHAR(20),
  rent_cost INT,
  elec_cost INT,
  water_cost INT,
  total_cost INT,
  status VARCHAR(20) DEFAULT 'UNPAID',  -- DRAFT, UNPAID, PAID, OVERDUE, VOID
  paid_at TIMESTAMP,
  due_date DATE,
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);
```

#### Users (Người dùng)

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE,
  password VARCHAR(255),
  email VARCHAR(255),
  full_name VARCHAR(200),
  phone VARCHAR(20),
  is_verified BOOLEAN DEFAULT false,
  status VARCHAR(20) DEFAULT 'ACTIVE',
  created_at TIMESTAMP DEFAULT now(),
  updated_at TIMESTAMP DEFAULT now()
);
```

### Enums

- **InvoiceStatus:** DRAFT, UNPAID, PAID, OVERDUE, VOID
- **RoomStatus:** VACANT, OCCUPIED, MAINTENANCE
- **WaterCalcMethod:** BY_METER, PER_CAPITA
- **UserStatus:** ACTIVE, INACTIVE, BLOCKED

---

## 👨‍💻 Quy trình phát triển

### Code Quality Standards

#### 1. Checkstyle

- Kiểm tra định dạng code Java
- Config tại: `backend/config/checkstyle/checkstyle.xml`
- Chạy: `./gradlew checkstyleMain`

#### 2. Spotless

- Tự động format code Java
- Chạy: `./gradlew spotlessApply`

#### 3. JaCoCo (Code Coverage)

- Yêu cầu tối thiểu: 80% coverage
- Chạy test: `./gradlew test`
- Tạo báo cáo: `./gradlew jacocoTestReport`
- Xem báo cáo: `build/reports/jacoco/test/html/index.html`

#### 4. ESLint & Prettier (Frontend)

**Frontend Admin:**

```bash
npm run lint          # Kiểm tra lỗi
npm run lint:fix      # Sửa lỗi tự động
npm run format        # Format code
npm run format:check  # Kiểm tra format
```

**Frontend Zalo App:**
Các lệnh tương tự

### Git Workflow

```
main (production)
    ↑
    └── develop (development)
            ↑
            └── feature/feature-name (tính năng mới)
            └── bugfix/bug-name (sửa bug)
            └── hotfix/hotfix-name (sửa nóng)
```

### Commit Convention

```
feat: Thêm tính năng mới
fix: Sửa bug
refactor: Tái cấu trúc code
style: Thay đổi định dạng
test: Thêm/sửa test
docs: Cập nhật tài liệu
chore: Cập nhật dependencies, config
```

### Pull Request Process

1. Tạo nhánh từ `develop`
2. Commit code với message rõ ràng
3. Chạy tất cả test: `./gradlew test`
4. Chạy code quality checks
5. Push lên và tạo Pull Request
6. Code review từ team members
7. Merge vào `develop`
8. Deploy lên staging/production

### Testing

#### Backend

```bash
cd backend

# Chạy tất cả test
./gradlew test

# Chạy test của class cụ thể
./gradlew test --tests "com.tpanh.backend.service.*"

# Chạy với coverage
./gradlew test jacocoTestReport
```

#### Frontend

```bash
# Chạy linter
npm run lint

# Chạy format check
npm run format:check
```

---

## 🔐 Security

### Authentication & Authorization

- **JWT (JSON Web Token):** Dùng Nimbus JOSE JWT
- **Token Expiration:** Configurable (default: 1 giờ)
- **Refresh Token:** Cấp token mới mà không cần đăng nhập lại
- **Password:** Hashed bằng Spring Security (BCrypt)

### CORS Configuration

Backend cho phép:

- Frontend Admin: http://localhost:5173
- Frontend Zalo App: (Cấu hình riêng)

### Sensitive Data

- Mật khẩu: Không bao giờ trả về trong response
- JWT Secret: Phải đổi trong production (dùng environment variables)
- Database credentials: Dùng environment variables

---

## 📊 Monitoring & Logging

### Backend Logs

Logs được cấu hình trong `application.yml`:

```yaml
logging:
  level:
    root: INFO
    com.tpanh.backend: DEBUG
  file: logs/application.log
```

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

---

## 🚢 Deployment

### Development

```bash
docker-compose up -d  # Khởi động local environment
```

### Staging/Production

Sử dụng Docker Image:

```bash
# Build image
docker build -f backend/Dockerfile -t room-management:1.0 .

# Run container
docker run -d \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/phongtro \
  -e SPRING_DATASOURCE_USERNAME=postgres \
  -e SPRING_DATASOURCE_PASSWORD=secure_password \
  -e APP_JWT_SECRET=your-256-bit-secret \
  -p 8080:8080 \
  room-management:1.0
```

---

## 📞 Support & Contribution

### Báo cáo lỗi

Vui lòng tạo issue trên GitHub với:

- Mô tả lỗi chi tiết
- Bước để reproduce
- Expected vs Actual behavior
- Screenshots/logs

### Contributing

1. Fork repository
2. Tạo nhánh feature
3. Commit changes
4. Push và tạo Pull Request

---

## 📄 License

Dự án này được phát triển cho mục đích thương mại. Tất cả quyền được bảo lưu.

---

## 👥 Team

- **Product Owner:** Chủ nhà trọ / Quản lý dự án
- **Backend Developer:** Tpanh & Team
- **Frontend Developer:** Tpanh & Team
- **DevOps/Infrastructure:** Team

---

## 📌 Roadmap

### Phase 1 (Hiện tại - MVP)

- ✅ Quản lý tòa nhà & phòng
- ✅ Quản lý khách thuê
- ✅ Ghi chỉ số điện/nước
- ✅ Tạo hóa đơn & thanh toán
- ✅ Nhắc nhở tự động
- ✅ API Backend hoàn chỉnh
- ⏳ Frontend Admin (70%)
- ⏳ Frontend Zalo App (50%)

### Phase 2 (Gói Pro)

- 📋 Xuất báo cáo Excel
- 🔔 Zalo Notification Service (ZNS)
- 📊 Dashboard nâng cao
- 💳 Tích hợp thanh toán trực tuyến

### Phase 3 (Nâng cao)

- 📱 Mobile App native (iOS/Android)
- 🤖 AI dự báo nhu cầu
- 📈 Phân tích kinh doanh nâng cao
- 🌐 Quản lý multi-site

---

## 📚 Tài liệu tham khảo

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [React Documentation](https://react.dev)
- [PostgreSQL Documentation](https://www.postgresql.org/docs)
- [Zalo Mini App Documentation](https://developers.zalo.me/docs)

---

**Last Updated:** 2025-12-11

**Version:** 0.0.1-SNAPSHOT
