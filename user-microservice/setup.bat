@echo off
echo Starting User Microservice Setup...

REM Check if Docker is installed
docker --version >nul 2>&1
if errorlevel 1 (
    echo Error: Docker is not installed. Please install Docker first.
    exit /b 1
)

echo Starting MongoDB containers...
docker-compose up -d

echo Waiting for MongoDB to be ready...
timeout /t 5

echo Building Spring Boot application...
call mvn clean install

if errorlevel 1 (
    echo Error: Maven build failed!
    exit /b 1
)

echo.
echo ==========================================
echo Setup completed successfully!
echo ==========================================
echo.
echo MongoDB Details:
echo   URI: mongodb://admin:password@localhost:27017
echo   Express UI: http://localhost:8081
echo.
echo To start the application, run:
echo   mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
echo.
echo Or run the JAR:
echo   java -jar target/user-microservice-1.0.0.jar --spring.profiles.active=dev
echo.
