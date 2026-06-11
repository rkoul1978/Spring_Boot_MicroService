# Development Guide

## Quick Start

### 1. Start MongoDB using Docker Compose

```bash
docker-compose up -d
```

This will start:
- MongoDB on `mongodb://admin:password@localhost:27017`
- MongoDB Express (UI) on `http://localhost:8081` (admin/password)

### 2. Build and Run the Application

```bash
# Build
mvn clean install

# Run with development profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Or run directly
java -jar target/user-microservice-1.0.0.jar --spring.profiles.active=dev
```

### 3. Test the API

Access the API at: `http://localhost:8080/api/v1/users`

### 4. Monitor MongoDB

Open MongoDB Express at: `http://localhost:8081` (admin/password)

## Testing Endpoints

### Create a User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "1234567890",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }'
```

### Get All Users
```bash
curl http://localhost:8080/api/v1/users
```

### Get User by ID
```bash
curl http://localhost:8080/api/v1/users/{id}
```

## Stopping Services

```bash
# Stop and remove containers
docker-compose down

# Stop only (keep containers)
docker-compose stop

# Start again
docker-compose start
```

## Useful Maven Commands

```bash
# Clean build
mvn clean

# Compile
mvn compile

# Run tests
mvn test

# Package
mvn package

# Install dependencies
mvn install

# Run with Maven
mvn spring-boot:run

# Build and run JAR
mvn clean package
java -jar target/user-microservice-1.0.0.jar
```

## IDE Setup

### IntelliJ IDEA
1. Open project as Maven project
2. Wait for Maven dependencies to download
3. Set Java SDK to 17
4. Enable annotation processing (Settings → Build, Execution, Deployment → Compiler → Annotation Processors)
5. Run → Run 'UserMicroserviceApplication'

### VS Code
1. Install extensions: Spring Boot Extension Pack, Language Support for Java (Red Hat)
2. Open the project folder
3. VS Code will prompt to configure the Java runtime
4. Press F5 to debug or Ctrl+F5 to run

### Eclipse
1. File → Import → Existing Maven Projects
2. Select the project folder
3. Right-click project → Properties → Java Compiler → Set to Java 17
4. Run as → Spring Boot App

## Troubleshooting

### MongoDB Connection Issues
- Check if MongoDB is running: `docker ps`
- Check Docker logs: `docker logs user-microservice-mongodb`
- Verify connection string in `application-dev.yml`

### Port Already in Use
```bash
# Kill process on port 8080
# Windows
netstat -ano | findstr :8080
taskkill /PID {PID} /F

# Linux/Mac
lsof -i :8080
kill -9 {PID}
```

### Maven Issues
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild
mvn clean install
```

## Performance Tips

- Use IDE debug mode with breakpoints for development
- Enable Spring DevTools for hot reload
- Monitor application logs in IDE console
- Use MongoDB Express to inspect data

