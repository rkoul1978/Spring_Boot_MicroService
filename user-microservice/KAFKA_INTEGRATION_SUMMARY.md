# Kafka Integration Summary

## Overview
Apache Kafka has been successfully integrated into the user-microservice project to enable event-driven architecture and asynchronous communication.

## Changes Made

### 1. Dependencies Added (pom.xml)
- `spring-kafka`: Spring Boot Kafka integration
- `jackson-databind`: JSON serialization for Kafka messages

### 2. New Java Classes Created

#### Event Models
- **[UserEvent.java](src/main/java/com/microservice/event/UserEvent.java)**
  - Data Transfer Object for user events
  - Contains all user information and event metadata
  - Fields: eventId, eventType, userId, email, firstName, lastName, etc.

#### Kafka Configuration
- **[KafkaProducerConfig.java](src/main/java/com/microservice/config/KafkaProducerConfig.java)**
  - Configures Kafka producer with JSON serialization
  - Settings: acks=all, retries=3, batching, compression
  - Provides `KafkaTemplate<String, UserEvent>` bean

- **[KafkaConsumerConfig.java](src/main/java/com/microservice/config/KafkaConsumerConfig.java)**
  - Configures Kafka consumer with JSON deserialization
  - Settings: manual acknowledgment, concurrency, group management
  - Provides `ConcurrentMessageListenerContainer` bean

#### Kafka Services
- **[KafkaUserEventProducer.java](src/main/java/com/microservice/kafka/KafkaUserEventProducer.java)**
  - Publishes user events to Kafka
  - Methods:
    - `publishUserCreatedEvent(UserDTO)`
    - `publishUserUpdatedEvent(UserDTO)`
    - `publishUserDeletedEvent(UserDTO)`
    - `publishUserDeactivatedEvent(UserDTO)`

- **[KafkaUserEventConsumer.java](src/main/java/com/microservice/kafka/KafkaUserEventConsumer.java)**
  - Consumes user events from Kafka topic
  - Listens on `user-events` topic
  - Routes events to appropriate handlers based on event type
  - Implements manual acknowledgment for reliability

### 3. Service Integration

#### Updated UserService.java
- Injected `KafkaUserEventProducer`
- Event publishing on user operations:
  - `createUser()` → publishes CREATED event
  - `updateUser()` → publishes UPDATED event
  - `deleteUser()` → publishes DELETED event
  - `deactivateUser()` → publishes DEACTIVATED event

### 4. Configuration Files

#### application.yml
Added complete Kafka configuration:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      # Serialization, acks, retries, batching settings
    consumer:
      # Deserialization, group, offset, concurrency settings
    topics:
      user-events: user-events
```

### 5. Docker Support

#### Updated docker-compose.yml
Added new services:
- **Zookeeper** (port 2181): Coordination for Kafka cluster
- **Kafka** (port 9092): Message broker
- **Kafka UI** (port 8080): Web-based Kafka management interface

Storage volumes:
- `zookeeper_data`: Zookeeper data persistence
- `zookeeper_logs`: Zookeeper logs persistence
- `kafka_data`: Kafka data persistence

### 6. Documentation

#### [KAFKA_INTEGRATION.md](KAFKA_INTEGRATION.md)
Comprehensive guide covering:
- Architecture and design patterns
- Event types and schemas
- Configuration options
- Setup and startup instructions
- Testing procedures
- Best practices
- Troubleshooting guide

#### [QUICKSTART.md](QUICKSTART.md) - Updated
- Added Kafka setup instructions
- Event monitoring guidance
- Updated project structure
- Service URLs for Kafka UI

#### Setup Scripts
- **[kafka-setup.sh](kafka-setup.sh)** (Linux/Mac)
  - Interactive menu for Kafka operations
  - Create topics, monitor messages, test producer
  - Check consumer status, view configurations

- **[kafka-setup.bat](kafka-setup.bat)** (Windows)
  - Windows batch version of setup script
  - Same functionality as bash script

#### [Postman_Collection_Kafka.json](Postman_Collection_Kafka.json)
Collection with pre-configured requests:
- Create users (triggers CREATED events)
- Update users (triggers UPDATED events)
- Delete users (triggers DELETED events)
- Deactivate users (triggers DEACTIVATED events)
- Query operations (get by ID, email, city, etc.)
- Variable support for user IDs

## Event Publishing Flow

```
User API Request
    ↓
UserController
    ↓
UserService
    ↓
UserRepository (save to MongoDB)
    ↓
KafkaUserEventProducer
    ↓
Kafka Topic (user-events)
    ↓
KafkaUserEventConsumer (if present)
    ↓
Other Microservices / External Systems
```

## Architecture Benefits

1. **Decoupling**: Services can operate independently
2. **Scalability**: Easy to add new consumers
3. **Reliability**: Asynchronous processing with retry logic
4. **Auditability**: All events are tracked and stored
5. **Integration**: External systems can consume events

## Getting Started

### Quick Start (Docker)
```bash
# Start all services
docker-compose up -d

# Build and run application
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Access services
- API: http://localhost:8080/api
- Kafka UI: http://localhost:8080
- MongoDB UI: http://localhost:8081
```

### Using Setup Scripts
```bash
# Windows
./kafka-setup.bat

# Linux/Mac
chmod +x kafka-setup.sh
./kafka-setup.sh
```

### Create a User and Monitor Events
```bash
# Create user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }'

# Monitor messages in Kafka UI or using CLI
docker exec -it user-microservice-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning
```

## Configuration Customization

### Development (application-dev.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: user-service-group-dev
```

### Production
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-prod-1:9092,kafka-prod-2:9092,kafka-prod-3:9092
    producer:
      compression-type: snappy
      acks: all
      retries: 5
    consumer:
      max-poll-records: 100
      session-timeout-ms: 30000
```

## Key Features Implemented

✅ Event-driven architecture  
✅ Producer publishes on CRUD operations  
✅ Consumer example implementation  
✅ JSON serialization for events  
✅ Manual acknowledgment for reliability  
✅ Docker containerization  
✅ Comprehensive documentation  
✅ Setup automation scripts  
✅ Postman collection for testing  
✅ Kafka UI for monitoring  

## Next Steps for Enhancement

1. **Implement business logic** in `KafkaUserEventConsumer`
   - Send welcome emails on user creation
   - Update search indexes on user update
   - Trigger cleanup on user deletion

2. **Add Dead Letter Queue (DLQ)** for failed messages

3. **Implement Schema Registry** for event schema management

4. **Add Metrics** with Prometheus/Grafana integration

5. **Create consumer microservices** that react to events
   - Notification Service
   - Analytics Service
   - Email Service
   - Audit Service

6. **Add Integration Tests** for Kafka functionality
   - Producer tests
   - Consumer tests
   - End-to-end event flow tests

7. **Implement Retry Logic** with exponential backoff

8. **Add Security** with SSL/TLS and SASL authentication

## Testing Checklist

- [ ] Start docker-compose services successfully
- [ ] Create a user and verify CREATED event in Kafka
- [ ] Update a user and verify UPDATED event in Kafka
- [ ] Deactivate a user and verify DEACTIVATED event in Kafka
- [ ] Delete a user and verify DELETED event in Kafka
- [ ] Monitor events using Kafka UI
- [ ] Monitor events using CLI consumer
- [ ] Check consumer group status
- [ ] Verify no errors in application logs
- [ ] Test with Postman collection
- [ ] Verify topic configuration

## Troubleshooting

### Services not starting
```bash
docker-compose logs
docker ps -a
```

### Topics not created
```bash
docker exec user-microservice-kafka kafka-topics \
  --bootstrap-server localhost:9092 --create \
  --topic user-events --partitions 3 --replication-factor 1
```

### Connection refused
- Verify Kafka is running: `docker ps | grep kafka`
- Check bootstrap-servers in application.yml
- Ensure port 9092 is not blocked

### Messages not appearing
- Verify producer is sending (check logs)
- Verify consumer is subscribed to correct topic
- Check consumer group offset

For more help, see [KAFKA_INTEGRATION.md](KAFKA_INTEGRATION.md#troubleshooting)

## References

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Confluent Cloud Documentation](https://docs.confluent.io/)
- [Docker Documentation](https://docs.docker.com/)
