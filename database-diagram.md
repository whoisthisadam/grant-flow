# Scholarship Calculation System - Database Diagram

## Entity Relationship Diagram

```mermaid
erDiagram
    Users ||--o| StudentProfiles : has
    Users ||--o{ ScholarshipPrograms : creates
    Users ||--o{ ScholarshipApplications : submits
    Users ||--o{ ScholarshipApplications : reviews
    Users ||--o{ Payments : approves
    
    StudentProfiles ||--o{ CourseGrades : receives
    Courses ||--o{ CourseGrades : assigned_to
    AcademicPeriods ||--o{ CourseGrades : taken_in
    
    ScholarshipPrograms ||--o{ ScholarshipApplications : receives
    AcademicPeriods ||--o{ ScholarshipApplications : applies_for
    
    ScholarshipApplications ||--o{ Payments : generates
    
    Users {
        bigint id PK
        varchar username UK
        varchar password_hash
        varchar first_name
        varchar last_name
        varchar email UK
        varchar role
        varchar pref_language
        datetime created_at
        datetime last_login
        boolean is_active
    }
    
    StudentProfiles {
        bigint id PK
        bigint user_id FK,UK
        varchar student_id UK
        date date_of_birth
        date enrollment_date
        date graduation_date
        decimal current_gpa
        varchar major
        varchar department
        int academic_year
    }
    
    Courses {
        bigint id PK
        varchar code UK
        varchar name
        varchar description
        int credits
        varchar department
    }
    
    AcademicPeriods {
        bigint id PK
        varchar name UK
        date start_date
        date end_date
        varchar type
    }
    
    CourseGrades {
        bigint id PK
        bigint student_id FK
        bigint course_id FK
        bigint period_id FK
        decimal grade_value
        varchar grade_letter
        date completion_date
        boolean included_in_gpa
        varchar comments
    }
    
    ScholarshipPrograms {
        bigint id PK
        varchar name
        varchar description
        decimal funding_amount
        decimal min_gpa
        bigint created_by FK
        boolean is_active
        datetime created_at
        date application_deadline
    }
    
    ScholarshipApplications {
        bigint id PK
        bigint applicant_id FK
        bigint program_id FK
        bigint period_id FK
        datetime submission_date
        varchar status
        datetime decision_date
        varchar decision_comments
        bigint reviewer_id FK
    }
    
    Payments {
        bigint id PK
        bigint application_id FK
        decimal amount
        datetime payment_date
        varchar status
        varchar reference_number UK
        bigint approved_by FK
        datetime approved_date
        boolean receipt_acknowledged
    }
```

## Legend

- PK: Primary Key
- FK: Foreign Key
- UK: Unique Key

## Relationship Types

- `||--o|`: One-to-One relationship
- `||--o{`: One-to-Many relationship
- `}o--o{`: Many-to-Many relationship

## Key Relationships

1. A User can have one StudentProfile (if they are a student)
2. A StudentProfile belongs to exactly one User
3. A StudentProfile can have many CourseGrades
4. A Course can be associated with many CourseGrades
5. An AcademicPeriod can have many CourseGrades
6. An Admin User can create many ScholarshipPrograms
7. A Student User can submit many ScholarshipApplications
8. A ScholarshipProgram can receive many ScholarshipApplications
9. An AcademicPeriod can have many ScholarshipApplications
10. An Admin User can review many ScholarshipApplications
11. A ScholarshipApplication can generate many Payments
12. An Admin User can approve many Payments

This diagram provides a visual representation of the database schema described in the database-schema.md file.
