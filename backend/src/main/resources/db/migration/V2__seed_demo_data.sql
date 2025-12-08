INSERT INTO buildings (id, name, owner_name, owner_phone, elec_unit_price, water_unit_price, water_calc_method)
VALUES (1, 'Trọ Xanh (Demo)', 'Nguyễn Văn Chủ', '0909123456', 3500, 20000, 'BY_METER');

INSERT INTO rooms (id, building_id, room_no, price, status)
VALUES (1, 1, 'P.101', 3000000, 'OCCUPIED');

INSERT INTO rooms (id, building_id, room_no, price, status)
VALUES (2, 1, 'P.102', 3500000, 'OCCUPIED');

INSERT INTO tenants (room_id, name, phone, is_contract_holder, start_date)
VALUES (1, 'Trần Văn Khách', '0988777666', true, '2024-01-01');

INSERT INTO meter_records (room_id, type, period, previous_value, current_value)
VALUES (1, 'ELEC', '2025-12', 1000, 1100); -- Dùng 100 kWh

INSERT INTO meter_records (room_id, type, period, previous_value, current_value)
VALUES (1, 'WATER', '2025-12', 200, 210);   -- Dùng 10 m3

SELECT setval('buildings_id_seq', (SELECT MAX(id) FROM buildings));
SELECT setval('rooms_id_seq', (SELECT MAX(id) FROM rooms));
SELECT setval('tenants_id_seq', (SELECT MAX(id) FROM tenants));