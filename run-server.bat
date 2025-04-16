@echo off
cd Server
echo Starting server...

REM Use Maven to build and copy dependencies
echo Building project...
call mvn clean compile dependency:copy-dependencies -DoutputDirectory=target/dependency -DincludeScope=runtime

REM Set up a simpler classpath that relies on Maven's dependency management
set CLASSPATH=target\classes;..\Models\target\classes;target\dependency\*

REM Run the server
echo Running server...
java -cp "%CLASSPATH%" com.kasperovich.RunServer

echo Server process exited with code %errorlevel%
cd ..
pause
