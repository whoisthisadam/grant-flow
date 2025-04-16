# Grant Flow - Project Rules and Guidelines

## Running the Application

### Server Module

To run the server application:

1. Use the provided batch file:
   ```
   .\run-server.bat
   ```

2. Alternatively, use Maven directly:
   ```
   cd Server
   mvn clean compile exec:java
   ```

3. Manual classpath method:
   ```
   cd Server
   mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
   set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*
   java -cp "%CLASSPATH%" com.kasperovich.RunServer
   ```

### Client Module

To run the client application:

1. Use the provided batch file:
   ```
   .\run-client.bat
   ```

2. Alternatively, use Maven directly:
   ```
   cd Client
   mvn clean javafx:run
   ```

3. Manual classpath method:
   ```
   cd Client
   mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
   set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*
   java -cp "%CLASSPATH%" com.kasperovich.RunClient
   ```

**Important**: Always start the server before starting the client.

## Building the Application

### Building All Modules

To build all modules at once:

```
mvn clean install
```

### Building Individual Modules

To build a specific module:

```
mvn clean install -pl Models
mvn clean install -pl Server
mvn clean install -pl Client
```

## Debugging

### Server Debugging

1. Using IDE:
   - Set breakpoints in your IDE
   - Run the Server module in debug mode

2. Using command line:
   ```
   cd Server
   mvn clean compile exec:java -Dexec.args="-debug"
   ```

3. Remote debugging:
   ```
   cd Server
   mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
   set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -cp "%CLASSPATH%" com.kasperovich.RunServer
   ```

### Client Debugging

1. Using IDE:
   - Set breakpoints in your IDE
   - Run the Client module in debug mode

2. Using command line:
   ```
   cd Client
   mvn clean javafx:run -Djavafx.debug=true
   ```

3. Remote debugging:
   ```
   cd Client
   mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime
   set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*
   java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006 -cp "%CLASSPATH%" com.kasperovich.RunClient
   ```

## Testing

### Running All Tests

```
mvn test
```

### Running Tests for a Specific Module

```
mvn test -pl Models
mvn test -pl Server
mvn test -pl Client
```

## Database Management

### Viewing Database Schema

1. Connect to the database using your preferred SQL client
2. Connection details are in the Server's Hibernate configuration

### Running Database Migrations

Database migrations are handled automatically when the server starts.

## Project Structure

- **Models**: Contains shared DTOs and entities
- **Server**: Contains server-side logic and database access
- **Client**: Contains JavaFX client application

## Development Guidelines

1. Always run tests before committing changes
2. Keep the Models module as lightweight as possible
3. Use proper logging throughout the application
4. Follow the established package structure
5. Document public APIs

## Notes About Scholarship Functionality

The scholarship-related functionality has been temporarily disabled in the client application. The UI buttons for this functionality are still present but will display informational messages when clicked.
