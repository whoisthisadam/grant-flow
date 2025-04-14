# Grant Flow - Scholarship Calculation System

A full-stack application with JavaFX Client and Server components for calculating students' scholarships. The server uses Hibernate to interact with Azure SQL Database (MS SQL), and communication is handled via Sockets.

## Project Structure

This is a Maven multi-module project with the following components:

- **Client**: JavaFX-based user interface
- **Server**: Backend service for processing scholarship calculations
- **Models**: Shared data models and communication protocols

## Setup Instructions

### Prerequisites

- Java JDK 22
- Maven 3.9 or higher
- Azure SQL Database (or local MS SQL Server for development)

### Building the Project

1. Clone the repository:
   ```
   git clone [repository-url]
   cd grant-flow
   ```

2. Build the project using Maven:
   ```
   mvn clean install
   ```

### Configuration

#### Server Configuration

Edit the server configuration file at `Server/src/main/resources/config.properties`:

```properties
# Server configuration
serverPort=9000

# Database configuration
db.url=jdbc:sqlserver://[your-server].database.windows.net:1433;database=[your-db]
db.username=[your-username]
db.password=[your-password]
db.driver=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

#### Client Configuration

Edit the client configuration file at `Client/src/main/resources/config.properties`:

```properties
# Server connection settings
serverIp=localhost
serverPort=9000
```

### Running the Application

#### Starting the Server

```
java -jar Server/target/Server-1.0-SNAPSHOT.jar
```

#### Starting the Client

```
java -jar Client/target/Client-1.0-SNAPSHOT.jar
```

## Features

- User registration and authentication
- Scholarship application submission and tracking
- Academic record management
- Scholarship calculation based on configurable criteria
- Administrative interface for managing scholarships and users
- Reporting and analytics
- Multi-language support (English/Russian)

## Development Guidelines

- Follow the Java code style conventions
- Document all public APIs
- Write unit tests for new functionality
- Use the logging framework for all logging needs
- Keep the UI and business logic separate

## Internationalization

The application supports both English and Russian languages. UI text is stored in resource bundles:

- English: `src/main/resources/messages_en.properties`
- Russian: `src/main/resources/messages_ru.properties`

## License

[Your license information]

## Contact

[Your contact information]
