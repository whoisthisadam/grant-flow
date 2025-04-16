# Grant Flow Client - Running Instructions

## Prerequisites
- Java 22 or higher
- Maven 3.8 or higher

## Running the Client Application

To run the client application, follow these steps:

1. Make sure the server is running first (use `run-server.bat`)
2. Open a command prompt or terminal
3. Navigate to the Client directory:
   ```
   cd D:\bsuir\networkdev\grant-flow\Client
   ```
4. Run the following Maven command:
   ```
   mvn clean javafx:run
   ```

## Troubleshooting

If you encounter any issues:

1. Make sure the server is running and accessible
2. Check that all dependencies are properly installed
3. Verify that you're using the correct Java version

## Notes About Scholarship Functionality

The scholarship-related functionality has been temporarily disabled in the client application. The UI buttons for this functionality are still present but will display informational messages when clicked.
