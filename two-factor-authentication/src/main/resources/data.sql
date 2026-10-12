INSERT INTO user_entity
(first_name, email, password, enabled, role)
VALUES
    (
        'Demo Admin',
        'admin@example.test',
        '$2a$12$3fvFFfMXwbAvVfKkKgGfV..p1KPapO/wuHNB0I.6yr1jZCtD./0s6',
        true,
        'ADMIN'
    ),
    (
        'Demo Manager',
        'manager@example.test',
        '$2a$12$3fvFFfMXwbAvVfKkKgGfV..p1KPapO/wuHNB0I.6yr1jZCtD./0s6',
        true,
        'MANAGER'
    ),
    (
        'Demo User',
        'user@example.test',
        '$2a$12$3fvFFfMXwbAvVfKkKgGfV..p1KPapO/wuHNB0I.6yr1jZCtD./0s6',
        true,
        'USER'
    )
ON CONFLICT (email) DO NOTHING;