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

## Notes About Scholarship Functionality

The scholarship-related functionality has been temporarily disabled in the client application. The UI buttons for this functionality are still present but will display informational messages when clicked.
