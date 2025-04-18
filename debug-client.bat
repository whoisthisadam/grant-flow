@echo off
echo Starting Grant Flow Client in Debug Mode...
cd Client

echo.
echo Debug Options:
echo 1. Remote Debugging with JavaFX (IDE connection on port 5005)
echo 2. Direct Debug with JavaFX Maven Plugin
echo.
set /p option="Select debug option (1 or 2): "

if "%option%"=="1" (
    echo Starting client with remote debugging on port 5005...
    echo Connect your IDE to localhost:5005 to begin debugging
    echo The application will wait until you connect a debugger
    
    :: Build and prepare dependencies
    call mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency
    
    :: Set classpath with all dependencies including JavaFX
    set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*
    
    :: Run with debug agent - using classpath approach instead of module path
    echo Starting application with classpath: %CLASSPATH%
    java -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005 -cp "%CLASSPATH%" com.kasperovich.RunClient
) else if "%option%"=="2" (
    echo Starting client with direct JavaFX debugging...
    
    :: Use the JavaFX launcher with JVM debug arguments
    call mvn clean compile
    
    :: Run using JavaFX launcher with debug options
    call mvn javafx:run -Djavafx.run.jvmArgs="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005"
) else (
    echo Invalid option selected. Exiting.
)

pause