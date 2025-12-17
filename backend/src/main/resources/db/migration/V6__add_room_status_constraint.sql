-- Thêm CHECK constraint để đảm bảo status chỉ có 3 giá trị hợp lệ
ALTER TABLE rooms
    ADD CONSTRAINT chk_room_status
        CHECK (status IN ('VACANT', 'OCCUPIED', 'MAINTENANCE'));

-- Cập nhật các giá trị status không hợp lệ (nếu có) thành VACANT
UPDATE rooms
SET status = 'VACANT'
WHERE status NOT IN ('VACANT', 'OCCUPIED', 'MAINTENANCE') OR status IS NULL;
