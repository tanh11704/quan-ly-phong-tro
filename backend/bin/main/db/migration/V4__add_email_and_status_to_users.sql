-- Thêm cột email và status vào bảng users
ALTER TABLE users
    ADD COLUMN email VARCHAR(255) UNIQUE,
    ADD COLUMN status VARCHAR(50) DEFAULT 'ACTIVE';

-- Cập nhật status cho các user hiện tại (Admin và Tenant đã tạo trước đó)
UPDATE users SET status = 'ACTIVE' WHERE status IS NULL;

-- Tạo chỉ mục cho email
CREATE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX idx_users_status ON users(status);
