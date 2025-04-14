-- Initial database schema for Scholarship Calculation System

-- Create users table
CREATE TABLE users (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    pref_language VARCHAR(10) DEFAULT 'en',
    created_at DATETIME2 DEFAULT GETDATE(),
    last_login DATETIME2,
    is_active BIT DEFAULT 1
);

-- Create student_profiles table
CREATE TABLE student_profiles (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    student_id VARCHAR(50) NOT NULL UNIQUE,
    date_of_birth DATE,
    enrollment_date DATE NOT NULL,
    graduation_date DATE,
    current_gpa DECIMAL(4,2),
    major VARCHAR(100),
    department VARCHAR(100),
    academic_year INT,
    CONSTRAINT fk_student_profiles_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create academic_periods table
CREATE TABLE academic_periods (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL
);

-- Create courses table
CREATE TABLE courses (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    credits INT NOT NULL,
    department VARCHAR(100)
);

-- Create course_grades table
CREATE TABLE course_grades (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    student_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    grade_value DECIMAL(4,2) NOT NULL,
    grade_letter VARCHAR(2),
    completion_date DATE,
    included_in_gpa BIT DEFAULT 1,
    comments VARCHAR(500),
    CONSTRAINT fk_course_grades_student FOREIGN KEY (student_id) REFERENCES student_profiles(id),
    CONSTRAINT fk_course_grades_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_course_grades_period FOREIGN KEY (period_id) REFERENCES academic_periods(id),
    CONSTRAINT uq_course_grades UNIQUE (student_id, course_id, period_id)
);

-- Create scholarship_programs table
CREATE TABLE scholarship_programs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    funding_amount DECIMAL(10,2) NOT NULL,
    min_gpa DECIMAL(4,2),
    created_by BIGINT NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    application_deadline DATE,
    CONSTRAINT fk_scholarship_programs_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

-- Create scholarship_applications table
CREATE TABLE scholarship_applications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    applicant_id BIGINT NOT NULL,
    program_id BIGINT NOT NULL,
    period_id BIGINT NOT NULL,
    submission_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    decision_date DATETIME2,
    decision_comments VARCHAR(500),
    reviewer_id BIGINT,
    CONSTRAINT fk_scholarship_applications_applicant FOREIGN KEY (applicant_id) REFERENCES users(id),
    CONSTRAINT fk_scholarship_applications_program FOREIGN KEY (program_id) REFERENCES scholarship_programs(id),
    CONSTRAINT fk_scholarship_applications_period FOREIGN KEY (period_id) REFERENCES academic_periods(id),
    CONSTRAINT fk_scholarship_applications_reviewer FOREIGN KEY (reviewer_id) REFERENCES users(id)
);

-- Create payments table
CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    application_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME2 NOT NULL DEFAULT GETDATE(),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reference_number VARCHAR(50) UNIQUE,
    approved_by BIGINT,
    approved_date DATETIME2,
    receipt_acknowledged BIT DEFAULT 0,
    CONSTRAINT fk_payments_application FOREIGN KEY (application_id) REFERENCES scholarship_applications(id),
    CONSTRAINT fk_payments_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- Create indexes for better performance
CREATE INDEX idx_student_profiles_user_id ON student_profiles(user_id);
CREATE INDEX idx_course_grades_student_id ON course_grades(student_id);
CREATE INDEX idx_course_grades_course_id ON course_grades(course_id);
CREATE INDEX idx_course_grades_period_id ON course_grades(period_id);
CREATE INDEX idx_scholarship_applications_applicant_id ON scholarship_applications(applicant_id);
CREATE INDEX idx_scholarship_applications_program_id ON scholarship_applications(program_id);
CREATE INDEX idx_scholarship_applications_period_id ON scholarship_applications(period_id);
CREATE INDEX idx_payments_application_id ON payments(application_id);

-- Insert initial admin user (password: admin123)
INSERT INTO users (username, password_hash, first_name, last_name, email, role, created_at, is_active)
VALUES ('admin', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'System', 'Administrator', 'admin@example.com', 'ADMIN', GETDATE(), 1);

-- Insert initial academic periods
INSERT INTO academic_periods (name, start_date, end_date, type)
VALUES 
('Fall 2023', '2023-09-01', '2023-12-31', 'SEMESTER'),
('Spring 2024', '2024-01-15', '2024-05-15', 'SEMESTER'),
('Summer 2024', '2024-06-01', '2024-08-15', 'SEMESTER'),
('Academic Year 2023-2024', '2023-09-01', '2024-05-15', 'YEAR');

-- Insert sample courses
INSERT INTO courses (code, name, description, credits, department)
VALUES 
('CS101', 'Introduction to Computer Science', 'Fundamental concepts of programming and computer science', 3, 'Computer Science'),
('MATH201', 'Calculus I', 'Limits, derivatives, and integrals of algebraic and transcendental functions', 4, 'Mathematics'),
('ENG110', 'Academic Writing', 'Principles of effective writing and argumentation', 3, 'English'),
('PHYS150', 'Physics I', 'Mechanics, kinematics, and dynamics', 4, 'Physics'),
('BIO120', 'General Biology', 'Introduction to biological principles', 4, 'Biology');

-- Insert initial scholarship program
INSERT INTO scholarship_programs (name, description, funding_amount, min_gpa, created_by, is_active, created_at, application_deadline)
VALUES ('Merit Scholarship', 'Scholarship for academic excellence', 5000.00, 3.5, 1, 1, GETDATE(), '2024-06-30');
