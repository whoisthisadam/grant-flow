# Scholarship Calculation System - Database Schema

## Overview

The database schema for the Scholarship Calculation System is designed to support both regular user (student) and administrative workflows. It includes entities for user management, academic records, scholarship programs, applications, and payment tracking.

## Entity Relationship Diagram

```
+----------------+       +-------------------+       +----------------+
| Users          |       | StudentProfiles   |       | CourseGrades   |
+----------------+       +-------------------+       +----------------+
| PK id          |<----->| PK id             |<----->| PK id          |
| username       |       | FK user_id        |       | FK student_id  |
| password_hash  |       | student_id        |       | FK course_id   |
| first_name     |       | date_of_birth     |       | FK period_id   |
| last_name      |       | enrollment_date   |       | grade_value    |
| email          |       | graduation_date   |       | grade_letter   |
| role           |       | current_gpa       |       | completion_date|
| pref_language  |       | major             |       | included_in_gpa|
| created_at     |       | department        |       | comments       |
| last_login     |       | academic_year     |       +----------------+
| is_active      |       +-------------------+              ^
+----------------+                                          |
       ^                                                    |
       |                                                    |
       |                                            +----------------+
       |                                            | Courses        |
       |                                            +----------------+
       |                                            | PK id          |
       |                                            | code           |
       |                                            | name           |
       |                                            | description    |
       |                                            | credits        |
       |                                            | department     |
       |                                            +----------------+
       |                                                    ^
       |                                                    |
       |                                                    |
+-------------------+                               +----------------+
| ScholarshipPrograms|                              | AcademicPeriods|
+-------------------+                               +----------------+
| PK id             |                               | PK id          |
| name              |                               | name           |
| description       |                               | start_date     |
| funding_amount    |                               | end_date       |
| min_gpa           |                               | type           |
| FK created_by     |                               +----------------+
| is_active         |                                       ^
| created_at        |                                       |
| application_deadline|                                     |
+-------------------+                                       |
       ^                                                    |
       |                                                    |
       |                                                    |
+-------------------+                               +----------------+
| ScholarshipApplications|                          | Payments       |
+-------------------+                               +----------------+
| PK id             |                               | PK id          |
| FK applicant_id   |                               | FK application_id|
| FK program_id     |                               | amount         |
| FK period_id      |<------------------------------>| payment_date   |
| submission_date   |                               | status         |
| status            |                               | reference_number|
| decision_date     |                               | FK approved_by |
| decision_comments |                               | approved_date  |
| FK reviewer_id    |                               | receipt_acknowledged|
+-------------------+                               +----------------+
```

## Tables Description

### Users
Stores information about all users of the system, both students and administrators.

| Column          | Type         | Constraints       | Description                           |
|-----------------|--------------|-------------------|---------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| username        | VARCHAR(50)  | NOT NULL, UNIQUE  | User's login name                     |
| password_hash   | VARCHAR(255) | NOT NULL          | Hashed password                       |
| first_name      | VARCHAR(100) | NOT NULL          | User's first name                     |
| last_name       | VARCHAR(100) | NOT NULL          | User's last name                      |
| email           | VARCHAR(255) | NOT NULL, UNIQUE  | User's email address                  |
| role            | VARCHAR(20)  | NOT NULL          | User role (STUDENT, ADMIN)            |
| pref_language   | VARCHAR(10)  | DEFAULT 'en'      | Preferred language (en, ru)           |
| created_at      | DATETIME     | NOT NULL          | Account creation timestamp            |
| last_login      | DATETIME     |                   | Last login timestamp                  |
| is_active       | BOOLEAN      | DEFAULT TRUE      | Whether the account is active         |

### StudentProfiles
Contains academic information for students.

| Column             | Type         | Constraints       | Description                           |
|--------------------|--------------|-------------------|---------------------------------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| user_id            | BIGINT       | FK, NOT NULL, UNIQUE | Reference to Users table          |
| student_id         | VARCHAR(20)  | NOT NULL, UNIQUE  | Student's official ID number          |
| date_of_birth      | DATE         |                   | Student's birth date                  |
| enrollment_date    | DATE         | NOT NULL          | Date of enrollment                    |
| graduation_date    | DATE         |                   | Expected graduation date              |
| current_gpa        | DECIMAL(4,2) |                   | Current Grade Point Average           |
| major              | VARCHAR(100) |                   | Student's major                       |
| department         | VARCHAR(100) |                   | Department name                       |
| academic_year      | INT          |                   | Current academic year                 |

### Courses
Stores information about academic courses.

| Column          | Type         | Constraints       | Description                           |
|-----------------|--------------|-------------------|---------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| code            | VARCHAR(20)  | NOT NULL, UNIQUE  | Course code                           |
| name            | VARCHAR(100) | NOT NULL          | Course name                           |
| description     | VARCHAR(500) |                   | Course description                    |
| credits         | INT          | NOT NULL          | Number of credits                     |
| department      | VARCHAR(100) |                   | Department offering the course        |

### AcademicPeriods
Represents academic terms (semesters, years).

| Column          | Type         | Constraints       | Description                           |
|-----------------|--------------|-------------------|---------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| name            | VARCHAR(50)  | NOT NULL, UNIQUE  | Period name (e.g., "Fall 2025")       |
| start_date      | DATE         | NOT NULL          | Period start date                     |
| end_date        | DATE         | NOT NULL          | Period end date                       |
| type            | VARCHAR(20)  | NOT NULL          | Period type (SEMESTER, YEAR)          |

### CourseGrades
Stores grades received by students for courses.

| Column          | Type         | Constraints       | Description                           |
|-----------------|--------------|-------------------|---------------------------------------|
| id              | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| student_id      | BIGINT       | FK, NOT NULL      | Reference to StudentProfiles table    |
| course_id       | BIGINT       | FK, NOT NULL      | Reference to Courses table            |
| period_id       | BIGINT       | FK, NOT NULL      | Reference to AcademicPeriods table    |
| grade_value     | DECIMAL(4,2) | NOT NULL          | Numeric grade value                   |
| grade_letter    | VARCHAR(2)   |                   | Letter grade (A, B, C, etc.)          |
| completion_date | DATE         |                   | Date when course was completed        |
| included_in_gpa | BOOLEAN      | DEFAULT TRUE      | Whether included in GPA calculation   |
| comments        | VARCHAR(500) |                   | Comments about the grade              |

### ScholarshipPrograms
Contains information about available scholarship programs.

| Column              | Type         | Constraints       | Description                           |
|---------------------|--------------|-------------------|---------------------------------------|
| id                  | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| name                | VARCHAR(100) | NOT NULL          | Program name                          |
| description         | VARCHAR(500) |                   | Program description                   |
| funding_amount      | DECIMAL(10,2)| NOT NULL          | Total funding available               |
| min_gpa             | DECIMAL(4,2) |                   | Minimum GPA required                  |
| created_by          | BIGINT       | FK, NOT NULL      | Reference to Users table (admin)      |
| is_active           | BOOLEAN      | DEFAULT TRUE      | Whether program is active             |
| created_at          | DATETIME     | NOT NULL          | Creation timestamp                    |
| application_deadline| DATE         |                   | Deadline for applications             |

### ScholarshipApplications
Tracks applications submitted by students for scholarships.

| Column             | Type         | Constraints       | Description                           |
|--------------------|--------------|-------------------|---------------------------------------|
| id                 | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| applicant_id       | BIGINT       | FK, NOT NULL      | Reference to Users table (student)    |
| program_id         | BIGINT       | FK, NOT NULL      | Reference to ScholarshipPrograms table|
| period_id          | BIGINT       | FK, NOT NULL      | Reference to AcademicPeriods table    |
| submission_date    | DATETIME     | NOT NULL          | Application submission timestamp      |
| status             | VARCHAR(20)  | NOT NULL          | Status (PENDING, APPROVED, REJECTED)  |
| decision_date      | DATETIME     |                   | Decision timestamp                    |
| decision_comments  | VARCHAR(500) |                   | Comments about the decision           |
| reviewer_id        | BIGINT       | FK                | Reference to Users table (admin)      |

### Payments
Records scholarship payments to students.

| Column              | Type         | Constraints       | Description                           |
|---------------------|--------------|-------------------|---------------------------------------|
| id                  | BIGINT       | PK, AUTO_INCREMENT| Unique identifier                     |
| application_id      | BIGINT       | FK, NOT NULL      | Reference to ScholarshipApplications  |
| amount              | DECIMAL(10,2)| NOT NULL          | Payment amount                        |
| payment_date        | DATETIME     | NOT NULL          | Payment timestamp                     |
| status              | VARCHAR(20)  | NOT NULL          | Status (PENDING, PROCESSED, FAILED)   |
| reference_number    | VARCHAR(50)  | UNIQUE            | Payment reference number              |
| approved_by         | BIGINT       | FK                | Reference to Users table (admin)      |
| approved_date       | DATETIME     |                   | Approval timestamp                    |
| receipt_acknowledged| BOOLEAN      | DEFAULT FALSE     | Whether receipt was acknowledged      |

## Relationships

1. **Users to StudentProfiles**: One-to-One (Only users with STUDENT role have profiles)
2. **StudentProfiles to CourseGrades**: One-to-Many (A student can have many course grades)
3. **Courses to CourseGrades**: One-to-Many (A course can have many grades from different students)
4. **AcademicPeriods to CourseGrades**: One-to-Many (Grades are associated with academic periods)
5. **Users to ScholarshipPrograms**: One-to-Many (Admins create scholarship programs)
6. **Users to ScholarshipApplications**: One-to-Many (Students submit applications)
7. **ScholarshipPrograms to ScholarshipApplications**: One-to-Many (Programs receive applications)
8. **AcademicPeriods to ScholarshipApplications**: One-to-Many (Applications are for specific periods)
9. **Users to ScholarshipApplications**: One-to-Many (Admins review applications)
10. **ScholarshipApplications to Payments**: One-to-Many (Approved applications lead to payments)
11. **Users to Payments**: One-to-Many (Admins approve payments)

## Indexes

1. Index on Users(username) for login lookups
2. Index on Users(email) for email lookups
3. Index on StudentProfiles(student_id) for student ID lookups
4. Index on CourseGrades(student_id, period_id) for retrieving grades by student and period
5. Index on ScholarshipApplications(applicant_id, status) for retrieving applications by student and status
6. Index on ScholarshipApplications(program_id, status) for retrieving applications by program and status
7. Index on Payments(application_id) for retrieving payments by application

## Constraints

1. Foreign key constraints on all relationships to maintain referential integrity
2. Unique constraints on username, email, and student_id to prevent duplicates
3. Check constraints on status fields to ensure valid values
4. Not null constraints on required fields
