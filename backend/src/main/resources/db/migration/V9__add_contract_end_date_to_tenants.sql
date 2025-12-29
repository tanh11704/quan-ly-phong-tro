-- Add contract_end_date column to tenants table
-- This field stores the expected contract end date (null for indefinite contracts)
-- Separate from end_date which stores the actual move-out date

ALTER TABLE tenants ADD COLUMN contract_end_date DATE;

COMMENT ON COLUMN tenants.contract_end_date IS 'Ngày hết hạn hợp đồng dự kiến (null = vô thời hạn)';
