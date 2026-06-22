-- Sihaniwala Foundation Database Schema
-- PostgreSQL 15+

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS uploaded_documents CASCADE;
DROP TABLE IF EXISTS donations CASCADE;
DROP TABLE IF EXISTS initiative_applications CASCADE;
DROP TABLE IF EXISTS gallery_images CASCADE;
DROP TABLE IF EXISTS contact_messages CASCADE;
DROP TABLE IF EXISTS volunteers CASCADE;
DROP TABLE IF EXISTS projects CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Users Table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_role ON users(role);

-- Donations Table
CREATE TABLE donations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    donor_name VARCHAR(255),
    donor_email VARCHAR(255),
    amount DECIMAL(12,2) NOT NULL,
    currency VARCHAR(10) DEFAULT 'INR',
    initiative VARCHAR(100),
    anonymous BOOLEAN DEFAULT FALSE,
    recurring BOOLEAN DEFAULT FALSE,
    payment_gateway VARCHAR(20) CHECK (payment_gateway IN ('RAZORPAY', 'PAYPAL')),
    payment_id VARCHAR(255),
    order_id VARCHAR(255),
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'SUCCESS', 'FAILED', 'REFUNDED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_donations_user ON donations(user_id);
CREATE INDEX idx_donations_status ON donations(status);
CREATE INDEX idx_donations_gateway ON donations(payment_gateway);
CREATE INDEX idx_donations_order ON donations(order_id);

-- Initiative Applications Table
CREATE TABLE initiative_applications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    initiative_type VARCHAR(30) NOT NULL CHECK (initiative_type IN ('HOSPITAL', 'MARRIAGE_SUPPORT', 'WATER_SPRAY', 'EDUCATION_BPL', 'FINANCIAL_HELP', 'SCHOOL_ADOPTION')),
    form_data TEXT,
    status VARCHAR(20) DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED')),
    admin_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE INDEX idx_applications_user ON initiative_applications(user_id);
CREATE INDEX idx_applications_status ON initiative_applications(status);
CREATE INDEX idx_applications_type ON initiative_applications(initiative_type);

-- Volunteers Table
CREATE TABLE volunteers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    city VARCHAR(100),
    interest VARCHAR(255),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Contact Messages Table
CREATE TABLE contact_messages (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    subject VARCHAR(255),
    message TEXT NOT NULL,
    read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Gallery Images Table
CREATE TABLE gallery_images (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255),
    category VARCHAR(100),
    file_path VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_gallery_category ON gallery_images(category);

-- Projects Table
CREATE TABLE projects (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    location VARCHAR(255),
    status VARCHAR(20) DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'COMPLETED', 'UPCOMING', 'ARCHIVED')),
    beneficiaries INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Uploaded Documents Table
CREATE TABLE uploaded_documents (
    id BIGSERIAL PRIMARY KEY,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    application_id BIGINT REFERENCES initiative_applications(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_documents_user ON uploaded_documents(user_id);
CREATE INDEX idx_documents_application ON uploaded_documents(application_id);

-- ============================================
-- SEED DATA
-- ============================================

-- Admin user (password: admin123 - BCrypt encoded)
INSERT INTO users (name, email, password, phone, role, enabled) VALUES
('Admin', 'admin@sihaniwala.org', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876543210', 'ADMIN', true),
('Demo User', 'user@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '+91-9876543211', 'USER', true);

-- Sample Projects
INSERT INTO projects (title, description, location, status, beneficiaries) VALUES
('Village School Renovation', 'Complete renovation of primary school building with modern facilities', 'Rajasthan, India', 'ACTIVE', 250),
('Clean Water Initiative', 'Installing water purification systems in 10 villages', 'Gujarat, India', 'ACTIVE', 5000),
('Women Empowerment Center', 'Skill development center for underprivileged women', 'Madhya Pradesh, India', 'COMPLETED', 150),
('Mobile Health Clinic', 'Healthcare delivery to remote villages via mobile clinic vans', 'Uttar Pradesh, India', 'ACTIVE', 10000);

-- Sample Gallery Images
INSERT INTO gallery_images (title, category, file_path, file_name) VALUES
('School Opening Ceremony', 'education', 'gallery/school-opening.jpg', 'school-opening.jpg'),
('Water Well Installation', 'water', 'gallery/water-well.jpg', 'water-well.jpg'),
('Medical Camp', 'health', 'gallery/medical-camp.jpg', 'medical-camp.jpg'),
('Marriage Support Event', 'marriage', 'gallery/marriage-event.jpg', 'marriage-event.jpg');
