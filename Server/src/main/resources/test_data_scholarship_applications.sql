
-- Application 2: Approved application for Research Grant (assuming program_id=2, period_id=2)
INSERT INTO scholarship_applications (
    applicant_id, 
    program_id, 
    period_id, 
    submission_date, 
    status, 
    decision_date,
    decision_comments,
    reviewer_id
) VALUES (
    3, -- User ID 3
    8, -- Assuming Research Grant has ID 2
    2, -- Assuming Spring 2025 has ID 2
    DATEADD(DAY, -30, GETDATE()), -- 30 days ago
    'APPROVED',
    DATEADD(DAY, -15, GETDATE()), -- 15 days ago
    'Excellent academic record and research proposal. The committee was impressed by your previous research experience and the clarity of your proposal.',
    1 -- Assuming an admin user has ID 1
);

-- Application 3: Rejected application for Excellence Award (assuming program_id=3, period_id=1)
INSERT INTO scholarship_applications (
    applicant_id, 
    program_id, 
    period_id, 
    submission_date, 
    status, 
    decision_date,
    decision_comments,
    reviewer_id
) VALUES (
    3, -- User ID 3
    5, -- Assuming Excellence Award has ID 3
    1, -- Assuming Fall 2024 has ID 1
    DATEADD(DAY, -45, GETDATE()), -- 45 days ago
    'REJECTED',
    DATEADD(DAY, -30, GETDATE()), -- 30 days ago
    'While your application was strong, we had many qualified applicants this year and could not accommodate all requests. We encourage you to apply again next semester.',
    2 -- Assuming another admin user has ID 2
);

