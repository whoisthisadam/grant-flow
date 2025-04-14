# Scholarship Calculation System - Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                                                                         │
│                           CLIENT APPLICATION                            │
│                                                                         │
│  ┌───────────────┐    ┌───────────────┐    ┌───────────────────────┐   │
│  │               │    │               │    │                       │   │
│  │  JavaFX UI    │◄───┤ Client Logic  │◄───┤ Socket Communication  │   │
│  │  Components   │    │   Services    │    │       Module          │   │
│  │               │    │               │    │                       │   │
│  └───────┬───────┘    └───────┬───────┘    └───────────┬───────────┘   │
│          │                    │                        │                │
│          │                    │                        │                │
│  ┌───────▼───────┐    ┌───────▼───────┐                │                │
│  │               │    │               │                │                │
│  │ Resource      │    │ Data Caching  │                │                │
│  │ Bundles       │    │ Service       │                │                │
│  │ (EN/RU)       │    │               │                │                │
│  │               │    │               │                │                │
│  └───────────────┘    └───────────────┘                │                │
│                                                        │                │
└────────────────────────────────────────────────────────┼────────────────┘
                                                         │
                                                         │ Socket Connection
                                                         │
┌────────────────────────────────────────────────────────┼────────────────┐
│                                                        │                │
│                             SERVER                     │                │
│                                                        │                │
│  ┌───────────────────────┐                             │                │
│  │                       │                             │                │
│  │  Socket Server        │◄────────────────────────────┘                │
│  │  Infrastructure       │                                              │
│  │                       │                                              │
│  └───────────┬───────────┘                                              │
│              │                                                          │
│              │                                                          │
│  ┌───────────▼───────────┐    ┌───────────────────────────────────┐    │
│  │                       │    │                                   │    │
│  │  Command Processing   │◄───┤  Authentication & Authorization   │    │
│  │  Framework            │    │                                   │    │
│  │                       │    │                                   │    │
│  └───────────┬───────────┘    └───────────────────────────────────┘    │
│              │                                                          │
│              │                                                          │
│  ┌───────────▼───────────┐    ┌───────────────────────────────────┐    │
│  │                       │    │                                   │    │
│  │  Business Logic       │◄───┤  Scholarship Calculation          │    │
│  │  Services             │    │  Algorithms                       │    │
│  │                       │    │                                   │    │
│  └───────────┬───────────┘    └───────────────────────────────────┘    │
│              │                                                          │
│              │                                                          │
│  ┌───────────▼───────────┐    ┌───────────────────────────────────┐    │
│  │                       │    │                                   │    │
│  │  Data Access Layer    │◄───┤  Reporting & Analytics Engine     │    │
│  │  (DAOs)               │    │                                   │    │
│  │                       │    │                                   │    │
│  └───────────┬───────────┘    └───────────────────────────────────┘    │
│              │                                                          │
│              │                                                          │
│  ┌───────────▼───────────┐                                              │
│  │                       │                                              │
│  │  Hibernate ORM        │                                              │
│  │                       │                                              │
│  └───────────┬───────────┘                                              │
│              │                                                          │
└──────────────┼──────────────────────────────────────────────────────────┘
               │
┌──────────────▼──────────────────────────────────────────────────────────┐
│                                                                         │
│                        Azure SQL Database (MS SQL)                       │
│                                                                         │
└─────────────────────────────────────────────────────────────────────────┘
```

## Component Descriptions

### Client Application

1. **JavaFX UI Components**
   - User interfaces for both regular users and administrators
   - Implements all screens defined in use cases
   - Supports language switching (English/Russian)

2. **Client Logic Services**
   - Handles business logic on the client side
   - Manages client-side validation
   - Coordinates UI updates and data presentation

3. **Socket Communication Module**
   - Manages connection to the server
   - Handles sending commands and receiving responses
   - Implements retry and error handling logic

4. **Resource Bundles**
   - Contains UI text in English and Russian
   - Supports runtime language switching
   - Handles formatting for dates, numbers, and currency

5. **Data Caching Service**
   - Stores frequently accessed data locally
   - Reduces server round-trips
   - Supports offline mode capabilities

### Server

1. **Socket Server Infrastructure**
   - Accepts and manages client connections
   - Handles multiple concurrent client sessions
   - Implements connection pooling and timeout handling

2. **Command Processing Framework**
   - Receives and routes client commands
   - Dispatches commands to appropriate handlers
   - Returns responses to clients

3. **Authentication & Authorization**
   - Verifies user credentials
   - Manages user sessions
   - Enforces role-based access control

4. **Business Logic Services**
   - Implements core application functionality
   - Processes scholarship applications
   - Manages user and academic records

5. **Scholarship Calculation Algorithms**
   - Implements GPA calculation
   - Determines scholarship eligibility
   - Calculates scholarship amounts

6. **Reporting & Analytics Engine**
   - Generates performance reports
   - Creates analytics dashboards
   - Produces financial reports

7. **Data Access Layer (DAOs)**
   - Provides interface to the database
   - Implements CRUD operations
   - Handles transaction management

8. **Hibernate ORM**
   - Maps Java objects to database tables
   - Manages entity relationships
   - Optimizes database operations

### Database

**Azure SQL Database (MS SQL)**
   - Stores all application data
   - Implements schema with proper relationships and constraints
   - Supports backup and recovery operations

## Data Flow

1. User interacts with JavaFX UI
2. Client Logic processes the interaction
3. Socket Communication module sends command to server
4. Server's Socket Infrastructure receives the command
5. Command Processing Framework routes to appropriate handler
6. Business Logic Services process the command
7. Data Access Layer interacts with the database as needed
8. Response flows back through the same path to the client
9. Client UI updates based on the response

## Security Considerations

1. All communication between client and server is secured
2. Authentication is required for all operations
3. Role-based access control enforces proper authorization
4. Sensitive data is encrypted in the database
5. Input validation occurs on both client and server sides
