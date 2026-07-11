-- V9: Refactor schema to match new business rules
-- Replaces purchases + financial_transactions with unified tickets
-- Replaces shopping_list_products with shopping_list_items
-- Removes group_periods in favor of date-based filtering
-- Budgets now have group_id, start_date, end_date (no period_id)
-- Tickets no longer reference period_id

-- 1. Drop old tables (respect FK order)
DROP TABLE IF EXISTS ticket_messages CASCADE;
DROP TABLE IF EXISTS purchase_products CASCADE;
DROP TABLE IF EXISTS budget_items CASCADE;
DROP TABLE IF EXISTS shopping_list_products CASCADE;
DROP TABLE IF EXISTS purchases CASCADE;
DROP TABLE IF EXISTS financial_transactions CASCADE;
DROP TABLE IF EXISTS incomes CASCADE;
DROP TABLE IF EXISTS expenses CASCADE;
DROP TABLE IF EXISTS budgets CASCADE;
DROP TABLE IF EXISTS group_monthly_periods CASCADE;
DROP TABLE IF EXISTS group_periods CASCADE;
DROP TABLE IF EXISTS tickets CASCADE;
DROP TABLE IF EXISTS period_summaries CASCADE;
DROP TABLE IF EXISTS ai_recommendations CASCADE;
DROP TABLE IF EXISTS ai_reports CASCADE;

-- 2. Create shopping_lists (referenced by shopping_list_items)
CREATE TABLE IF NOT EXISTS shopping_lists (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id),
    created_by UUID REFERENCES users(id),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS shopping_lists_group_id_idx ON shopping_lists (group_id);

-- 3. Create shopping_list_items (replaces shopping_list_products)
CREATE TABLE IF NOT EXISTS shopping_list_items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shopping_list_id UUID NOT NULL REFERENCES shopping_lists(id),
    product_id UUID REFERENCES products(id),
    custom_product_name VARCHAR(255),
    estimated_price NUMERIC(12, 2) CHECK (estimated_price >= 0),
    estimated_quantity NUMERIC(12, 2) DEFAULT 1 CHECK (estimated_quantity > 0),
    estimated_brand VARCHAR(255),
    unit VARCHAR(20) CHECK (unit IN ('UNIT', 'KG', 'G', 'L', 'ML', 'PACK')),
    priority VARCHAR(20) NOT NULL DEFAULT 'PRIMARY' CHECK (priority IN ('ESSENTIAL', 'PRIMARY', 'SECONDARY')),
    checked BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    last_checked_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS shopping_list_items_list_id_idx ON shopping_list_items (shopping_list_id);

-- 4. Create budgets (linked to group, with date range)
CREATE TABLE IF NOT EXISTS budgets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total NUMERIC(12, 2) NOT NULL,
    created_by UUID REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS budgets_group_id_idx ON budgets (group_id);

-- 5. Create tickets (financial movements: INCOME / EXPENSE, no period_id)
CREATE TABLE IF NOT EXISTS tickets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id),
    uploaded_by UUID NOT NULL REFERENCES users(id),
    supermarket_name VARCHAR(255) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    movement_type VARCHAR(20) CHECK (movement_type IN ('EXPENSE', 'INCOME')),
    purchase_date TIMESTAMP NOT NULL,
    image_url TEXT,
    comment TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'ERROR')),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS tickets_group_id_idx ON tickets (group_id);

-- 6. Create ticket_products (products detected in ticket images / manual entry)
CREATE TABLE IF NOT EXISTS ticket_products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL REFERENCES tickets(id),
    product_id UUID REFERENCES products(id),
    detected_name VARCHAR(255),
    quantity NUMERIC(12, 2),
    price NUMERIC(12, 2),
    brand VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS ticket_products_ticket_id_idx ON ticket_products (ticket_id);

-- 7. Create ticket_analysis (OCR / AI analysis results)
CREATE TABLE IF NOT EXISTS ticket_analysis (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ticket_id UUID NOT NULL UNIQUE REFERENCES tickets(id),
    ocr_text TEXT,
    summary TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'ERROR')),
    processed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 8. Create group_settings
CREATE TABLE IF NOT EXISTS group_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL UNIQUE REFERENCES groups(id),
    currency VARCHAR(10) NOT NULL DEFAULT 'ARS',
    notifications_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    ai_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    allow_member_invite BOOLEAN NOT NULL DEFAULT FALSE,
    allow_member_delete BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 9. Create notification_settings
CREATE TABLE IF NOT EXISTS notification_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    budget_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    shopping_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    invitation_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    ai_notifications BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

-- 10. Create audit_logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(100) NOT NULL,
    entity VARCHAR(100),
    entity_id UUID,
    description TEXT,
    group_id UUID REFERENCES groups(id),
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
