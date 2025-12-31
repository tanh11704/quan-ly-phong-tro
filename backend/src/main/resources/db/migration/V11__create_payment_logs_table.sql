-- Payment audit log for tracking all invoice status changes
CREATE TABLE payment_logs (
    id SERIAL PRIMARY KEY,
    invoice_id INTEGER NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    action VARCHAR(30) NOT NULL,           -- PAID, MARKED_OVERDUE, STATUS_CHANGED
    old_status VARCHAR(20),
    new_status VARCHAR(20),
    amount INTEGER,
    performed_by VARCHAR(255),             -- User ID or 'SYSTEM'
    note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Index for querying logs by invoice
CREATE INDEX idx_payment_logs_invoice ON payment_logs(invoice_id);

-- Index for querying logs by date (for audit reports)
CREATE INDEX idx_payment_logs_created ON payment_logs(created_at);

COMMENT ON TABLE payment_logs IS 'Audit log for all invoice payment and status changes';
COMMENT ON COLUMN payment_logs.action IS 'Action type: PAID, MARKED_OVERDUE, STATUS_CHANGED';
COMMENT ON COLUMN payment_logs.performed_by IS 'User ID who performed action, or SYSTEM for automated jobs';
