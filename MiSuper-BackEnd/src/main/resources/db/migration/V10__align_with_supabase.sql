-- V10: Align schema with current Supabase structure
-- Makes password_history compatible with code (credential_id nullable, user_id added)

ALTER TABLE password_history ALTER COLUMN credential_id DROP NOT NULL;
ALTER TABLE password_history ADD COLUMN IF NOT EXISTS user_id UUID REFERENCES users(id);
ALTER TABLE password_history ADD COLUMN IF NOT EXISTS active BOOLEAN DEFAULT TRUE;
ALTER TABLE password_history ADD COLUMN IF NOT EXISTS expired_at TIMESTAMP;
