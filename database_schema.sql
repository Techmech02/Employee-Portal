-- ============================================
--  Employee Portal - MySQL Schema
--  Run this in MySQL Workbench or CLI
-- ============================================

CREATE DATABASE IF NOT EXISTS employee_portal;
USE employee_portal;

-- Core employee table
CREATE TABLE IF NOT EXISTS employees (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employee_id VARCHAR(50)  NOT NULL UNIQUE,
    name        VARCHAR(100) NOT NULL,
    fathers_name VARCHAR(100),
    email       VARCHAR(150) NOT NULL UNIQUE,
    gender      ENUM('Male','Female','Other') NOT NULL,
    contact     VARCHAR(20),
    image_path  VARCHAR(300),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Dynamic/custom fields per employee
CREATE TABLE IF NOT EXISTS employee_custom_fields (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    employee_id INT NOT NULL,
    field_label VARCHAR(100) NOT NULL,
    field_value TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);
