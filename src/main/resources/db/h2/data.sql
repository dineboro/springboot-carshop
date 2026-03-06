INSERT INTO vets VALUES (default, 'James', 'Carter');
INSERT INTO vets VALUES (default, 'Helen', 'Leary');
INSERT INTO vets VALUES (default, 'Linda', 'Douglas');
INSERT INTO vets VALUES (default, 'Rafael', 'Ortega');
INSERT INTO vets VALUES (default, 'Henry', 'Stevens');
INSERT INTO vets VALUES (default, 'Sharon', 'Jenkins');

INSERT INTO specialties VALUES (default, 'radiology');
INSERT INTO specialties VALUES (default, 'surgery');
INSERT INTO specialties VALUES (default, 'dentistry');

INSERT INTO vet_specialties VALUES (2, 1);
INSERT INTO vet_specialties VALUES (3, 2);
INSERT INTO vet_specialties VALUES (3, 3);
INSERT INTO vet_specialties VALUES (4, 2);
INSERT INTO vet_specialties VALUES (5, 1);

INSERT INTO types VALUES (default, 'cat');
INSERT INTO types VALUES (default, 'dog');
INSERT INTO types VALUES (default, 'lizard');
INSERT INTO types VALUES (default, 'snake');
INSERT INTO types VALUES (default, 'bird');
INSERT INTO types VALUES (default, 'hamster');

INSERT INTO owners VALUES (default, 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023');
INSERT INTO owners VALUES (default, 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749');
INSERT INTO owners VALUES (default, 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763');
INSERT INTO owners VALUES (default, 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198');
INSERT INTO owners VALUES (default, 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765');
INSERT INTO owners VALUES (default, 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654');
INSERT INTO owners VALUES (default, 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387');
INSERT INTO owners VALUES (default, 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683');
INSERT INTO owners VALUES (default, 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435');
INSERT INTO owners VALUES (default, 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487');

INSERT INTO pets VALUES (default, 'Leo', '2010-09-07', 1, 1);
INSERT INTO pets VALUES (default, 'Basil', '2012-08-06', 6, 2);
INSERT INTO pets VALUES (default, 'Rosy', '2011-04-17', 2, 3);
INSERT INTO pets VALUES (default, 'Jewel', '2010-03-07', 2, 3);
INSERT INTO pets VALUES (default, 'Iggy', '2010-11-30', 3, 4);
INSERT INTO pets VALUES (default, 'George', '2010-01-20', 4, 5);
INSERT INTO pets VALUES (default, 'Samantha', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Max', '2012-09-04', 1, 6);
INSERT INTO pets VALUES (default, 'Lucky', '2011-08-06', 5, 7);
INSERT INTO pets VALUES (default, 'Mulligan', '2007-02-24', 2, 8);
INSERT INTO pets VALUES (default, 'Freddy', '2010-03-09', 5, 9);
INSERT INTO pets VALUES (default, 'Lucky', '2010-06-24', 2, 10);
INSERT INTO pets VALUES (default, 'Sly', '2012-06-08', 1, 10);

INSERT INTO visits VALUES (default, 7, '2013-01-01', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-02', 'rabies shot');
INSERT INTO visits VALUES (default, 8, '2013-01-03', 'neutered');
INSERT INTO visits VALUES (default, 7, '2013-01-04', 'spayed');

-- Update existing users (set NULL to TRUE)
UPDATE users
SET is_active = TRUE
WHERE is_active IS NULL;

UPDATE users
SET is_approved = TRUE
WHERE is_approved IS NULL;

-- Make columns NOT NULL with defaults
ALTER TABLE users
  MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE users
  MODIFY COLUMN is_approved BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure admin is set correctly
UPDATE users
SET is_active = TRUE,
    is_approved = TRUE
WHERE email = 'admin@carrepair.com';-- Update existing users (set NULL to TRUE)
UPDATE users
SET is_active = TRUE
WHERE is_active IS NULL;

UPDATE users
SET is_approved = TRUE
WHERE is_approved IS NULL;

-- Make columns NOT NULL with defaults
ALTER TABLE users
  MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE users
  MODIFY COLUMN is_approved BOOLEAN NOT NULL DEFAULT FALSE;

-- Ensure admin is set correctly
UPDATE users
SET is_active = TRUE,
    is_approved = TRUE
WHERE email = 'admin@carrepair.com';

INSERT INTO permissions (name, description) VALUES
-- User Management
('CREATE_USER', 'Create new user accounts'),
('EDIT_USER', 'Edit user accounts'),
('DELETE_USER', 'Delete user accounts'),
('VIEW_USERS', 'View user list'),
('MANAGE_ROLES', 'Assign/remove user roles'),
('APPROVE_STAFF', 'Approve staff registrations'),

-- Customer Management
('CREATE_CUSTOMER', 'Create new customers'),
('EDIT_CUSTOMER', 'Edit customer information'),
('DELETE_CUSTOMER', 'Delete customers'),
('VIEW_CUSTOMERS', 'View all customers'),
('INVITE_CUSTOMER', 'Send portal registration invites'),

-- Vehicle Management
('CREATE_VEHICLE', 'Add new vehicles'),
('EDIT_VEHICLE', 'Edit vehicle information'),
('DELETE_VEHICLE', 'Delete vehicles'),
('VIEW_VEHICLES', 'View all vehicles'),

-- Appointment Management
('CREATE_APPOINTMENT', 'Create appointments'),
('EDIT_APPOINTMENT', 'Edit appointments'),
('CANCEL_APPOINTMENT', 'Cancel appointments'),
('VIEW_APPOINTMENTS', 'View all appointments'),
('ASSIGN_MECHANIC', 'Assign mechanics to jobs'),

-- Service Management
('CREATE_SERVICE', 'Create service records'),
('EDIT_SERVICE', 'Edit service records'),
('VIEW_SERVICES', 'View service records'),

-- Financial
('CREATE_ESTIMATE', 'Create estimates'),
('APPROVE_ESTIMATE', 'Approve estimates'),
('CREATE_INVOICE', 'Generate invoices'),
('EDIT_INVOICE', 'Edit invoices'),
('VIEW_INVOICES', 'View invoices'),
('DELETE_INVOICE', 'Delete invoices'),
('PROCESS_PAYMENT', 'Process payments'),
('VIEW_PAYMENTS', 'View payment records'),
('REFUND_PAYMENT', 'Issue refunds'),

-- Purchase Orders
('CREATE_PURCHASE_ORDER', 'Create purchase orders'),
('APPROVE_PURCHASE_ORDER', 'Approve purchase orders'),
('VIEW_PURCHASE_ORDERS', 'View purchase orders'),

-- Reporting
('VIEW_REPORTS', 'Access reports'),
('VIEW_REVENUE_REPORT', 'View revenue reports'),
('VIEW_SERVICE_REPORT', 'View service statistics'),

-- Employee Management
('CREATE_EMPLOYEE', 'Add employees'),
('EDIT_EMPLOYEE', 'Edit employee records'),
('DELETE_EMPLOYEE', 'Remove employees'),
('VIEW_EMPLOYEES', 'View employee list'),

-- Communication
('VIEW_COMMUNICATIONS', 'View communications'),
('CREATE_COMMUNICATION', 'Log communications'),

-- Customer Portal (for CUSTOMER role)
('VIEW_OWN_VEHICLES', 'View own vehicles'),
('VIEW_OWN_SERVICE_HISTORY', 'View own service history'),
('VIEW_OWN_INVOICES', 'View own invoices'),
('VIEW_OWN_APPOINTMENTS', 'View own appointments');


-- Assign ALL permissions to ADMIN
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, (SELECT id FROM roles WHERE name = 'ADMIN')
FROM permissions p;


-- Assign permissions to MANAGER
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, (SELECT id FROM roles WHERE name = 'MANAGER')
FROM permissions p
WHERE p.name IN (
                 'VIEW_USERS',
                 'CREATE_CUSTOMER', 'EDIT_CUSTOMER', 'DELETE_CUSTOMER', 'VIEW_CUSTOMERS', 'INVITE_CUSTOMER',
                 'CREATE_VEHICLE', 'EDIT_VEHICLE', 'DELETE_VEHICLE', 'VIEW_VEHICLES',
                 'CREATE_APPOINTMENT', 'EDIT_APPOINTMENT', 'CANCEL_APPOINTMENT', 'VIEW_APPOINTMENTS', 'ASSIGN_MECHANIC',
                 'CREATE_SERVICE', 'EDIT_SERVICE', 'VIEW_SERVICES',
                 'CREATE_ESTIMATE', 'APPROVE_ESTIMATE',
                 'CREATE_INVOICE', 'EDIT_INVOICE', 'VIEW_INVOICES',
                 'PROCESS_PAYMENT', 'VIEW_PAYMENTS', 'REFUND_PAYMENT',
                 'CREATE_PURCHASE_ORDER', 'APPROVE_PURCHASE_ORDER', 'VIEW_PURCHASE_ORDERS',
                 'VIEW_REPORTS', 'VIEW_REVENUE_REPORT', 'VIEW_SERVICE_REPORT',
                 'CREATE_EMPLOYEE', 'EDIT_EMPLOYEE', 'VIEW_EMPLOYEES',
                 'VIEW_COMMUNICATIONS', 'CREATE_COMMUNICATION'
  );


-- Assign permissions to RECEPTIONIST
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, (SELECT id FROM roles WHERE name = 'RECEPTIONIST')
FROM permissions p
WHERE p.name IN (
                 'CREATE_CUSTOMER', 'EDIT_CUSTOMER', 'VIEW_CUSTOMERS', 'INVITE_CUSTOMER',
                 'CREATE_VEHICLE', 'EDIT_VEHICLE', 'VIEW_VEHICLES',
                 'CREATE_APPOINTMENT', 'EDIT_APPOINTMENT', 'VIEW_APPOINTMENTS',
                 'VIEW_SERVICES',
                 'CREATE_ESTIMATE', 'VIEW_INVOICES',
                 'PROCESS_PAYMENT', 'VIEW_PAYMENTS',
                 'VIEW_COMMUNICATIONS', 'CREATE_COMMUNICATION'
  );


-- Assign permissions to TECHNICIAN
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, (SELECT id FROM roles WHERE name = 'TECHNICIAN')
FROM permissions p
WHERE p.name IN (
                 'VIEW_CUSTOMERS',
                 'VIEW_VEHICLES',
                 'VIEW_APPOINTMENTS',
                 'CREATE_SERVICE', 'EDIT_SERVICE', 'VIEW_SERVICES'
  );


-- Assign permissions to CUSTOMER (for portal)
INSERT INTO permission_role (permission_id, role_id)
SELECT p.id, (SELECT id FROM roles WHERE name = 'CUSTOMER')
FROM permissions p
WHERE p.name IN (
                 'VIEW_OWN_VEHICLES',
                 'VIEW_OWN_SERVICE_HISTORY',
                 'VIEW_OWN_INVOICES',
                 'VIEW_OWN_APPOINTMENTS'
  );



-- 1. Create ADMIN role (if doesn't exist)
INSERT INTO roles (name, description)
VALUES ('ADMIN', 'System Administrator')
ON DUPLICATE KEY UPDATE description = 'System Administrator';

-- 2. Create admin user
-- Password: Admin123!
INSERT INTO users (first_name, last_name, email, password_hash, phone, is_active, is_approved, created_at, updated_at)
VALUES (
         'Admin',
         'User',
         'admin@carrepair.com',
         '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
         '5551234567',
         1,
         1,
         NOW(),
         NOW()
       );

-- 3. Assign ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = 'admin@carrepair.com'
  AND r.name = 'ADMIN';

-- 4. Verify it worked
SELECT id, email, is_active, is_approved
FROM users
WHERE email = 'admin@carrepair.com';
