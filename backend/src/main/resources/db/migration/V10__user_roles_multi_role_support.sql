-- Migration for multi-role support
-- Runs in transaction (Flyway default)

-- Verify count before migration
DO $$
DECLARE
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO user_count FROM users WHERE roles IS NOT NULL;
    RAISE NOTICE 'Users with roles before migration: %', user_count;
END $$;

-- Create pivot table for user roles (many-to-many)
CREATE TABLE user_roles (
    user_id VARCHAR(50) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_id, role)
);

-- Create index for faster lookups
CREATE INDEX idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX idx_user_roles_role ON user_roles(role);

-- Migrate existing data from users.roles column
INSERT INTO user_roles (user_id, role)
SELECT id, roles FROM users WHERE roles IS NOT NULL;

-- Verify count after migration
DO $$
DECLARE
    role_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO role_count FROM user_roles;
    RAISE NOTICE 'User roles after migration: %', role_count;
END $$;

-- Drop old column (data already migrated)
ALTER TABLE users DROP COLUMN roles;
