# Scholarship Calculation System - User Flows

## Overview

The Scholarship Calculation System has two distinct user flows:

1. **Regular User Flow** - For students and academic staff who need to view, apply for, and track scholarships
2. **Administrator Flow** - For system administrators who manage the scholarship system, users, and configurations

This document outlines the key processes and use cases for each user type.

## Regular User Flow

Regular users include students and academic staff who interact with the scholarship system to view available scholarships, apply for them, and track their status.

### Key Features for Regular Users:

- View available scholarships and eligibility criteria
- Submit scholarship applications
- Track application status
- View personal academic records
- Receive notifications about scholarship decisions
- Access reports on scholarship disbursements
- Switch language preferences (English/Russian)

### Regular User Use Cases:

1. **UC-R1: User Registration and Profile Creation**
   - Description: New users register in the system and create their academic profiles
   - Primary Actor: Student
   - Flow:
     1. User navigates to registration page
     2. User enters personal and academic information
     3. System validates the information
     4. User confirms email address
     5. System creates user profile
   - Postcondition: New user account is created and ready for use

2. **UC-R2: Scholarship Application Submission**
   - Description: User applies for an available scholarship
   - Primary Actor: Student
   - Flow:
     1. User logs into the system
     2. User browses available scholarships
     3. User selects a scholarship to apply for
     4. System checks preliminary eligibility
     5. User completes application form
     6. User uploads required documents
     7. System validates application
     8. User submits application
   - Postcondition: Scholarship application is submitted and pending review

3. **UC-R3: Academic Record Update**
   - Description: User updates their academic records with new grades and achievements
   - Primary Actor: Student
   - Flow:
     1. User logs into the system
     2. User navigates to academic record section
     3. User adds new course grades or academic achievements
     4. System calculates updated GPA
     5. User reviews and confirms updates
   - Postcondition: Academic record is updated, potentially affecting scholarship eligibility

4. **UC-R4: Scholarship Status Tracking**
   - Description: User checks the status of submitted scholarship applications
   - Primary Actor: Student
   - Flow:
     1. User logs into the system
     2. User navigates to application status dashboard
     3. System displays all applications with current status
     4. User selects an application to view details
     5. System shows detailed status information and any reviewer comments
   - Postcondition: User is informed about current application status

5. **UC-R5: Scholarship Payment Receipt**
   - Description: User receives and acknowledges scholarship payment
   - Primary Actor: Student
   - Flow:
     1. System notifies user of approved payment
     2. User logs into the system
     3. User views payment details
     4. User acknowledges receipt of payment
     5. System records acknowledgment
   - Postcondition: Payment receipt is recorded in the system

6. **UC-R6: Language Preference Change**
   - Description: User changes the interface language
   - Primary Actor: Student/Staff
   - Flow:
     1. User selects language option from interface
     2. System updates UI language immediately
     3. System stores language preference for future sessions
   - Postcondition: UI displays in selected language and preference is saved

7. **UC-R7: Academic Performance Report Generation**
   - Description: User generates a report of their academic performance and scholarship history
   - Primary Actor: Student
   - Flow:
     1. User navigates to reports section
     2. User selects report type and parameters
     3. System generates the report
     4. User views or downloads the report
   - Postcondition: User has access to requested academic and scholarship information

## Administrator Flow

Administrators manage the scholarship system, including user management, scholarship program configuration, and system monitoring.

### Key Features for Administrators:

- Manage user accounts and permissions
- Configure scholarship programs and eligibility criteria
- Review and approve/reject scholarship applications
- Manage scholarship funds and disbursements
- Generate system-wide reports
- Configure system settings and parameters
- Monitor system performance and security

### Administrator Use Cases:

1. **UC-A1: Scholarship Program Creation**
   - Description: Administrator creates a new scholarship program
   - Primary Actor: Administrator
   - Flow:
     1. Admin logs into the system
     2. Admin navigates to scholarship management
     3. Admin selects "Create New Scholarship Program"
     4. Admin enters program details (name, description, funding amount)
     5. Admin defines eligibility criteria
     6. Admin sets application deadlines
     7. Admin configures approval workflow
     8. Admin activates the scholarship program
   - Postcondition: New scholarship program is available for student applications

2. **UC-A2: Scholarship Application Review**
   - Description: Administrator reviews submitted scholarship applications
   - Primary Actor: Administrator
   - Flow:
     1. Admin logs into the system
     2. Admin navigates to application review dashboard
     3. Admin filters applications by program, status, or date
     4. Admin selects an application to review
     5. System displays application details and supporting documents
     6. Admin verifies eligibility criteria
     7. Admin approves or rejects application with comments
     8. System updates application status
   - Postcondition: Application status is updated and notification is sent to applicant

3. **UC-A3: User Account Management**
   - Description: Administrator manages user accounts and permissions
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to user management section
     2. Admin searches for specific user or browses user list
     3. Admin selects user account to manage
     4. Admin can modify user details, reset password, or change permissions
     5. Admin can activate/deactivate account
     6. System applies and records changes
   - Postcondition: User account is updated with new settings

4. **UC-A4: Scholarship Fund Allocation**
   - Description: Administrator allocates funds to scholarship programs
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to financial management section
     2. Admin selects scholarship program
     3. Admin views current fund allocation and usage
     4. Admin enters new allocation amount
     5. System validates against available budget
     6. Admin confirms allocation
     7. System updates program funding
   - Postcondition: Scholarship program has updated funding allocation

5. **UC-A5: System Configuration**
   - Description: Administrator configures system-wide settings
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to system configuration
     2. Admin selects configuration category (academic periods, GPA calculation, notifications)
     3. Admin modifies configuration parameters
     4. System validates changes
     5. Admin applies changes
     6. System updates configuration
   - Postcondition: System operates with updated configuration parameters

6. **UC-A6: Reporting and Analytics**
   - Description: Administrator generates system-wide reports and analytics
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to reporting dashboard
     2. Admin selects report type (scholarship distribution, academic performance, financial)
     3. Admin configures report parameters and filters
     4. System generates report with visualizations
     5. Admin views or exports report
   - Postcondition: Administrator has access to requested system data and analytics

7. **UC-A7: Batch Scholarship Processing**
   - Description: Administrator processes multiple scholarship applications or payments in batch
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to batch processing section
     2. Admin selects operation type (application review, payment processing)
     3. Admin sets filter criteria for batch
     4. System displays matching items
     5. Admin reviews and selects items for processing
     6. Admin applies batch action (approve, reject, process payment)
     7. System processes all selected items
   - Postcondition: Multiple scholarship items are processed simultaneously

8. **UC-A8: Academic Period Management**
   - Description: Administrator configures academic periods for scholarship cycles
   - Primary Actor: Administrator
   - Flow:
     1. Admin navigates to academic period management
     2. Admin creates or modifies academic periods (semesters/years)
     3. Admin sets key dates (start, end, application deadlines)
     4. Admin associates scholarship programs with periods
     5. System updates academic calendar
   - Postcondition: Academic periods are configured for scholarship operations

## Integration Points

The Regular User and Administrator flows interact at several key points:

1. **Application Review Process**: Regular users submit applications that administrators review and approve/reject
2. **Academic Record Verification**: Administrators may verify academic records submitted by regular users
3. **Payment Processing**: Administrators process payments that regular users receive and acknowledge
4. **System Notifications**: Actions by either user type may trigger notifications to the other
5. **Reporting**: Data entered by regular users appears in administrator reports and analytics

## Security Considerations

1. **Role-Based Access Control**: Strict separation between regular user and administrator capabilities
2. **Audit Logging**: All critical actions by both user types are logged for accountability
3. **Data Privacy**: Regular users can only access their own data, while administrators have broader access with appropriate safeguards
4. **Approval Workflows**: Multi-level approvals for sensitive operations like fund disbursement
