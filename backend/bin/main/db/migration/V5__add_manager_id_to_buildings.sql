-- Thêm cột manager_id vào bảng buildings
ALTER TABLE buildings
    ADD COLUMN manager_id VARCHAR(50);

-- Tạo foreign key constraint
ALTER TABLE buildings
    ADD CONSTRAINT fk_building_manager
        FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL;

-- Tạo index để tìm kiếm nhanh hơn
CREATE INDEX idx_buildings_manager_id ON buildings(manager_id) WHERE manager_id IS NOT NULL;

-- Cập nhật các building hiện tại (nếu có) - set manager_id cho admin user nếu có
-- Lưu ý: Chỉ chạy nếu có admin user trong hệ thống
-- UPDATE buildings SET manager_id = (SELECT id FROM users WHERE roles = 'ADMIN' LIMIT 1) WHERE manager_id IS NULL;
