CREATE TABLE IF NOT EXISTS group_invitations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id),
    invited_email VARCHAR(255) NOT NULL,
    invited_by UUID NOT NULL REFERENCES users(id),
    token VARCHAR(36) NOT NULL DEFAULT gen_random_uuid()::text,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    expires_at TIMESTAMP NOT NULL DEFAULT (now() + interval '7 days'),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS group_invitations_token_unique ON group_invitations (token);
CREATE INDEX IF NOT EXISTS group_invitations_email_idx ON group_invitations (invited_email);
CREATE INDEX IF NOT EXISTS group_invitations_status_idx ON group_invitations (status);
