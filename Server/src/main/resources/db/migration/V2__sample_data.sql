-- Sample data for testing and development

-- Insert sample student users (password: student123)
INSERT INTO users (username, password_hash, first_name, last_name, email, role, created_at, is_active)
VALUES 
('student1', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZ.X5L6/QxHR2jUJIWJnhMtXFePC', 'John', 'Smith', 'john.smith@example.com', 'STUDENT', GETDATE(), 1),
('student2', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZ.X5L6/QxHR2jUJIWJnhMtXFePC', 'Emma', 'Johnson', 'emma.johnson@example.com', 'STUDENT', GETDATE(), 1),
('student3', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZ.X5L6/QxHR2jUJIWJnhMtXFePC', 'Michael', 'Williams', 'michael.williams@example.com', 'STUDENT', GETDATE(), 1),
('student4', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZ.X5L6/QxHR2jUJIWJnhMtXFePC', 'Sophia', 'Brown', 'sophia.brown@example.com', 'STUDENT', GETDATE(), 1),
('student5', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZ.X5L6/QxHR2jUJIWJnhMtXFePC', 'Daniel', 'Jones', 'daniel.jones@example.com', 'STUDENT', GETDATE(), 1);

-- Insert additional admin user (password: admin123)
INSERT INTO users (username, password_hash, first_name, last_name, email, role, created_at, is_active)
VALUES 
('advisor', '$2a$10$hKDVYxLefVHV/vtuPhWD3OigtRyOykRLDdUAp80Z1crSoS1lFqaFS', 'Academic', 'Advisor', 'advisor@example.com', 'ADMIN', GETDATE(), 1);

-- Insert student profiles
INSERT INTO student_profiles (user_id, student_id, date_of_birth, enrollment_date, graduation_date, current_gpa, major, department, academic_year)
VALUES 
(2, 'S20230001', '2000-05-15', '2023-09-01', '2027-05-15', 3.8, 'Computer Science', 'Engineering', 1),
(3, 'S20230002', '2001-02-20', '2023-09-01', '2027-05-15', 3.5, 'Mathematics', 'Science', 1),
(4, 'S20230003', '2000-11-10', '2023-09-01', '2027-05-15', 3.9, 'Physics', 'Science', 1),
(5, 'S20230004', '2001-07-25', '2023-09-01', '2027-05-15', 3.2, 'English Literature', 'Humanities', 1),
(6, 'S20230005', '2000-09-30', '2023-09-01', '2027-05-15', 3.7, 'Biology', 'Science', 1);

-- Insert additional courses
INSERT INTO courses (code, name, description, credits, department)
VALUES 
('CS201', 'Data Structures and Algorithms', 'Advanced data structures and algorithm analysis', 4, 'Computer Science'),
('CS301', 'Database Systems', 'Design and implementation of database systems', 3, 'Computer Science'),
('MATH301', 'Linear Algebra', 'Vector spaces, matrices, and linear transformations', 3, 'Mathematics'),
('PHYS250', 'Physics II', 'Electricity, magnetism, and optics', 4, 'Physics'),
('BIO220', 'Molecular Biology', 'Structure and function of macromolecules', 4, 'Biology');

-- Insert course grades for students
-- Student 1 (John Smith)
INSERT INTO course_grades (student_id, course_id, period_id, grade_value, grade_letter, completion_date, included_in_gpa)
VALUES 
(1, 1, 1, 4.0, 'A', '2023-12-15', 1),  -- CS101, Fall 2023
(1, 2, 1, 3.7, 'A-', '2023-12-15', 1), -- MATH201, Fall 2023
(1, 3, 1, 3.3, 'B+', '2023-12-15', 1), -- ENG110, Fall 2023
(1, 6, 2, 4.0, 'A', '2024-05-10', 1),  -- CS201, Spring 2024
(1, 7, 2, 3.7, 'A-', '2024-05-10', 1); -- CS301, Spring 2024

-- Student 2 (Emma Johnson)
INSERT INTO course_grades (student_id, course_id, period_id, grade_value, grade_letter, completion_date, included_in_gpa)
VALUES 
(2, 1, 1, 3.7, 'A-', '2023-12-15', 1),  -- CS101, Fall 2023
(2, 2, 1, 3.3, 'B+', '2023-12-15', 1),  -- MATH201, Fall 2023
(2, 4, 1, 3.0, 'B', '2023-12-15', 1),   -- PHYS150, Fall 2023
(2, 8, 2, 3.7, 'A-', '2024-05-10', 1),  -- MATH301, Spring 2024
(2, 9, 2, 3.3, 'B+', '2024-05-10', 1);  -- PHYS250, Spring 2024

-- Student 3 (Michael Williams)
INSERT INTO course_grades (student_id, course_id, period_id, grade_value, grade_letter, completion_date, included_in_gpa)
VALUES 
(3, 2, 1, 4.0, 'A', '2023-12-15', 1),   -- MATH201, Fall 2023
(3, 4, 1, 4.0, 'A', '2023-12-15', 1),   -- PHYS150, Fall 2023
(3, 5, 1, 3.7, 'A-', '2023-12-15', 1),  -- BIO120, Fall 2023
(3, 8, 2, 4.0, 'A', '2024-05-10', 1),   -- MATH301, Spring 2024
(3, 9, 2, 4.0, 'A', '2024-05-10', 1);   -- PHYS250, Spring 2024

-- Student 4 (Sophia Brown)
INSERT INTO course_grades (student_id, course_id, period_id, grade_value, grade_letter, completion_date, included_in_gpa)
VALUES 
(4, 3, 1, 3.7, 'A-', '2023-12-15', 1),  -- ENG110, Fall 2023
(4, 5, 1, 3.0, 'B', '2023-12-15', 1),   -- BIO120, Fall 2023
(4, 1, 1, 3.0, 'B', '2023-12-15', 1),   -- CS101, Fall 2023
(4, 3, 2, 3.3, 'B+', '2024-05-10', 1),  -- ENG110, Spring 2024
(4, 10, 2, 3.0, 'B', '2024-05-10', 1);  -- BIO220, Spring 2024

-- Student 5 (Daniel Jones)
INSERT INTO course_grades (student_id, course_id, period_id, grade_value, grade_letter, completion_date, included_in_gpa)
VALUES 
(5, 5, 1, 4.0, 'A', '2023-12-15', 1),   -- BIO120, Fall 2023
(5, 2, 1, 3.7, 'A-', '2023-12-15', 1),  -- MATH201, Fall 2023
(5, 3, 1, 3.3, 'B+', '2023-12-15', 1),  -- ENG110, Fall 2023
(5, 10, 2, 4.0, 'A', '2024-05-10', 1),  -- BIO220, Spring 2024
(5, 4, 2, 3.7, 'A-', '2024-05-10', 1);  -- PHYS150, Spring 2024

-- Insert additional scholarship programs
INSERT INTO scholarship_programs (name, description, funding_amount, min_gpa, created_by, is_active, created_at, application_deadline)
VALUES 
('STEM Excellence Scholarship', 'For outstanding students in Science, Technology, Engineering, and Mathematics', 7500.00, 3.7, 1, 1, GETDATE(), '2024-07-15'),
('Need-Based Grant', 'Financial assistance for students with demonstrated financial need', 3000.00, 2.5, 1, 1, GETDATE(), '2024-08-01'),
('Leadership Award', 'For students demonstrating exceptional leadership qualities', 2500.00, 3.0, 7, 1, GETDATE(), '2024-06-15'),
('Research Fellowship', 'Support for students engaged in research projects', 4000.00, 3.5, 7, 1, GETDATE(), '2024-07-01');

-- Insert scholarship applications
INSERT INTO scholarship_applications (applicant_id, program_id, period_id, submission_date, status, decision_date, decision_comments, reviewer_id)
VALUES 
-- Approved applications
(2, 1, 4, DATEADD(day, -30, GETDATE()), 'APPROVED', DATEADD(day, -20, GETDATE()), 'Excellent academic record', 1),
(4, 2, 4, DATEADD(day, -25, GETDATE()), 'APPROVED', DATEADD(day, -15, GETDATE()), 'Meets all criteria', 7),
(3, 2, 4, DATEADD(day, -28, GETDATE()), 'APPROVED', DATEADD(day, -18, GETDATE()), 'Outstanding performance in STEM subjects', 1),
-- Pending applications
(5, 3, 4, DATEADD(day, -10, GETDATE()), 'PENDING', NULL, NULL, NULL),
(6, 4, 4, DATEADD(day, -5, GETDATE()), 'PENDING', NULL, NULL, NULL),
-- Rejected application
(3, 4, 4, DATEADD(day, -20, GETDATE()), 'REJECTED', DATEADD(day, -10, GETDATE()), 'Limited research experience', 7);

-- Insert payments for approved applications
INSERT INTO payments (application_id, amount, payment_date, status, reference_number, approved_by, approved_date, receipt_acknowledged)
VALUES 
(1, 5000.00, DATEADD(day, -15, GETDATE()), 'PROCESSED', 'SCH-20240501-1234', 1, DATEADD(day, -15, GETDATE()), 1),
(2, 3000.00, DATEADD(day, -10, GETDATE()), 'PROCESSED', 'SCH-20240510-5678', 7, DATEADD(day, -10, GETDATE()), 0),
(3, 7500.00, DATEADD(day, -5, GETDATE()), 'PENDING', 'SCH-20240515-9012', NULL, NULL, 0);
