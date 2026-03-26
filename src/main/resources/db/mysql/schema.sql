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
