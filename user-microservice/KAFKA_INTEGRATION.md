# Apache Kafka Integration Guide

## Overview

Apache Kafka has been integrated into the user-microservice to enable event-driven architecture. The microservice now publishes user-related events to Kafka topics, allowing other services to consume these events asynchronously.

## Architecture

The Kafka integration follows a **Producer-Consumer** pattern:

```
┌─────────────────────┐
│  User Microservice  │
│   (Producer)        │
└──────────┬──────────┘
           │
           ├─ User Created Events
           ├─ User Updated Events
           ├─ User Deleted Events
           └─ User Deactivated Events
                    ↓
        ┌───────────────────────┐
        │  Kafka Topic:         │
        │  user-events          │
        └───────────────────────┘
                    ↓
        ┌─────────────────────────────┐
        │  Consumers (Other Services) │
        │  - Notification Service     │
        │  - Analytics Service        │
        │  - Email Service            │
        │  - Cache Service            │
        └─────────────────────────────┘
```

## Event Types

The microservice publishes the following events:

### 1. **User Created Event**
Triggered when a new user is created.
```json
{
  "event_id": "uuid",
  "event_type": "CREATED",
  "user_id": "user_id",
  "email": "user@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "phone_number": "+1234567890",
  "address": "123 Main St",
  "city": "New York",
  "state": "NY",
  "zip_code": "10001",
  "country": "USA",
  "is_active": true,
  "timestamp": "2024-01-15T10:30:00",
  "source": "user-microservice"
}
```

### 2. **User Updated Event**
Triggered when user information is modified.
```json
{
  "event_id": "uuid",
  "event_type": "UPDATED",
  "user_id": "user_id",
  "email": "user@example.com",
  "first_name": "Jane",
  "last_name": "Smith",
  "phone_number": "+1987654321",
  "address": "456 Oak Ave",
  "city": "Boston",
  "state": "MA",
  "zip_code": "02101",
  "country": "USA",
  "is_active": true,
  "timestamp": "2024-01-15T11:45:00",
  "source": "user-microservice"
}
```

### 3. **User Deleted Event**
Triggered when a user is deleted from the system.
```json
{
  "event_id": "uuid",
  "event_type": "DELETED",
  "user_id": "user_id",
  "email": "user@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "timestamp": "2024-01-15T12:00:00",
  "source": "user-microservice"
}
```

### 4. **User Deactivated Event**
Triggered when a user account is deactivated.
```json
{
  "event_id": "uuid",
  "event_type": "DEACTIVATED",
  "user_id": "user_id",
  "email": "user@example.com",
  "first_name": "John",
  "last_name": "Doe",
  "is_active": false,
  "timestamp": "2024-01-15T13:15:00",
  "source": "user-microservice"
}
```

## Project Structure

New Kafka-related files have been added:

```
src/main/java/com/microservice/
├── event/
│   └── UserEvent.java                 # Event DTO
├── kafka/
│   ├── KafkaUserEventProducer.java     # Producer service
│   └── KafkaUserEventConsumer.java     # Consumer service
└── config/
    ├── KafkaProducerConfig.java        # Producer configuration
    └── KafkaConsumerConfig.java        # Consumer configuration
```

## Configuration

### application.yml - Kafka Configuration

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      retries: 3
      batch-size: 16384
      linger-ms: 10
      buffer-memory: 33554432
    
    consumer:
      group-id: user-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      auto-offset-reset: earliest
      max-poll-records: 100
      session-timeout-ms: 30000
    
    topics:
      user-events: user-events
```

### Environment-Specific Configuration

#### Development (application-dev.yml)
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
```

#### Production
```yaml
spring:
  kafka:
    bootstrap-servers: kafka-broker-1:9092,kafka-broker-2:9092,kafka-broker-3:9092
    producer:
      acks: all
      retries: 5
      compression-type: snappy
    consumer:
      max-poll-records: 100
      session-timeout-ms: 30000
```

## Getting Started

### 1. Start Kafka Services with Docker Compose

```bash
# From the project root directory
docker-compose up -d

# Verify services are running
docker-compose ps
```

This will start:
- **MongoDB** (port 27017)
- **Kafka** (port 9092)
- **Zookeeper** (port 2181)
- **Kafka UI** (port 8080) - Web UI for Kafka management

### 2. Verify Kafka Installation

```bash
# Access Kafka container
docker exec -it user-microservice-kafka bash

# List topics
kafka-topics --bootstrap-server localhost:9092 --list

# Create topics (if auto-creation is disabled)
kafka-topics --bootstrap-server localhost:9092 \
  --create \
  --topic user-events \
  --partitions 3 \
  --replication-factor 1
```

### 3. Start the Microservice

```bash
# Build and run
mvn clean install
mvn spring-boot:run
```

Or with development profile:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 4. Access Services

- **Application**: http://localhost:8080/api
- **MongoDB Express**: http://localhost:8081
- **Kafka UI**: http://localhost:8080 (Note: adjust if port conflicts)

## Testing Kafka Integration

### 1. Create a User (Generate Event)

```bash
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  }'
```

### 2. Monitor Kafka Messages

#### Using Kafka UI (Web Interface)
- Open: http://localhost:8080
- Navigate to Topics → user-events
- View messages in real-time

#### Using Command Line

```bash
# Consume messages from the beginning
docker exec -it user-microservice-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning

# Consume messages from the latest offset
docker exec -it user-microservice-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events
```

### 3. Producer Performance Testing

```bash
# Send test messages
docker exec -it user-microservice-kafka kafka-producer-perf-test \
  --topic user-events \
  --num-records 1000 \
  --record-size 1024 \
  --throughput 1000 \
  --producer-props bootstrap.servers=localhost:9092
```

## Key Classes

### UserEvent (event/UserEvent.java)
Data Transfer Object for user events. Contains all user information and event metadata.

### KafkaUserEventProducer (kafka/KafkaUserEventProducer.java)
Service for publishing user events:
- `publishUserCreatedEvent()` - Publishes when user is created
- `publishUserUpdatedEvent()` - Publishes when user is updated
- `publishUserDeletedEvent()` - Publishes when user is deleted
- `publishUserDeactivatedEvent()` - Publishes when user is deactivated

### KafkaUserEventConsumer (kafka/KafkaUserEventConsumer.java)
Service for consuming user events:
- Processes events from `user-events` topic
- Routes to appropriate handlers based on event type
- Implements manual acknowledgment for reliable processing

### KafkaProducerConfig (config/KafkaProducerConfig.java)
Spring configuration for Kafka producer with:
- JSON serialization
- Acks configuration (all)
- Retry logic
- Batching and buffer settings

### KafkaConsumerConfig (config/KafkaConsumerConfig.java)
Spring configuration for Kafka consumer with:
- JSON deserialization
- Consumer group management
- Manual acknowledgment
- Concurrency settings

## Integrating with UserService

The `UserService` now publishes events:

```java
public UserDTO createUser(UserDTO userDTO) {
    // ... save user logic ...
    kafkaUserEventProducer.publishUserCreatedEvent(createdUserDTO);
    return createdUserDTO;
}

public UserDTO updateUser(String id, UserDTO userDTO) {
    // ... update user logic ...
    kafkaUserEventProducer.publishUserUpdatedEvent(updatedUserDTO);
    return updatedUserDTO;
}

public void deleteUser(String id) {
    // ... delete user logic ...
    kafkaUserEventProducer.publishUserDeletedEvent(deletedUserDTO);
}

public void deactivateUser(String id) {
    // ... deactivate user logic ...
    kafkaUserEventProducer.publishUserDeactivatedEvent(deactivatedUserDTO);
}
```

## Best Practices

### Producer
1. **Error Handling**: Implement DLQ (Dead Letter Queue) for failed messages
2. **Compression**: Use `snappy` or `lz4` compression for large payloads
3. **Idempotence**: Enable idempotent producer to prevent duplicates
4. **Monitoring**: Track producer metrics (latency, throughput, failures)

### Consumer
1. **Offset Management**: Use manual acknowledgment for critical operations
2. **Concurrency**: Adjust concurrency based on throughput requirements
3. **Error Handling**: Implement proper error handling with exponential backoff
4. **Monitoring**: Monitor consumer lag and processing failures

### General
1. **Topic Partitioning**: Use user ID as key for better partitioning
2. **Retention**: Set appropriate retention policies based on business needs
3. **Schema Evolution**: Consider using Avro or Protobuf for schema management
4. **Security**: Use SSL/TLS and SASL authentication in production
5. **Metrics**: Export metrics to Prometheus/Grafana for monitoring

## Troubleshooting

### Issue: "Unable to connect to broker"
```bash
# Check if Kafka is running
docker ps | grep kafka

# Check Kafka logs
docker logs user-microservice-kafka

# Verify bootstrap-servers configuration
grep bootstrap-servers application.yml
```

### Issue: "Messages not appearing in topic"
```bash
# Verify producer is sending messages
# Check application logs for KafkaUserEventProducer

# Verify topic exists
docker exec user-microservice-kafka kafka-topics --bootstrap-server localhost:9092 --list

# Check topic configuration
docker exec user-microservice-kafka kafka-topics --bootstrap-server localhost:9092 --describe --topic user-events
```

### Issue: "Consumer group lag"
```bash
# Check consumer group status
docker exec user-microservice-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group user-service-group \
  --describe
```

## Stopping Services

```bash
# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Additional Resources

- [Apache Kafka Documentation](https://kafka.apache.org/documentation/)
- [Spring Kafka Documentation](https://spring.io/projects/spring-kafka)
- [Confluent Cloud Documentation](https://docs.confluent.io/)
- [Kafka Best Practices](https://kafka.apache.org/documentation/#bestpractices)

## Next Steps

1. **Implement Consumer Logic**: Update `KafkaUserEventConsumer` with specific business logic
2. **Create DLQ**: Implement Dead Letter Queue for error handling
3. **Add Monitoring**: Integrate with Prometheus and Grafana
4. **Schema Registry**: Set up Confluent Schema Registry for schema management
5. **Multi-Service Integration**: Connect other microservices as consumers
6. **Testing**: Add integration tests for Kafka functionality
