-- Bảng Users: Hỗ trợ cả Admin (username/password) và Tenant (Zalo login)
CREATE TABLE users (
    id VARCHAR(50) PRIMARY KEY, -- UUID format: a1b2-c3d4...
    username VARCHAR(50) UNIQUE, -- Dùng cho Admin đăng nhập, có thể NULL nếu là user Zalo
    password VARCHAR(255),      -- Hash bằng BCrypt, có thể NULL nếu login bằng Zalo
    zalo_id VARCHAR(50) UNIQUE, -- ID định danh từ Zalo trả về, dùng để map tài khoản
    full_name VARCHAR(100) NOT NULL,
    roles VARCHAR(50) NOT NULL,  -- Lưu dạng chuỗi: "ADMIN" hoặc "TENANT"
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tạo chỉ mục để tìm kiếm nhanh
CREATE INDEX idx_users_username ON users(username) WHERE username IS NOT NULL;
CREATE INDEX idx_users_zalo_id ON users(zalo_id) WHERE zalo_id IS NOT NULL;
CREATE INDEX idx_users_roles ON users(roles);

