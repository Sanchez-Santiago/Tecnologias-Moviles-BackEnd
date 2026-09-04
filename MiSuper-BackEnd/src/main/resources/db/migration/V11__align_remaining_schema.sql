-- V11: Align schema with reference schema (DataBase.sql) and real Supabase DB.
-- Adds columns/tables used by the app that V1-V10 never created, and fixes the
-- notifications.data type (jsonb in Supabase vs text expected by the model).

-- 1. groups: owner_id, type, is_personal, cycle_day
ALTER TABLE groups ADD COLUMN IF NOT EXISTS owner_id UUID REFERENCES users(id);
ALTER TABLE groups ADD COLUMN IF NOT EXISTS "type" VARCHAR(50) NOT NULL DEFAULT 'FAMILIA';
ALTER TABLE groups ADD COLUMN IF NOT EXISTS is_personal BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE groups ADD COLUMN IF NOT EXISTS cycle_day INTEGER NOT NULL DEFAULT 1 CHECK (cycle_day >= 1 AND cycle_day <= 28);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'groups' AND column_name = 'owner_id') THEN
        UPDATE groups SET owner_id = created_by WHERE owner_id IS NULL AND created_by IS NOT NULL;
        IF NOT EXISTS (SELECT 1 FROM groups WHERE owner_id IS NULL) THEN
            ALTER TABLE groups ALTER COLUMN owner_id SET NOT NULL;
        END IF;
    END IF;
END $$;

-- 2. group_invitations: invited_user_id, accepted_at + named FKs
ALTER TABLE group_invitations ADD COLUMN IF NOT EXISTS invited_user_id UUID REFERENCES users(id);
ALTER TABLE group_invitations ADD COLUMN IF NOT EXISTS accepted_at TIMESTAMP;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_invitation_group') THEN
        ALTER TABLE group_invitations ADD CONSTRAINT fk_invitation_group FOREIGN KEY (group_id) REFERENCES groups(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_invitation_user') THEN
        ALTER TABLE group_invitations ADD CONSTRAINT fk_invitation_user FOREIGN KEY (invited_user_id) REFERENCES users(id);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_invitation_sender') THEN
        ALTER TABLE group_invitations ADD CONSTRAINT fk_invitation_sender FOREIGN KEY (invited_by) REFERENCES users(id);
    END IF;
END $$;

-- 3. notifications: group_id, description, read_at + type fix + FK + index
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS group_id UUID REFERENCES groups(id);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS read_at TIMESTAMP;
ALTER TABLE notifications ALTER COLUMN data TYPE TEXT USING data::text;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'fk_notification_group') THEN
        ALTER TABLE notifications ADD CONSTRAINT fk_notification_group FOREIGN KEY (group_id) REFERENCES groups(id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS notifications_group_id_idx ON notifications (group_id);

-- 4. users: birth_date, profile_photo, active (parity)
ALTER TABLE users ADD COLUMN IF NOT EXISTS birth_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS profile_photo TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;

-- 5. group_members: active, left_at (parity)
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE group_members ADD COLUMN IF NOT EXISTS left_at TIMESTAMP;

-- 6. audit_logs: old_data, new_data, ip_address (parity)
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS old_data JSONB;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS new_data JSONB;
ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS ip_address VARCHAR(45);

-- 7. Missing tables from the reference schema
CREATE TABLE IF NOT EXISTS user_credentials (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    failed_attempts INTEGER NOT NULL DEFAULT 0 CHECK (failed_attempts >= 0),
    account_locked BOOLEAN NOT NULL DEFAULT FALSE,
    locked_until TIMESTAMP,
    last_login TIMESTAMP,
    last_password_change TIMESTAMP NOT NULL DEFAULT now(),
    password_expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_credentials_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS product_price_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL,
    store_id UUID,
    price NUMERIC NOT NULL CHECK (price >= 0),
    observed_at TIMESTAMP NOT NULL DEFAULT now(),
    source VARCHAR(20) CHECK (source IN ('TICKET', 'USER', 'AI', 'OTHER')),
    CONSTRAINT fk_price_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT fk_price_store FOREIGN KEY (store_id) REFERENCES stores(id)
);

CREATE INDEX IF NOT EXISTS product_price_history_product_id_idx ON product_price_history (product_id);

CREATE TABLE IF NOT EXISTS ai_actions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID,
    shopping_list_item_id UUID,
    action_type VARCHAR(50) NOT NULL CHECK (action_type IN ('UPDATE_PRICE', 'UPDATE_BRAND', 'MARK_PURCHASED', 'CREATE_PRODUCT', 'MERGE_PRODUCT', 'OTHER')),
    old_value JSONB,
    new_value JSONB,
    accepted BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_ai_action_item FOREIGN KEY (shopping_list_item_id) REFERENCES shopping_list_items(id)
);