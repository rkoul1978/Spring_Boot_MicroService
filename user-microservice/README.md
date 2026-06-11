# User Microservice

A Spring Boot microservice application that provides RESTful APIs for managing user data with MongoDB database integration.

## Features

- RESTful API endpoints for CRUD operations on users
- MongoDB integration with Spring Data MongoDB
- Comprehensive logging
- Global exception handling
- DTOs for API contracts
- Lombok for reducing boilerplate code
- Maven build system

## Technology Stack

- **Java**: 17
- **Spring Boot**: 3.1.5
- **Spring Data MongoDB**: For MongoDB integration
- **MongoDB**: NoSQL Database
- **Lombok**: To reduce boilerplate code
- **Maven**: Build tool

## Project Structure

```
user-microservice/
├── src/
│   ├── main/
│   │   ├── java/com/microservice/
│   │   │   ├── UserMicroserviceApplication.java    # Main application class
│   │   │   ├── controller/
│   │   │   │   └── UserController.java              # REST endpoints
│   │   │   ├── service/
│   │   │   │   └── UserService.java                 # Business logic
│   │   │   ├── repository/
│   │   │   │   └── UserRepository.java              # Data access layer
│   │   │   ├── model/
│   │   │   │   └── User.java                        # Entity model
│   │   │   ├── dto/
│   │   │   │   └── UserDTO.java                     # Data transfer object
│   │   │   ├── config/
│   │   │   │   └── MongoConfig.java                 # MongoDB configuration
│   │   │   └── exception/
│   │   │       └── GlobalExceptionHandler.java      # Exception handling
│   │   └── resources/
│   │       └── application.yml                      # Application configuration
│   └── test/
│       └── java/com/microservice/                   # Test classes
├── pom.xml                                          # Maven configuration
└── README.md                                        # This file
```

## Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher
- MongoDB 4.0 or higher (running locally or via connection string)

## Installation & Setup

### 1. Clone/Navigate to the project
```bash
cd user-microservice
```

### 2. Configure MongoDB Connection

Edit `src/main/resources/application.yml`:

**For local MongoDB:**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/user_db
```

**For MongoDB Atlas (Cloud):**
```yaml
spring:
  data:
    mongodb:
      uri: mongodb+srv://username:password@cluster.mongodb.net/user_db?retryWrites=true&w=majority
```

**For MongoDB with credentials:**
```yaml
spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: user_db
      username: admin
      password: password
```

### 3. Build the Project
```bash
mvn clean install
```

### 4. Run the Application
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080/api`

## API Endpoints

### Base URL
```
http://localhost:8080/api/v1/users
```

### Endpoints

#### 1. Create User
- **POST** `/v1/users`
- **Request Body**:
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "1234567890",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA"
}
```
- **Response**: `201 Created` with created user object

#### 2. Get All Users
- **GET** `/v1/users`
- **Response**: `200 OK` with list of all users

#### 3. Get User by ID
- **GET** `/v1/users/{id}`
- **Response**: `200 OK` with user object

#### 4. Get User by Email
- **GET** `/v1/users/email/{email}`
- **Response**: `200 OK` with user object

#### 5. Get Active Users
- **GET** `/v1/users/status/active`
- **Response**: `200 OK` with list of active users

#### 6. Get Users by City
- **GET** `/v1/users/city/{city}`
- **Response**: `200 OK` with list of users in the city

#### 7. Search Users by Name
- **GET** `/v1/users/search?firstName=John&lastName=Doe`
- **Response**: `200 OK` with list of matching users

#### 8. Update User
- **PUT** `/v1/users/{id}`
- **Request Body**: Same as Create User
- **Response**: `200 OK` with updated user object

#### 9. Deactivate User
- **PATCH** `/v1/users/{id}/deactivate`
- **Response**: `204 No Content`

#### 10. Delete User
- **DELETE** `/v1/users/{id}`
- **Response**: `204 No Content`

## Example Usage with cURL

### Create a User
```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith@example.com",
    "phoneNumber": "9876543210",
    "address": "456 Oak Ave",
    "city": "Los Angeles",
    "state": "CA",
    "zipCode": "90001",
    "country": "USA"
  }'
```

### Get All Users
```bash
curl -X GET http://localhost:8080/api/v1/users
```

### Get User by ID
```bash
curl -X GET http://localhost:8080/api/v1/users/{user-id}
```

### Update User
```bash
curl -X PUT http://localhost:8080/api/v1/users/{user-id} \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane.smith.updated@example.com",
    "phoneNumber": "1111111111",
    "address": "789 Pine Rd",
    "city": "San Francisco",
    "state": "CA",
    "zipCode": "94105",
    "country": "USA"
  }'
```

### Delete User
```bash
curl -X DELETE http://localhost:8080/api/v1/users/{user-id}
```

## Database Schema (MongoDB)

### Users Collection
```javascript
{
  "_id": ObjectId("..."),
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "phoneNumber": "1234567890",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zipCode": "10001",
  "country": "USA",
  "active": true,
  "createdAt": ISODate("2024-01-01T12:00:00Z"),
  "updatedAt": ISODate("2024-01-01T12:00:00Z")
}
```

## Logging

Logs are configured in `application.yml`:
- Root level: INFO
- Application level: DEBUG
- Output: Console with timestamp and message

## Error Handling

The application has centralized exception handling:
- **404 Not Found**: When a user is not found
- **500 Internal Server Error**: For other exceptions

## Running Tests

```bash
mvn test
```

## Building JAR

```bash
mvn clean package
java -jar target/user-microservice-1.0.0.jar
```

## Development

### Hot Reload
The application includes Spring DevTools for automatic reload during development.

### IDE Setup
- Import as Maven project in your IDE (IntelliJ IDEA, Eclipse, VS Code)
- Java 17 should be configured
- Enable annotation processing for Lombok

## Future Enhancements

- Add authentication and authorization (JWT)
- Add API documentation (Swagger/Springdoc)
- Add pagination and sorting
- Add request validation annotations
- Add audit fields
- Add caching
- Add Spring Security
- Add unit and integration tests
- Add Docker support
- Add CI/CD pipeline

## License

This project is open source and available under the MIT License.

## Support

For issues or questions, please create an issue in the repository.
