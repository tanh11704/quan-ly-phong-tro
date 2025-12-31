ALTER TABLE tenants ADD COLUMN user_id VARCHAR(255);
CREATE INDEX idx_tenants_user_id ON tenants(user_id);
