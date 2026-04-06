-- Add google_id column to users table for OAuth2
ALTER TABLE users ADD COLUMN google_id VARCHAR(255) NULL;
ALTER TABLE users ADD UNIQUE INDEX idx_google_id (google_id);

-- Fix password column to allow null for OAuth2 users
ALTER TABLE users MODIFY COLUMN password VARCHAR(255) NULL;
