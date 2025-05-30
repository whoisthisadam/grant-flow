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

## Hibernate and Entity Relationship Guidelines

### Handling Lazy Loading

- **Always use JOIN FETCH in HQL queries when retrieving entities with relationships**
  - When retrieving entities that have relationships (OneToMany, ManyToOne, etc.), use JOIN FETCH in your HQL queries to eagerly load related entities
  - This prevents LazyInitializationException when accessing related entities outside of the Hibernate session
  - Example:
    ```java
    // Instead of this:
    "FROM ScholarshipApplication a WHERE a.applicant = :applicant"
    
    // Use this:
    "FROM ScholarshipApplication a " +
    "LEFT JOIN FETCH a.applicant " +
    "LEFT JOIN FETCH a.program " +
    "LEFT JOIN FETCH a.period " +
    "LEFT JOIN FETCH a.reviewer " +
    "WHERE a.applicant = :applicant"
    ```
  - Benefits:
    - Reduces the number of database queries
    - Prevents LazyInitializationException
    - Makes code more robust and maintainable
    - Avoids the need for complex exception handling in service or DTO conversion layers

- **Avoid handling lazy loading exceptions in service or DTO layers**
  - Fix the root cause by properly fetching related entities in the DAO layer
  - Don't rely on Hibernate.initialize() or try-catch blocks to handle lazy loading issues

## UI Navigation Rule

- **Always use the `ChangeScene.changeScene` utility for switching between JavaFX screens.**
    - This method loads the specified FXML, injects the resource bundle, sets up the controller with the current `ClientConnection` and, if needed, the `UserDTO`.
    - Avoid duplicating scene-switching logic in controllers. Instead, replace all manual FXMLLoader and scene setup code with a call to `ChangeScene.changeScene`.
    - Example usage:
      ```java
      ChangeScene.changeScene(event, "/fxml/dashboard_screen.fxml", LangManager.getBundle().getString("dashboard.title"), clientConnection, user);
      ```
    - This ensures localization, controller setup, and navigation are handled consistently across the app.

## JavaFX Controller Guidelines

### Controller Initialization Pattern

All controllers in the application follow a specific initialization pattern to ensure proper dependency injection and prevent timing issues:

1. **Controller Hierarchy**:
   - All controllers must extend `BaseController` which implements the `Connectionable` interface
   - This provides access to the client connection via `getClientConnection()`

2. **Initialization Method**:
   - Controllers should override the `initializeData()` method from `BaseController`
   - This method is called manually by `ChangeScene` after all dependencies are injected
   - Do NOT use JavaFX's automatic `initialize()` method as it runs before dependencies are set

3. **Proper Initialization Order**:
   ```java
   @Override
   public void initializeData() {
       // 1. Set up UI components
       setupUIComponents();
       
       // 2. Load data using client connection
       if (getClientConnection() != null) {
           loadData();
       }
       
       // 3. Update UI texts with current language
       updateTexts();
   }
   ```

4. **Navigation**:
   - Always use the `ChangeScene` utility for navigation between screens
   - This ensures proper dependency injection and initialization

Example controller implementation:
```java
public class MyScreenController extends BaseController {
    @FXML
    private Label titleLabel;
    
    @Setter
    private UserDTO user;
    
    @Override
    public void initializeData() {
        // UI setup
        setupTable();
        
        // Load data
        if (getClientConnection() != null) {
            loadDataFromServer();
        }
        
        // Update texts
        updateTexts();
    }
    
    @Override
    public void updateTexts() {
        titleLabel.setText(LangManager.getBundle().getString("my.screen.title"));
    }
    
    @Override
    public String getFxmlPath() {
        return "/fxml/my_screen.fxml";
    }
}
```

### Dependency Injection

- Client connection is automatically injected via `BaseController`
- User DTO is injected via reflection in `ChangeScene` if the controller has a `setUser` method
- Other dependencies should be set before calling `initializeData()`

## Testing Guidelines

### Unit Testing Command Handlers

When testing command handlers in the `ClientProcessingThread` class, follow these guidelines:

1. **Focus on testing the business logic, not the networking**:
   - Create a test class for each command handler method
   - Create a standalone handler class that implements only the business logic
   - Example naming: `HandleApplyForScholarshipTest` for testing `handleApplyForScholarship`

2. **Create a simplified test handler class**:
   ```java
   private class TestScholarshipHandler {
       private final List<ResponseWrapper> sentResponses = new ArrayList<>();
       private final ScholarshipApplicationService scholarshipApplicationService;
       private final Long authenticatedUserId;
       
       public TestScholarshipHandler(ScholarshipApplicationService service, Long userId) {
           this.scholarshipApplicationService = service;
           this.authenticatedUserId = userId;
       }
       
       public void handleApplyForScholarship(CommandWrapper commandWrapper) {
           try {
               // Extract application data
               SubmitScholarshipApplicationCommand command = commandWrapper.getData();
               
               if (command == null) {
                   ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                           false,
                           "Scholarship application data is missing",
                           null
                   );
                   sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
                   return;
               }
               
               // Submit the application
               ScholarshipApplicationDTO application = scholarshipApplicationService.submitApplication(
                       authenticatedUserId,
                       command.getProgramId(),
                       command.getPeriodId(),
                       command.getAdditionalInfo()
               );
               
               // Create success response
               ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                       true,
                       "Application submitted successfully",
                       application
               );
               
               sendResponse(new ResponseWrapper(ResponseFromServer.SUCCESS, response));
               
           } catch (Exception e) {
               // Create error response
               ScholarshipApplicationResponse response = new ScholarshipApplicationResponse(
                       false,
                       e.getMessage(),
                       null
               );
               
               sendResponse(new ResponseWrapper(ResponseFromServer.ERROR, response));
           }
       }
       
       private void sendResponse(ResponseWrapper response) {
           sentResponses.add(response);
       }
       
       public ResponseWrapper getLastSentResponse() {
           if (sentResponses.isEmpty()) {
               return null;
           }
           return sentResponses.get(sentResponses.size() - 1);
       }
   }
   ```

3. **Create test implementations of services**:
   ```java
   private class TestScholarshipApplicationService extends ScholarshipApplicationService {
       private boolean throwException = false;
       private String exceptionMessage = "";
       private ScholarshipApplicationDTO returnValue = null;
       
       public void setThrowException(boolean throwException, String message) {
           this.throwException = throwException;
           this.exceptionMessage = message;
       }
       
       public void setReturnValue(ScholarshipApplicationDTO returnValue) {
           this.returnValue = returnValue;
       }
       
       @Override
       public ScholarshipApplicationDTO submitApplication(Long userId, Long programId, Long periodId, String additionalInfo) 
               throws Exception {
           if (throwException) {
               throw new Exception(exceptionMessage);
           }
           return returnValue;
       }
   }
   ```

4. **Test both success and error scenarios**:
   - Test with valid input data
   - Test with null or invalid input data
   - Test service exceptions
   - Verify the correct response is sent

5. **Advantages of this approach**:
   - Avoids issues with mocking complex dependencies like sockets and streams
   - Works with any Java version, including Java 22
   - Focuses on testing the actual business logic
   - Simpler and more maintainable tests

## Notes About Scholarship Functionality

The scholarship-related functionality has been temporarily disabled in the client application. The UI buttons for this functionality are still present but will display informational messages when clicked.

## Client-Server Communication Guidelines

### Exception Handling in Client-Server Communication

- **Always send a proper response from the server, even when exceptions occur**
  - When an exception occurs during command processing, catch it and include the error message in a structured response
  - Never let the server fail silently without sending a response to the client
  - Example:
    ```java
    try {
        // Process the command
        Result result = service.processCommand(command);
        
        // Send success response
        sendObject(new ResponseWrapper(ResponseFromServer.SUCCESS, result));
    } catch (Exception e) {
        logger.error("Error processing command", e);
        
        // Create a structured error response with the exception message
        ErrorResponse response = new ErrorResponse(false, e.getMessage());
        
        // Send error response to client
        sendObject(new ResponseWrapper(ResponseFromServer.ERROR, response));
    }
    ```

- **Propagate specific error messages from server to client UI**
  - On the client side, throw exceptions with the server's error message instead of returning null
  - In the UI layer, catch these exceptions and display the specific error message to the user
  - Example (Client Connection Layer):
    ```java
    if (response.getResponse() == ResponseFromServer.ERROR) {
        String errorMessage = response.getData() != null ? response.getData().toString() : "Unknown error";
        logger.warn("Operation failed: {}", errorMessage);
        throw new Exception(errorMessage);
    }
    ```
  - Example (UI Layer):
    ```java
    try {
        Result result = clientConnection.performOperation(params);
        // Handle success
    } catch (Exception e) {
        // Display the specific error message from the server
        showError("Error", e.getMessage());
    }
    ```

- **Benefits of this approach**:
  - Prevents the client from hanging while waiting for a response that will never come
  - Provides users with specific, actionable error messages
  - Maintains the request-response flow even in error scenarios
  - Makes debugging easier by preserving error context across the network boundary

- **Common mistakes to avoid**:
  - Throwing exceptions on the server without sending a response
  - Returning null from client methods instead of throwing exceptions with error messages
  - Displaying generic error messages instead of the specific server error
  - Using different error handling patterns for different operations

## ClientConnection Usage Guidelines

### Sending Commands and Receiving Responses

- **Use `sendObject(commandWrapper)` instead of `sendCommand(wrapper)`**
  - This ensures that the command is properly wrapped and sent to the server

- **Use `ResponseWrapper response = receiveObject()` instead of `ServerResponse response = receiveResponse()`**
  - This ensures that the response is properly unwrapped and handled

- **Check response with `response.getResponse() == ResponseFromServer.SUCCESS` instead of `response.isSuccess()`**
  - This ensures that the response is properly checked and handled

- **Always handle both `IOException` and `ClassNotFoundException` in the method signature and catch blocks**
  - This ensures that all possible exceptions are handled and the application remains stable

Example usage:
```java
try {
    CommandWrapper command = new CommandWrapper(CommandType.MY_COMMAND, params);
    sendObject(command);
    
    ResponseWrapper response = receiveObject();
    if (response.getResponse() == ResponseFromServer.SUCCESS) {
        Result result = (Result) response.getData();
        // Handle success
    } else {
        // Handle error
    }
} catch (IOException | ClassNotFoundException e) {
    logger.error("Error sending or receiving data", e);
    // Handle exception
}
```

## Notes

- **Do not use `cd` commands in the `run_command` tool**
  - Instead, specify the working directory using the `cwd` parameter.
  - Example: `run_command("mvn clean install", cwd="Server")`

- **When implementing new methods in ClientConnection, use the correct pattern for communication**
  - Use `sendObject(commandWrapper)` instead of `sendCommand(wrapper)`
  - Use `ResponseWrapper response = receiveObject()` instead of `ServerResponse response = receiveResponse()`
  - Check response with `response.getResponse() == ResponseFromServer.SUCCESS` instead of `response.isSuccess()`
  - Always handle both `IOException` and `ClassNotFoundException` in the method signature and catch blocks
  - Follow the existing pattern in other methods for consistency

- **Always use the `ChangeScene.changeScene` utility for switching between JavaFX screens.**
