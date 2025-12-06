# Requirements - Mini App Quản lý Phòng Trọ

## 1. Tổng quan

**Mục tiêu**: Giúp chủ nhà trọ quản lý phòng, khách thuê, hóa đơn điện nước và nhắc đóng tiền tự động.

**Người dùng**: Chủ nhà trọ (10-30 phòng)

**Platform**: Zalo Mini App

## 2. Tech Stack

- **Backend**: Spring Boot 3.x, PostgreSQL
- **Frontend**: React 18, Zalo Mini App SDK
- **Auth**: JWT
- **Deployment**: Docker + VPS

## 3. Tính năng MVP

### 3.1 Quản lý Tòa nhà & Phòng

- CRUD thông tin tòa nhà (tên, chủ nhà, SĐT)
- CRUD phòng (số phòng, giá thuê, trạng thái: VACANT/OCCUPIED/MAINTENANCE)
- Hiển thị danh sách phòng theo trạng thái

### 3.2 Quản lý Khách thuê

- Thêm khách thuê (tên, SĐT, phòng, ngày bắt đầu/kết thúc)
- Xem lịch sử khách thuê của phòng
- Chuyển phòng/kết thúc hợp đồng

### 3.3 Ghi chỉ số Điện/Nước

- Nhập chỉ số điện/nước theo kỳ (tháng)
- Lưu chỉ số cũ + mới, tự động tính tiêu thụ
- Hệ số giá điện/nước có thể cấu hình

### 3.4 Hóa đơn & Thanh toán

- Tự động tạo hóa đơn theo kỳ (tiền phòng + điện + nước)
- Đánh dấu đã thanh toán + ghi ngày thanh toán
- Lịch sử thanh toán của từng khách

### 3.5 Nhắc nhở Tự động

- Gửi thông báo in-app trước hạn đóng tiền (3 ngày)
- Tích hợp Zalo Notification Service (ZNS) - gói Pro
- Job chạy hàng ngày kiểm tra hóa đơn sắp đến hạn

### 3.6 Báo cáo

- Báo cáo thu/chi theo tháng
- Thống kê phòng trống/đã thuê
- Xuất Excel (gói Pro)

## 4. Database Schema

```sql
-- 1. ENUMS (Định nghĩa các kiểu dữ liệu liệt kê)
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'UNPAID', 'PAID', 'OVERDUE', 'VOID');
CREATE TYPE water_calc_method AS ENUM ('BY_METER', 'PER_CAPITA');

-- 2. TABLES
CREATE TABLE buildings (
  id SERIAL PRIMARY KEY,
  name VARCHAR(200),
  owner_name VARCHAR(200),
  owner_phone VARCHAR(20),
  elec_unit_price INT,            -- Giá điện (VNĐ/kWh)
  water_unit_price INT,           -- Giá nước (VNĐ)
  water_calc_method VARCHAR(20),  -- 'BY_METER' hoặc 'PER_CAPITA'
  created_at TIMESTAMP DEFAULT now()
);

CREATE TABLE rooms (
  id SERIAL PRIMARY KEY,
  building_id INT REFERENCES buildings(id) ON DELETE CASCADE,
  room_no VARCHAR(50),
  price INT,
  status VARCHAR(20) DEFAULT 'VACANT', -- Có thể nâng cấp thành Enum sau
  INDEX idx_building_status (building_id, status)
);

CREATE TABLE tenants (
  id SERIAL PRIMARY KEY,
  room_id INT REFERENCES rooms(id),
  name VARCHAR(200),
  phone VARCHAR(20),
  is_contract_holder BOOLEAN DEFAULT false, -- Người đại diện ký hợp đồng
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMP DEFAULT now(),
  INDEX idx_room (room_id)
);

CREATE TABLE meter_records (
  id SERIAL PRIMARY KEY,
  room_id INT REFERENCES rooms(id),
  type VARCHAR(10),   -- 'ELEC' hoặc 'WATER'
  period VARCHAR(20), -- '2025-12'
  previous_value INT,
  current_value INT,
  created_at TIMESTAMP DEFAULT now(),
  UNIQUE(room_id, type, period)
);

CREATE TABLE invoices (
  id SERIAL PRIMARY KEY,
  room_id INT REFERENCES rooms(id),
  tenant_id INT REFERENCES tenants(id), -- Người đại diện thanh toán
  period VARCHAR(20),     -- '2025-12'
  room_price INT,
  elec_amount INT,
  water_amount INT,
  total_amount INT,       -- Tổng cộng
  status VARCHAR(20),     -- Lưu giá trị của Enum InvoiceStatus
  due_date DATE,          -- Hạn chót
  paid_at TIMESTAMP,      -- Thời điểm thanh toán thực tế
  created_at TIMESTAMP DEFAULT now(),
  INDEX idx_status_due (status, due_date)
);
```

## 5. API Endpoints

### 5.1 Authentication

- `POST /api/auth/login` - Đăng nhập (phone + password)
- `POST /api/auth/refresh` - Refresh token

### 5.2 Buildings & Rooms

- `POST /api/buildings` - Tạo tòa nhà
- `GET /api/buildings/{id}` - Chi tiết tòa nhà
- `GET /api/buildings/{id}/rooms` - Danh sách phòng
- `POST /api/rooms` - Thêm phòng
- `PUT /api/rooms/{id}` - Cập nhật phòng
- `DELETE /api/rooms/{id}` - Xóa phòng

### 5.3 Tenants

- `POST /api/tenants` - Thêm khách thuê
- `GET /api/tenants/{id}` - Thông tin khách
- `GET /api/rooms/{roomId}/tenants` - Lịch sử khách của phòng
- `PUT /api/tenants/{id}/end` - Kết thúc hợp đồng

### 5.4 Meter Records

- `POST /api/meters` - Ghi chỉ số
- `GET /api/rooms/{roomId}/meters?period=2025-12` - Xem chỉ số

### 5.5 Invoices

- `POST /api/invoices/generate?period=2025-12` - Tạo hóa đơn hàng loạt
- `GET /api/invoices?period=2025-12&paid=false` - Danh sách hóa đơn
- `GET /api/tenants/{id}/invoices` - Hóa đơn của khách
- `PUT /api/invoices/{id}/pay` - Đánh dấu đã thanh toán
- `DELETE /api/invoices/{id}` - Xóa hóa đơn (chưa thanh toán)

### 5.6 Reports & Notifications

- `GET /api/reports/monthly?period=2025-12` - Báo cáo tháng
- `GET /api/reports/summary` - Tổng quan (số phòng, doanh thu)
- `POST /api/notify/invoice-due` - Gửi nhắc nhở (manual trigger)

## 6. Business Rules

### 6.1 Tạo hóa đơn

- Chỉ tạo cho phòng có khách thuê đang hoạt động
- Tính: Tiền phòng + (Điện × đơn giá) + (Nước × đơn giá)
- Due date: ngày 5 của tháng tiếp theo

### 6.2 Nhắc nhở

- Quét hóa đơn chưa thanh toán, due date trong 3 ngày tới
- Gửi thông báo in-app hoặc ZNS (nếu có token)
- Chạy job lúc 8h sáng hàng ngày

### 6.3 Multi-tenant

- Mỗi owner quản lý nhiều buildings
- Filter data theo owner_id (từ JWT)

## 7. UI Screens (React)

### 7.1 Home (Dashboard)

- Tổng số phòng (trống/đã thuê)
- Doanh thu tháng này
- Hóa đơn chưa thanh toán
- Quick actions: Ghi chỉ số, Tạo hóa đơn

### 7.2 Danh sách Phòng

- Grid view: số phòng, giá, trạng thái
- Filter theo trạng thái
- Action: Sửa, Xem chi tiết

### 7.3 Chi tiết Phòng

- Thông tin phòng
- Khách thuê hiện tại
- Lịch sử chỉ số điện/nước
- Lịch sử hóa đơn

### 7.4 Quản lý Khách thuê

- Danh sách khách đang thuê
- Form thêm/sửa khách

### 7.5 Ghi chỉ số

- Chọn tháng
- Danh sách phòng với input điện/nước
- Submit hàng loạt

### 7.6 Hóa đơn

- Danh sách hóa đơn (filter: tháng, trạng thái)
- Chi tiết hóa đơn
- Button: Đánh dấu đã thanh toán

### 7.7 Báo cáo

- Biểu đồ doanh thu theo tháng
- Xuất Excel (gói Pro)

## 8. Non-Functional Requirements

### 8.1 Performance

- Response time < 500ms cho API thường
- Hỗ trợ đến 100 phòng/building
- Job notification chạy dưới 5 phút

### 8.2 Security

- JWT với expiry 7 ngày (refresh 30 ngày)
- HTTPS only
- Input validation (phone, price, dates)

### 8.3 Scalability

- 1 VPS 1GB đủ cho 5-10 chủ nhà
- PostgreSQL connection pool: 10 connections

## 9. Lộ trình 2 tuần

| Ngày   | Task                                                 |
| ------ | ---------------------------------------------------- |
| D1-3   | Setup Spring Boot + Auth + Building/Room/Tenant CRUD |
| D4-7   | Meter records + Invoice generation logic + API       |
| D8-10  | Notification job (cron) + in-app notify stub         |
| D11-13 | React UI (6 screens) + Zalo Mini App integration     |
| D14    | Deploy Docker + Demo với 1 khách thử                 |

## 10. Pricing

- **Phí cài đặt**: 150k (1 lần)
- **Gói Basic**: 49k/tháng (in-app notify, quản lý cơ bản)
- **Gói Pro**: 149k/tháng (ZNS, xuất Excel)

## 11. Future Enhancements (Post-MVP)

- Tự động gửi hóa đơn PDF qua Zalo
- Tích hợp thu hộ (payment gateway)
- Báo cáo thuế chi tiết
- Multi-building support UI
