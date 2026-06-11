# Spring Boot User Microservice

A production-ready Spring Boot microservice with complete RESTful API endpoints, MongoDB integration, Apache Kafka event streaming, and Redis caching.

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

1. **Start MongoDB, Kafka, and Redis**
```bash
docker-compose up -d
```

2. **Create Kafka Topics** (Optional - auto-created)
```bash
# Windows
kafka-setup.bat

# Linux/Mac
chmod +x kafka-setup.sh
./kafka-setup.sh
```

3. **Build the Project**
```bash
mvn clean install
```

4. **Run the Application**
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

5. **Access the Application**
- API: http://localhost:8080/api/v1/users
- MongoDB UI: http://localhost:8081 (admin/password)
- Kafka UI: http://localhost:8080
- Redis UI: http://localhost:8082
- Kafka Bootstrap: localhost:9092
- Redis: localhost:6379

## Features

✅ Complete RESTful CRUD APIs  
✅ MongoDB Integration with Spring Data  
✅ **Redis Caching** (10-100x faster queries!)  
✅ Apache Kafka Event Streaming  
✅ Event-Driven Architecture  
✅ Comprehensive Error Handling  
✅ Logging & Monitoring  
✅ Docker Support  
✅ Development Ready  

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/v1/users` | Create new user |
| GET | `/v1/users` | Get all users (cached) |
| GET | `/v1/users/{id}` | Get user by ID (cached) |
| GET | `/v1/users/email/{email}` | Get user by email (cached) |
| GET | `/v1/users/status/active` | Get active users (cached) |
| GET | `/v1/users/city/{city}` | Get users by city (cached) |
| GET | `/v1/users/search` | Search users by name (cached) |
| PUT | `/v1/users/{id}` | Update user |
| PATCH | `/v1/users/{id}/deactivate` | Deactivate user |
| DELETE | `/v1/users/{id}` | Delete user |

## Redis Caching

This microservice automatically caches all user queries in Redis for improved performance:

- **Cache Coverage**: All read operations (GET requests)
- **Cache TTL**: 1 hour default
- **Performance**: 10-100x faster response times
- **Smart Invalidation**: Caches automatically cleared on writes

### Cache Operations

```
First Request (Cache Miss) → Database Query → Cache Result → ~100ms
Subsequent Requests (Cache Hit) → Redis Cache → Result → ~5-10ms
```

### Monitoring Cache

**Redis Commander** - Web UI for cache management
- URL: http://localhost:8082
- View cached data in real-time
- Monitor cache hit/miss rates
- Manually evict cache if needed

### Testing Cache Performance

```bash
# First request (slow - cache miss)
curl -w "\nTime: %{time_total}s\n" http://localhost:8080/api/v1/users

# Second request (fast - cache hit)
curl -w "\nTime: %{time_total}s\n" http://localhost:8080/api/v1/users

# Expected: 10-100x faster!
```

## Kafka Event Streaming

This microservice publishes events to Apache Kafka for the following operations:

- **CREATED**: When a new user is created
- **UPDATED**: When user information is modified
- **DELETED**: When a user is removed
- **DEACTIVATED**: When a user account is disabled

### Event Flow

```
Create/Update/Delete User → Kafka Topic (user-events) → Other Services/Consumers
```

### Monitoring Kafka Events

**Using Kafka UI**
- Open: http://localhost:8080
- Navigate to Topics → user-events
- View messages in real-time

**Using Command Line**
```bash
docker exec -it user-microservice-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

For complete Kafka documentation, see [KAFKA_INTEGRATION.md](KAFKA_INTEGRATION.md)

## Project Structure

```
user-microservice/
├── src/main/java/com/microservice/
│   ├── controller/          # REST endpoints
│   ├── service/             # Business logic (with caching)
│   ├── repository/          # Data access
│   ├── model/               # Entities
│   ├── dto/                 # Data Transfer Objects
│   ├── event/               # Event DTOs
│   ├── kafka/               # Kafka producers/consumers
│   ├── config/              # Configuration
│   │   ├── RedisCacheConfig.java      # Cache configuration
│   │   ├── KafkaProducerConfig.java   # Kafka producer
│   │   ├── KafkaConsumerConfig.java   # Kafka consumer
│   │   └── MongoConfig.java           # MongoDB config
│   └── exception/           # Exception handling
├── src/main/resources/      # Configuration files
├── src/test/                # Tests
├── pom.xml                  # Maven dependencies
├── docker-compose.yml       # Docker setup (MongoDB, Kafka, Zookeeper, Redis)
├── kafka-setup.sh/bat       # Kafka configuration script
├── KAFKA_INTEGRATION.md     # Kafka integration guide
├── REDIS_CACHING.md         # Redis caching guide
├── REDIS_INTEGRATION_SUMMARY.md # Redis summary
└── README.md                # Detailed documentation
```

## Technologies

- **Language**: Java 17
- **Framework**: Spring Boot 3.1.5
- **Database**: MongoDB
- **Caching**: Redis (with Spring Cache)
- **Messaging**: Apache Kafka
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
