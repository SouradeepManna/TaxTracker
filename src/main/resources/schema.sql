-- ===========================================================
-- TaxTracker - MySQL DDL Schema
-- ===========================================================

DROP TABLE IF EXISTS pending_registration;
DROP TABLE IF EXISTS form_90c_transaction;
DROP TABLE IF EXISTS form_90c;
DROP TABLE IF EXISTS uploaded_document;
DROP TABLE IF EXISTS transaction_details;
DROP TABLE IF EXISTS user_details;

CREATE TABLE user_details (
    user_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    address_line1  VARCHAR(200),
    address_line2  VARCHAR(200),
    area           VARCHAR(100),
    city           VARCHAR(100),
    state          VARCHAR(100),
    pin            VARCHAR(6),
    mob_number     VARCHAR(10)
);

CREATE TABLE transaction_details (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    email              VARCHAR(150) NOT NULL,
    txn_date           DATE NOT NULL,
    amount             DECIMAL(15,2) NOT NULL,
    tax_amount         DECIMAL(15,2) NOT NULL,
    type               VARCHAR(20) NOT NULL,
    organization_name  VARCHAR(150) NOT NULL,
    financial_year     VARCHAR(9) NOT NULL,
    txn_month          VARCHAR(2),
    INDEX idx_txn_email (email),
    INDEX idx_txn_fy (financial_year)
);

CREATE TABLE form_90c (
    form_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(150) NOT NULL,
    name            VARCHAR(150) NOT NULL,
    mobile_number   VARCHAR(10) NOT NULL,
    financial_year  VARCHAR(9),
    status          VARCHAR(20) NOT NULL,
    document_name   VARCHAR(255),
    created_at      DATETIME,
    INDEX idx_form_email (email)
);

CREATE TABLE form_90c_transaction (
    row_id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_name  VARCHAR(150) NOT NULL,
    amount             DECIMAL(15,2) NOT NULL,
    tax_amount         DECIMAL(15,2) NOT NULL,
    type               VARCHAR(20) NOT NULL,
    form_id            BIGINT,
    CONSTRAINT fk_form90c_txn FOREIGN KEY (form_id) REFERENCES form_90c(form_id) ON DELETE CASCADE
);

CREATE TABLE uploaded_document (
    document_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(150),
    file_name     VARCHAR(255) NOT NULL,
    content_type  VARCHAR(100),
    file_size     BIGINT,
    data          LONGBLOB,
    uploaded_at   DATETIME
);

CREATE TABLE pending_registration (
    pending_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    email          VARCHAR(150) NOT NULL UNIQUE,
    password       VARCHAR(255) NOT NULL,
    mob_number     VARCHAR(10),
    address_line1  VARCHAR(200),
    address_line2  VARCHAR(200),
    area           VARCHAR(100),
    city           VARCHAR(100),
    state          VARCHAR(100),
    pin            VARCHAR(6),
    otp            VARCHAR(6) NOT NULL,
    otp_expiry     DATETIME NOT NULL
);
