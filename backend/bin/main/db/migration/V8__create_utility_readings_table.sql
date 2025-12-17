-- Create utility_readings table for recording meter readings
CREATE TABLE utility_readings (
    id SERIAL PRIMARY KEY,
    room_id INT NOT NULL,
    month VARCHAR(20) NOT NULL, -- Format: 'YYYY-MM'
    electric_index INT,
    water_index INT,
    image_evidence VARCHAR(500), -- URL or path to meter image
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_room_utility_reading FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    UNIQUE(room_id, month) -- Ensure one reading per room per month
);

CREATE INDEX idx_utility_readings_room_month ON utility_readings(room_id, month);
