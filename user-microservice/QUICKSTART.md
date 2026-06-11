# Spring Boot User Microservice

A production-ready Spring Boot microservice with complete RESTful API endpoints and MongoDB integration.

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose

### Automated Setup (Recommended)

#### Windows
```bash
setup.bat
```

#### Linux/Mac
```bash
chmod +x setup.sh
./setup.sh
```

### Manual Setup

1. **Start MongoDB**
```bash
docker-compose up -d
```

2. **Build the Project**
```bash
mvn clean install
```

3. **Run the Application**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

4. **Access the Application**
- API: http://localhost:8080/api/v1/users
- MongoDB UI: http://localhost:8081 (admin/password)

## Features

✅ Complete RESTful CRUD APIs  
✅ MongoDB Integration with Spring Data  
✅ Comprehensive Error Handling  
✅ Logging & Monitoring  
✅ Docker Support  
✅ Development Ready  

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/users` | Create new user |
| GET | `/v1/users` | Get all users |
| GET | `/v1/users/{id}` | Get user by ID |
| GET | `/v1/users/email/{email}` | Get user by email |
| GET | `/v1/users/status/active` | Get active users |
| GET | `/v1/users/city/{city}` | Get users by city |
| GET | `/v1/users/search` | Search users by name |
| PUT | `/v1/users/{id}` | Update user |
| PATCH | `/v1/users/{id}/deactivate` | Deactivate user |
| DELETE | `/v1/users/{id}` | Delete user |

## Project Structure

```
user-microservice/
├── src/main/java/com/microservice/
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic
│   ├── repository/          # Data access
│   ├── model/               # Entities
│   ├── dto/                 # Data Transfer Objects
│   ├── config/              # Configuration
│   └── exception/           # Exception handling
├── src/main/resources/      # Configuration files
├── src/test/                # Tests
├── pom.xml                  # Maven dependencies
├── docker-compose.yml       # Docker setup
└── README.md                # Documentation
```

## Technologies

- **Language**: Java 17
- **Framework**: Spring Boot 3.1.5
- **Database**: MongoDB
- **Build**: Maven
- **Tools**: Docker, Lombok

## Sample Request

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

## Database

Users are stored in MongoDB collection with automatic timestamps for creation and updates.

## Documentation

- [README.md](README.md) - Complete API documentation
- [DEVELOPMENT.md](DEVELOPMENT.md) - Development guide
- [docker-compose.yml](docker-compose.yml) - Docker configuration

## Build & Deploy

```bash
# Build JAR
mvn clean package

# Run JAR
java -jar target/user-microservice-1.0.0.jar

# Build Docker Image
docker build -t user-microservice:1.0.0 .

# Run Docker Container
docker run -p 8080:8080 --network user-microservice-network user-microservice:1.0.0
```

## Stopping Services

```bash
docker-compose down
```

## License

MIT License

---

**Created**: 2024 | **Version**: 1.0.0
