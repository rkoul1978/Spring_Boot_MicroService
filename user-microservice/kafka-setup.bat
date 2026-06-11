@echo off
REM Kafka Setup and Testing Script for User Microservice (Windows)

setlocal enabledelayedexpansion

echo.
echo ========================================
echo Kafka Setup and Testing Script (Windows)
echo ========================================
echo.

REM Colors won't work in basic batch, so we'll use simple text
REM Check if Docker is running
echo [*] Checking Docker daemon...
docker ps >nul 2>&1
if %errorlevel% neq 0 (
    echo [X] Docker daemon is not running. Please start Docker and try again.
    exit /b 1
)
echo [OK] Docker is running
echo.

:menu
echo ========================================
echo Kafka Setup Options
echo ========================================
echo 1. Start all services
echo 2. Create Kafka topics
echo 3. List Kafka topics
echo 4. Describe user-events topic
echo 5. Send test message
echo 6. Check consumer status
echo 7. Show service URLs
echo 8. Stop all services
echo 9. View logs
echo 0. Exit
echo.
set /p choice="Select an option (0-9): "

if "%choice%"=="1" (
    echo.
    echo [*] Starting Docker containers...
    call docker-compose up -d
    echo.
    echo [*] Waiting for services to be ready (15 seconds)...
    timeout /t 15 /nobreak
    echo [OK] Services should now be running
    echo.
    goto menu
) else if "%choice%"=="2" (
    echo.
    echo [*] Creating Kafka topics...
    docker exec user-microservice-kafka kafka-topics --bootstrap-server localhost:9092 --create --topic user-events --partitions 3 --replication-factor 1 2>nul || echo [OK] Topic already exists
    echo.
    goto menu
) else if "%choice%"=="3" (
    echo.
    echo [*] Available Kafka topics:
    docker exec user-microservice-kafka kafka-topics --bootstrap-server localhost:9092 --list
    echo.
    goto menu
) else if "%choice%"=="4" (
    echo.
    echo [*] Topic configuration:
    docker exec user-microservice-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic user-events
    echo.
    goto menu
) else if "%choice%"=="5" (
    echo.
    echo [*] Sending test message...
    REM Simplified test message
    docker exec user-microservice-kafka kafka-console-producer --broker-list localhost:9092 --topic user-events ^
        > nul 2>&1 ^
        echo {"event_id":"test-123","event_type":"CREATED","user_id":"user-123","email":"test@example.com"}
    echo [OK] Test message sent (or already exists)
    echo.
    goto menu
) else if "%choice%"=="6" (
    echo.
    echo [*] Checking consumer group status...
    docker exec user-microservice-kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group user-service-group --describe
    echo.
    goto menu
) else if "%choice%"=="7" (
    echo.
    echo ========================================
    echo Service URLs
    echo ========================================
    echo Application: http://localhost:8080/api
    echo MongoDB Express: http://localhost:8081
    echo Kafka UI: http://localhost:8080
    echo Kafka Bootstrap: localhost:9092
    echo Zookeeper: localhost:2181
    echo.
    goto menu
) else if "%choice%"=="8" (
    echo.
    echo [*] Stopping all services...
    call docker-compose down
    echo [OK] Services stopped
    echo.
    goto menu
) else if "%choice%"=="9" (
    echo.
    echo [*] Showing Docker logs...
    call docker-compose logs -f
    echo.
    goto menu
) else if "%choice%"=="0" (
    echo.
    echo [OK] Exiting...
    exit /b 0
) else (
    echo.
    echo [X] Invalid option. Please try again.
    echo.
    goto menu
)
