-- 1. ENUMS (Định nghĩa các kiểu dữ liệu liệt kê)
-- Giúp đảm bảo dữ liệu nhất quán, tránh nhập sai chính tả
CREATE TYPE invoice_status AS ENUM ('DRAFT', 'UNPAID', 'PAID', 'OVERDUE', 'VOID');
CREATE TYPE water_calc_method AS ENUM ('BY_METER', 'PER_CAPITA');

-- 2. TABLES (Tạo bảng dữ liệu)

-- Bảng Tòa nhà
CREATE TABLE buildings (
    id SERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    owner_name VARCHAR(200),
    owner_phone VARCHAR(20),
    -- Cấu hình đơn giá điện nước
    elec_unit_price INT DEFAULT 3500,
    water_unit_price INT DEFAULT 20000,
    water_calc_method VARCHAR(50) DEFAULT 'BY_METER', -- Lưu ý: Flyway có thể cần cast kiểu dữ liệu này cẩn thận
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng Phòng
CREATE TABLE rooms (
    id SERIAL PRIMARY KEY,
    building_id INT NOT NULL,
    room_no VARCHAR(50) NOT NULL,
    price INT NOT NULL, -- Giá thuê cơ bản
    status VARCHAR(20) DEFAULT 'VACANT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_building FOREIGN KEY (building_id) REFERENCES buildings(id) ON DELETE CASCADE
);
-- Tạo chỉ mục để tìm kiếm nhanh hơn
CREATE INDEX idx_rooms_building_status ON rooms(building_id, status);

-- Bảng Khách thuê
CREATE TABLE tenants (
    id SERIAL PRIMARY KEY,
    room_id INT,
    name VARCHAR(200) NOT NULL,
    phone VARCHAR(20),
    is_contract_holder BOOLEAN DEFAULT false, -- Người đại diện ký hợp đồng
    start_date DATE DEFAULT CURRENT_DATE,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_tenant FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE SET NULL
);

-- Bảng Chỉ số Điện/Nước (Meter Records)
CREATE TABLE meter_records (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    type VARCHAR(10) NOT NULL,   -- 'ELEC' hoặc 'WATER'
    period VARCHAR(20) NOT NULL, -- Format: 'YYYY-MM' (Ví dụ: '2025-12')
    previous_value INT DEFAULT 0,
    current_value INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_meter FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE(room_id, type, period) -- Đảm bảo 1 phòng không có 2 bản ghi điện trong cùng 1 tháng
);

-- Bảng Hóa đơn (Invoices)
CREATE TABLE invoices (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    tenant_id INT, -- Người đại diện thanh toán (có thể null nếu khách đã rời đi)
    period VARCHAR(20) NOT NULL, -- '2025-12'
    
    -- Các trường tiền thành phần
    room_price INT DEFAULT 0,
    elec_amount INT DEFAULT 0,
    water_amount INT DEFAULT 0,
    total_amount INT DEFAULT 0,
    
    -- Trạng thái vòng đời hóa đơn
    status VARCHAR(50) DEFAULT 'DRAFT', -- Sẽ map với Enum invoice_status
    
    due_date DATE,          -- Hạn chót thanh toán
    paid_at TIMESTAMP,      -- Thời điểm thanh toán thực tế
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_room_invoice FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_tenant_invoice FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE SET NULL
);
CREATE INDEX idx_invoices_status_due ON invoices(status, due_date);