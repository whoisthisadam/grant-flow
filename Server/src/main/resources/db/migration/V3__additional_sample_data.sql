-- Additional sample data for testing the scholarship application flow

-- Insert additional academic periods
INSERT INTO academic_periods (name, start_date, end_date, type)
VALUES 
('Fall 2024', '2024-09-01', '2024-12-31', 'SEMESTER'),
('Spring 2025', '2025-01-15', '2025-05-15', 'SEMESTER'),
('Summer 2025', '2025-06-01', '2025-08-15', 'SEMESTER'),
('Academic Year 2024-2025', '2024-09-01', '2025-05-15', 'YEAR'),
('Academic Year 2025-2026', '2025-09-01', '2026-05-15', 'YEAR');

-- Insert additional scholarship programs with future deadlines (accepting applications)
INSERT INTO scholarship_programs (name, description, funding_amount, min_gpa, created_by, is_active, created_at, application_deadline)
VALUES 
('Computer Science Excellence Award', 'For outstanding students in Computer Science and related fields. Applicants must demonstrate exceptional programming skills and academic achievement.', 8000.00, 3.5, 1, 1, GETDATE(), '2025-06-30'),
('Engineering Innovation Scholarship', 'Supporting innovative engineering students who have demonstrated creativity and problem-solving abilities in their coursework or projects.', 6500.00, 3.2, 1, 1, GETDATE(), '2025-07-15'),
('Global Leadership Scholarship', 'For students with exceptional leadership qualities and international experience. Preference given to those involved in community service.', 5000.00, 3.0, 7, 1, GETDATE(), '2025-08-01'),
('Science Research Grant', 'Supporting students conducting research in biology, chemistry, physics, or related fields. Must include research proposal in application.', 7000.00, 3.3, 7, 1, GETDATE(), '2025-06-15'),
('First-Generation Student Scholarship', 'For high-achieving students who are the first in their family to attend college. Demonstrates financial need and academic promise.', 9000.00, 3.0, 1, 1, GETDATE(), '2025-07-01'),
('Women in STEM Scholarship', 'Encouraging women to pursue careers in Science, Technology, Engineering, and Mathematics fields. Applicants must demonstrate academic excellence and career goals.', 7500.00, 3.4, 7, 1, GETDATE(), '2025-08-15'),
('Diversity in Computing Scholarship', 'Promoting diversity in computer science and information technology fields. Open to underrepresented students with strong academic records.', 6000.00, 3.2, 1, 1, GETDATE(), '2025-07-30'),
('Sustainable Development Award', 'For students pursuing studies related to environmental science, sustainability, or renewable energy. Must demonstrate commitment to environmental causes.', 5500.00, 3.1, 7, 1, GETDATE(), '2025-06-20');

-- Insert additional scholarship programs that are not accepting applications (past deadlines or inactive)
INSERT INTO scholarship_programs (name, description, funding_amount, min_gpa, created_by, is_active, created_at, application_deadline)
VALUES 
('Summer Research Fellowship', 'Supporting summer research projects in any academic discipline. Requires faculty recommendation.', 4000.00, 3.5, 1, 1, GETDATE(), '2024-04-30'),
('International Student Grant', 'Financial assistance for international students with demonstrated need and academic excellence.', 5500.00, 3.3, 7, 0, GETDATE(), '2025-06-15'),
('Performing Arts Scholarship', 'For students with exceptional talent in music, theater, dance, or other performing arts.', 4500.00, 3.0, 1, 1, GETDATE(), '2024-05-15');

-- Insert sample applications for existing students
-- Note: These are in addition to the applications already in V2__sample_data.sql
INSERT INTO scholarship_applications (applicant_id, program_id, period_id, submission_date, status, decision_date, decision_comments, reviewer_id)
VALUES 
-- Approved applications for new programs
(2, 5, 7, DATEADD(day, -45, GETDATE()), 'APPROVED', DATEADD(day, -30, GETDATE()), 'Excellent leadership qualities and community involvement', 1),
(3, 6, 7, DATEADD(day, -40, GETDATE()), 'APPROVED', DATEADD(day, -25, GETDATE()), 'Outstanding research proposal and academic record', 7),

-- Pending applications for new programs
(4, 7, 7, DATEADD(day, -15, GETDATE()), 'PENDING', NULL, NULL, NULL),
(5, 8, 7, DATEADD(day, -10, GETDATE()), 'PENDING', NULL, NULL, NULL),
(6, 9, 7, DATEADD(day, -5, GETDATE()), 'PENDING', NULL, NULL, NULL),

-- Rejected applications for new programs
(2, 10, 7, DATEADD(day, -30, GETDATE()), 'REJECTED', DATEADD(day, -20, GETDATE()), 'Application did not meet minimum requirements for international student support', 1),
(5, 11, 7, DATEADD(day, -25, GETDATE()), 'REJECTED', DATEADD(day, -15, GETDATE()), 'Limited evidence of performing arts experience', 7);

-- Insert payments for newly approved applications
INSERT INTO payments (application_id, amount, payment_date, status, reference_number, approved_by, approved_date, receipt_acknowledged)
VALUES 
(7, 5000.00, DATEADD(day, -25, GETDATE()), 'PROCESSED', 'SCH-20240520-3456', 1, DATEADD(day, -25, GETDATE()), 1),
(8, 7000.00, DATEADD(day, -20, GETDATE()), 'PROCESSED', 'SCH-20240525-7890', 7, DATEADD(day, -20, GETDATE()), 0);
