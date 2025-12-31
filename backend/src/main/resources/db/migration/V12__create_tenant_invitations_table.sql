CREATE TABLE tenant_invitations (
    id UUID PRIMARY KEY,
    room_id INTEGER NOT NULL REFERENCES rooms(id),
    email VARCHAR(255) NOT NULL,
    is_contract_holder BOOLEAN DEFAULT FALSE,
    contract_end_date DATE,
    status VARCHAR(20) NOT NULL,
    expired_at TIMESTAMP WITH TIME ZONE NOT NULL,
    invited_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uq_invite_room_email_pending
ON tenant_invitations(room_id, email)
WHERE status = 'PENDING';
