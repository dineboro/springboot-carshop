CREATE TABLE IF NOT EXISTS vets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS specialties (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vet_specialties (
  vet_id INT(4) UNSIGNED NOT NULL,
  specialty_id INT(4) UNSIGNED NOT NULL,
  FOREIGN KEY (vet_id) REFERENCES vets(id),
  FOREIGN KEY (specialty_id) REFERENCES specialties(id),
  UNIQUE (vet_id,specialty_id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS types (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(80),
  INDEX(name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS owners (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(30),
  last_name VARCHAR(30),
  address VARCHAR(255),
  city VARCHAR(80),
  telephone VARCHAR(20),
  INDEX(last_name)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS pets (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(30),
  birth_date DATE,
  type_id INT(4) UNSIGNED NOT NULL,
  owner_id INT(4) UNSIGNED,
  INDEX(name),
  FOREIGN KEY (owner_id) REFERENCES owners(id),
  FOREIGN KEY (type_id) REFERENCES types(id)
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS visits (
  id INT(4) UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  pet_id INT(4) UNSIGNED,
  visit_date DATE,
  description VARCHAR(255),
  FOREIGN KEY (pet_id) REFERENCES pets(id)
) engine=InnoDB;
CREATE TABLE IF NOT EXISTS users (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   first_name VARCHAR(50),
  last_name VARCHAR(50),
  nickname VARCHAR(50),
  nickname_is_flagged TINYINT DEFAULT 0,
  email VARCHAR(255) NOT NULL,
  public_email TINYINT DEFAULT 0,
  phone VARCHAR(255),
  preferred_language varchar(50) null,
  public_phone TINYINT DEFAULT 0,
  password_hash VARCHAR(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME,
  UNIQUE INDEX idx_users_email (email),
  INDEX idx_users_name (last_name, first_name)
  ) engine=InnoDB;

CREATE TABLE IF NOT EXISTS roles (
                                   id INT AUTO_INCREMENT PRIMARY KEY,
                                   name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255)
  );

CREATE TABLE IF NOT EXISTS permissions (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL UNIQUE,
  description VARCHAR(255)
  );

CREATE TABLE IF NOT EXISTS user_roles (
                                        user_id INT NOT NULL,
                                        role_id INT NOT NULL,
                                        PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS permission_role (
                                             permission_id INT NOT NULL,
                                             role_id INT NOT NULL,
                                             PRIMARY KEY (permission_id, role_id),
  FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
  FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
  );

CREATE TABLE IF NOT EXISTS schools (
                                     id INT AUTO_INCREMENT PRIMARY KEY,
                                     name VARCHAR(255) NOT NULL,
  domain VARCHAR(255) NOT NULL,
  status_id ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  UNIQUE INDEX idx_schools_domain (domain)
  )engine=InnoDB;

CREATE TABLE IF NOT EXISTS locations (
                                       id INT AUTO_INCREMENT PRIMARY KEY,
                                       school_id INT NOT NULL,
                                       parent_location_id INT NULL,
                                       name VARCHAR(255) NOT NULL,
  description TEXT,
  address VARCHAR(255),
  latitude DECIMAL(8,4),
  longitude DECIMAL(8,4),
  status_id ENUM('DRAFT', 'ACTIVE', 'CLOSED', 'COMING_SOON') DEFAULT 'ACTIVE',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  CONSTRAINT fk_locations_school FOREIGN KEY (school_id) REFERENCES schools(id) ON DELETE CASCADE,
  CONSTRAINT fk_locations_parent FOREIGN KEY (parent_location_id) REFERENCES locations(id) ON DELETE SET NULL,
  UNIQUE KEY uk_school_location (school_id, name)
  )engine=InnoDB;

CREATE TABLE IF NOT EXISTS subscriptions (
                                           id INT AUTO_INCREMENT PRIMARY KEY,
                                           name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  monthly_price INT NOT NULL,
  annual_price INT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME,
  UNIQUE KEY uk_subscription_name (name)
  )engine=InnoDB;

CREATE TABLE IF NOT EXISTS customer (
  customer_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  customer_name VARCHAR(255) NOT NULL,
  phone VARCHAR(20) NOT NULL,
  email VARCHAR(255),
  status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
  created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_date DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS vehicle (
  vin VARCHAR(17) NOT NULL PRIMARY KEY,
  customer_id INT,
  make VARCHAR(255) NOT NULL,
  model VARCHAR(255) NOT NULL,
  model_year INT NOT NULL,
  color VARCHAR(100),
  license_plate VARCHAR(50),
  current_mileage INT,
  status ENUM('ACTIVE', 'IN_SERVICE', 'RETIRED') DEFAULT 'ACTIVE',
  created_date DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted_at DATETIME DEFAULT NULL,
  FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE SET NULL
) engine=InnoDB;

CREATE TABLE IF NOT EXISTS service_appointment (
  appointment_id INT NOT NULL AUTO_INCREMENT,
  customer_id INT NOT NULL,
  vin VARCHAR(17) NOT NULL,
  appointment_date DATETIME NOT NULL,
  description VARCHAR(1024) NULL,
  status ENUM('Scheduled', 'In_Progress', 'Completed', 'Cancelled', 'No_Show') NOT NULL DEFAULT 'Scheduled',
  notes TEXT NULL,
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (appointment_id),
  FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
  FOREIGN KEY (vin) REFERENCES vehicle(vin) ON DELETE CASCADE,
  INDEX idx_appointment_customer_id (customer_id),
  INDEX idx_appointment_vin (vin),
  INDEX idx_appointment_date (appointment_date),
  INDEX idx_appointment_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Service appointments';

CREATE TABLE IF NOT EXISTS employee (
  employee_id INT NOT NULL AUTO_INCREMENT,
  first_name VARCHAR(50) NOT NULL,
  last_name VARCHAR(50) NOT NULL,
  role ENUM('Technician', 'Service_Advisor', 'Manager', 'Receptionist') NOT NULL,
  email VARCHAR(255) NULL,
  phone VARCHAR(20) NULL,
  hired_date DATE NULL,
  status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,
  PRIMARY KEY (employee_id),
  INDEX idx_employee_last_name (last_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Shop employees and technicians';

-- Service Catalog Table (menu of services offered)
CREATE TABLE IF NOT EXISTS service_catalog (
  service_catalog_id INT NOT NULL AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(500) NULL,
  category ENUM('GENERAL_SERVICE','OIL_CHANGE','TIRE_SERVICE','BRAKE_SERVICE','BATTERY_SERVICE','INSPECTION','DIAGNOSTIC','ELECTRICAL','TRANSMISSION','AC_COOLING','FILTERS_WIPERS','ALIGNMENT','OTHER') NOT NULL DEFAULT 'OTHER',
  default_labor_hours DECIMAL(5,2) NULL,
  default_price DECIMAL(10,2) NULL,
  status ENUM('ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at DATETIME NULL,

  PRIMARY KEY (service_catalog_id),
  INDEX idx_catalog_category (category),
  INDEX idx_catalog_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Catalog of services offered by the shop';


-- Service Line Table (work order tied to an appointment)
CREATE TABLE IF NOT EXISTS service_line (
  service_line_id INT NOT NULL AUTO_INCREMENT,
  appointment_id INT NOT NULL,
  service_catalog_id INT NULL,
  vin VARCHAR(17) NOT NULL,
  service_date DATETIME NOT NULL,
  service_description VARCHAR(1024) NOT NULL,
  labor_hours DECIMAL(5,2) NULL,
  labor_rate DECIMAL(8,2) NULL,
  labor_cost DECIMAL(10,2) NULL,
  parts_cost DECIMAL(10,2) NULL,
  total_cost DECIMAL(10,2) NOT NULL DEFAULT 0,
  notes VARCHAR(1024) NULL,
  assigned_to INT NULL,
  status ENUM('Open', 'In_Progress', 'Completed', 'Cancelled') NOT NULL DEFAULT 'Open',
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (service_line_id),
  FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE CASCADE,
  FOREIGN KEY (service_catalog_id) REFERENCES service_catalog(service_catalog_id) ON DELETE SET NULL,
  FOREIGN KEY (vin) REFERENCES vehicle(vin) ON DELETE CASCADE,
  FOREIGN KEY (assigned_to) REFERENCES employee(employee_id) ON DELETE SET NULL,
  INDEX idx_service_line_appointment_id (appointment_id),
  INDEX idx_service_line_assigned_to (assigned_to),
  INDEX idx_service_line_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Work orders tied to an appointment';


-- Service Line Item Table (individual billable items within a service line)
CREATE TABLE IF NOT EXISTS service_line_item (
  service_line_item_id INT NOT NULL AUTO_INCREMENT,
  service_line_id INT NOT NULL,
  service_catalog_id INT NULL,
  item_type ENUM('Service', 'Part', 'Labor') NOT NULL,
  description VARCHAR(500) NOT NULL,
  quantity DECIMAL(8,2) NOT NULL DEFAULT 1,
  unit_price DECIMAL(10,2) NOT NULL,
  total_price DECIMAL(10,2) NOT NULL,
  notes VARCHAR(500) NULL,
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (service_line_item_id),
  FOREIGN KEY (service_line_id) REFERENCES service_line(service_line_id) ON DELETE CASCADE,
  FOREIGN KEY (service_catalog_id) REFERENCES service_catalog(service_catalog_id) ON DELETE SET NULL,
  INDEX idx_sli_service_line_id (service_line_id),
  INDEX idx_sli_catalog_id (service_catalog_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Individual line items within a service line';


-- Invoice Table
CREATE TABLE IF NOT EXISTS invoice (
  invoice_id INT NOT NULL AUTO_INCREMENT,
  invoice_number VARCHAR(50) NOT NULL,
  appointment_id INT NOT NULL,
  customer_id INT NOT NULL,
  vin VARCHAR(17) NOT NULL,
  invoice_date DATETIME NOT NULL,
  due_date DATETIME NULL,
  subtotal DECIMAL(10,2) NOT NULL DEFAULT 0,
  tax_rate DECIMAL(5,4) NOT NULL DEFAULT 0.0700,
  tax_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
  amount_paid DECIMAL(10,2) NOT NULL DEFAULT 0,
  status ENUM('Draft', 'Sent', 'Paid', 'Partial', 'Void') NOT NULL DEFAULT 'Draft',
  notes TEXT NULL,
  created_by INT NOT NULL,
  created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (invoice_id),
  UNIQUE KEY uk_invoice_number (invoice_number),
  FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE CASCADE,
  FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
  FOREIGN KEY (vin) REFERENCES vehicle(vin) ON DELETE CASCADE,
  FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE RESTRICT,
  INDEX idx_invoice_appointment_id (appointment_id),
  INDEX idx_invoice_customer_id (customer_id),
  INDEX idx_invoice_status (status),
  INDEX idx_invoice_date (invoice_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Customer invoices';


CREATE TABLE IF NOT EXISTS recipes
(
  id bigint unsigned auto_increment primary key,
  recipe_ingredients varchar(255) null,
  instructions       varchar(255) not null,
  type               varchar(50)  null,
  category           varchar(50)  null,
  dietary_preference varchar(50)  null,
  internal_notes     varchar(255) not null,
  constraint id unique (id),
  constraint internal_notes unique (internal_notes)
);

-- Payment Table
CREATE TABLE IF NOT EXISTS payment (
                       payment_id INT NOT NULL AUTO_INCREMENT,
                       invoice_id INT NOT NULL,
                       payment_date DATETIME NOT NULL,
                       payment_method ENUM('Cash', 'Credit_Card', 'Debit_Card', 'Check', 'Bank_Transfer') NOT NULL,
                       amount DECIMAL(10,2) NOT NULL,
                       transaction_id VARCHAR(100) NULL,
                       received_by INT NOT NULL,
                       notes VARCHAR(500) NULL,
                       created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       PRIMARY KEY (payment_id),
                       FOREIGN KEY (invoice_id) REFERENCES invoice(invoice_id) ON DELETE CASCADE,
                       FOREIGN KEY (received_by) REFERENCES users(id) ON DELETE RESTRICT,
                       INDEX idx_payment_invoice_id (invoice_id),
                       INDEX idx_payment_date (payment_date),
                       INDEX idx_payment_received_by (received_by),
                       CONSTRAINT chk_payment_amount CHECK (amount > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Payment records';


-- Estimate Table
CREATE TABLE IF NOT EXISTS estimate (
                        estimate_id INT NOT NULL AUTO_INCREMENT,
                        estimate_number VARCHAR(50) NOT NULL,
                        customer_id INT NOT NULL,
                        vin VARCHAR(17) NOT NULL,
                        estimate_date DATETIME NOT NULL,
                        valid_until DATETIME NULL,
                        description VARCHAR(1024) NULL,
                        labor_cost DECIMAL(10,2) NOT NULL,
                        parts_cost DECIMAL(10,2) NOT NULL,
                        tax_amount DECIMAL(10,2) NOT NULL,
                        total_estimate DECIMAL(10,2) NOT NULL,
                        status ENUM('Draft', 'Sent', 'Approved', 'Rejected', 'Expired') NOT NULL,
                        approved_date DATETIME NULL,
                        converted_to_appointment BOOLEAN NOT NULL DEFAULT FALSE,
                        appointment_id INT NULL,
                        prepared_by INT NOT NULL,
                        created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        PRIMARY KEY (estimate_id),
                        UNIQUE KEY uk_estimate_estimate_number (estimate_number),
                        FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
                        FOREIGN KEY (vin) REFERENCES vehicle(vin) ON DELETE CASCADE,
                        FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE SET NULL,
                        FOREIGN KEY (prepared_by) REFERENCES users(id) ON DELETE RESTRICT,
                        INDEX idx_estimate_customer_id (customer_id),
                        INDEX idx_estimate_vin (vin),
                        INDEX idx_estimate_status (status),
                        INDEX idx_estimate_date (estimate_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Service estimates';


-- EstimateLineItem Table
CREATE TABLE IF NOT EXISTS estimate_line_item (
                                  estimate_line_item_id INT NOT NULL AUTO_INCREMENT,
                                  estimate_id INT NOT NULL,
                                  item_type ENUM('Service', 'Part', 'Labor') NOT NULL,
                                  description VARCHAR(500) NOT NULL,
                                  quantity DECIMAL(8,2) NOT NULL DEFAULT 1,
                                  unit_price DECIMAL(10,2) NOT NULL,
                                  total_price DECIMAL(10,2) NOT NULL,
                                  notes VARCHAR(500) NULL,

                                  PRIMARY KEY (estimate_line_item_id),
                                  FOREIGN KEY (estimate_id) REFERENCES estimate(estimate_id) ON DELETE CASCADE,
                                  INDEX idx_estimate_line_item_estimate_id (estimate_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Estimate line items';


-- CustomerCommunication Table
CREATE TABLE IF NOT EXISTS customer_communication (
                                      communication_id INT NOT NULL AUTO_INCREMENT,
                                      customer_id INT NOT NULL,
                                      appointment_id INT NULL,
                                      communication_type ENUM('Phone_Call', 'Email', 'SMS', 'In_Person', 'Other') NOT NULL,
                                      direction ENUM('Inbound', 'Outbound') NOT NULL,
                                      subject VARCHAR(256) NULL,
                                      notes TEXT NULL,
                                      contacted_by INT NULL,
                                      communication_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      follow_up_required BOOLEAN NOT NULL DEFAULT FALSE,
                                      follow_up_date DATETIME NULL,

                                      PRIMARY KEY (communication_id),
                                      FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
                                      FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE SET NULL,
                                      FOREIGN KEY (contacted_by) REFERENCES users(id) ON DELETE SET NULL,
                                      INDEX idx_communication_customer_id (customer_id),
                                      INDEX idx_communication_appointment_id (appointment_id),
                                      INDEX idx_communication_date (communication_date),
                                      INDEX idx_communication_follow_up (follow_up_required, follow_up_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Customer communication log';


-- VehicleServiceHistory Table
CREATE TABLE IF NOT EXISTS vehicle_service_history (
                                       service_history_id INT NOT NULL AUTO_INCREMENT,
                                       vin VARCHAR(17) NOT NULL,
                                       appointment_id INT NOT NULL,
                                       service_date DATETIME NOT NULL,
                                       mileage INT NULL,
                                       service_summary VARCHAR(1024) NOT NULL,
                                       total_cost DECIMAL(10,2) NOT NULL,
                                       performed_by INT NULL,
                                       created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       PRIMARY KEY (service_history_id),
                                       FOREIGN KEY (vin) REFERENCES vehicle(vin) ON DELETE CASCADE,
                                       FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE CASCADE,
                                       FOREIGN KEY (performed_by) REFERENCES employee(employee_id) ON DELETE SET NULL,
                                       INDEX idx_service_history_vin (vin),
                                       INDEX idx_service_history_appointment_id (appointment_id),
                                       INDEX idx_service_history_date (service_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Complete service history per vehicle (CARFAX-style)';


-- PurchaseOrder Table
CREATE TABLE IF NOT EXISTS purchase_order (
                              purchase_order_id INT NOT NULL AUTO_INCREMENT,
                              po_number VARCHAR(50) NOT NULL,
                              supplier_id INT NOT NULL,
                              order_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              expected_delivery DATE NULL,
                              actual_delivery DATE NULL,
                              status ENUM('Draft', 'Submitted', 'Confirmed', 'Shipped', 'Received', 'Cancelled') NOT NULL,
                              subtotal DECIMAL(10,2) NOT NULL,
                              shipping_cost DECIMAL(10,2) NULL DEFAULT 0,
                              tax DECIMAL(10,2) NULL DEFAULT 0,
                              total_amount DECIMAL(10,2) NOT NULL,
                              ordered_by INT NOT NULL,
                              notes VARCHAR(1024) NULL,
                              created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                              PRIMARY KEY (purchase_order_id),
                              UNIQUE KEY uk_purchase_order_po_number (po_number),
                              FOREIGN KEY (supplier_id) REFERENCES supplier(supplier_id) ON DELETE RESTRICT,
                              FOREIGN KEY (ordered_by) REFERENCES users(id) ON DELETE RESTRICT,
                              INDEX idx_purchase_order_supplier_id (supplier_id),
                              INDEX idx_purchase_order_status (status),
                              INDEX idx_purchase_order_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Purchase orders';


-- PurchaseOrderItem Table
CREATE TABLE IF NOT EXISTS purchase_order_item (
                                   po_item_id INT NOT NULL AUTO_INCREMENT,
                                   purchase_order_id INT NOT NULL,
                                   part_number VARCHAR(100) NOT NULL,
                                   description VARCHAR(500) NOT NULL,
                                   quantity_ordered INT NOT NULL,
                                   quantity_received INT NOT NULL DEFAULT 0,
                                   unit_cost DECIMAL(10,2) NOT NULL,
                                   total_cost DECIMAL(10,2) NOT NULL,

                                   PRIMARY KEY (po_item_id),
                                   FOREIGN KEY (purchase_order_id) REFERENCES purchase_order(purchase_order_id) ON DELETE CASCADE,
                                   INDEX idx_po_item_purchase_order_id (purchase_order_id),
                                   INDEX idx_po_item_part_number (part_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Purchase order items';


-- AppointmentReminder Table
CREATE TABLE IF NOT EXISTS appointment_reminder (
                                    reminder_id INT NOT NULL AUTO_INCREMENT,
                                    appointment_id INT NOT NULL,
                                    reminder_type ENUM('Email', 'SMS', 'Phone_Call') NOT NULL,
                                    scheduled_date DATETIME NOT NULL,
                                    sent_date DATETIME NULL,
                                    status ENUM('Scheduled', 'Sent', 'Failed', 'Cancelled') NOT NULL,
                                    message TEXT NULL,
                                    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    PRIMARY KEY (reminder_id),
                                    FOREIGN KEY (appointment_id) REFERENCES service_appointment(appointment_id) ON DELETE CASCADE,
                                    INDEX idx_reminder_appointment_id (appointment_id),
                                    INDEX idx_reminder_status (status),
                                    INDEX idx_reminder_scheduled_date (scheduled_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Appointment reminders';


-- =====================================================
-- PART 3: CUSTOMER PORTAL INVITATION SYSTEM
-- (For future implementation - receptionist invites customers)
-- =====================================================

CREATE TABLE IF NOT EXISTS customer_invitations (
                                    id INT AUTO_INCREMENT PRIMARY KEY,
                                    customer_id INT NOT NULL,
                                    email VARCHAR(255) NOT NULL,
                                    invitation_token VARCHAR(255) NOT NULL UNIQUE,
                                    invited_by INT NOT NULL COMMENT 'user_id of receptionist who sent invite',
                                    invited_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    expires_at TIMESTAMP NOT NULL,
                                    is_used BOOLEAN DEFAULT FALSE,
                                    used_at TIMESTAMP NULL,

                                    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE CASCADE,
                                    FOREIGN KEY (invited_by) REFERENCES users(id) ON DELETE RESTRICT,

                                    INDEX idx_invitation_token (invitation_token),
                                    INDEX idx_invitation_customer (customer_id),
                                    INDEX idx_invitation_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Customer portal registration invitations (future feature)';







