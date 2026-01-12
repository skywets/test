
INSERT INTO roles (name) SELECT 'USER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USER');
INSERT INTO roles (name) SELECT 'COURIER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'COURIER');
INSERT INTO roles (name) SELECT 'RESTAURANT_OWNER' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'RESTAURANT_OWNER');
INSERT INTO roles (name) SELECT 'ADMIN' WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'ADMIN');


INSERT INTO permissions (name) SELECT 'USER_READ' WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_READ');
INSERT INTO permissions (name) SELECT 'USER_WRITE' WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'USER_WRITE');
INSERT INTO permissions (name) SELECT 'ADMIN_FULL' WHERE NOT EXISTS (SELECT 1 FROM permissions WHERE name = 'ADMIN_FULL');

INSERT INTO users (name, email, password, active, created_at)
SELECT 'Admin', 'admin@example.com', '$2a$10$76atvSyzvpkfXW9.B9Zsh.9.fH0u1pLp0eHInV6hV6uM8sI6lXv6m', true, NOW()
    WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@example.com');


INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.email = 'admin@example.com' AND r.name = 'ADMIN'
  AND NOT EXISTS (SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id AND ur.role_id = r.id);
