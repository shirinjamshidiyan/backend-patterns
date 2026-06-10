CREATE TABLE orders (
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    customer_id UUID NOT NULL,
    total_amount NUMERIC(12,2) NOT NULL CHECK ( total_amount >= 0 ),
    status VARCHAR(50) NOT NULL CHECK (  status in ('CREATED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);