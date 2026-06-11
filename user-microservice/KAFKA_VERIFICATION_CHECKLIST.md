# Kafka Integration Verification Checklist

## File Structure Verification

### New Java Classes
- [ ] `src/main/java/com/microservice/event/UserEvent.java` - Event DTO
- [ ] `src/main/java/com/microservice/config/KafkaProducerConfig.java` - Producer configuration
- [ ] `src/main/java/com/microservice/config/KafkaConsumerConfig.java` - Consumer configuration
- [ ] `src/main/java/com/microservice/kafka/KafkaUserEventProducer.java` - Producer service
- [ ] `src/main/java/com/microservice/kafka/KafkaUserEventConsumer.java` - Consumer service

### Updated Files
- [ ] `pom.xml` - Added Kafka dependencies
- [ ] `src/main/resources/application.yml` - Added Kafka configuration
- [ ] `src/main/java/com/microservice/service/UserService.java` - Added producer calls
- [ ] `docker-compose.yml` - Added Kafka and Zookeeper services

### Documentation
- [ ] `KAFKA_INTEGRATION.md` - Complete Kafka guide
- [ ] `KAFKA_INTEGRATION_SUMMARY.md` - Summary of changes
- [ ] `QUICKSTART.md` - Updated with Kafka info
- [ ] `kafka-setup.sh` - Linux/Mac setup script
- [ ] `kafka-setup.bat` - Windows setup script
- [ ] `Postman_Collection_Kafka.json` - API testing collection

## Code Verification

### Dependencies (pom.xml)
```bash
# Check for these dependencies
grep -n "spring-kafka" pom.xml
grep -n "jackson-databind" pom.xml
```

Expected output:
- `<artifactId>spring-kafka</artifactId>`
- `<artifactId>jackson-databind</artifactId>`

### UserService Integration
```bash
# Check if KafkaUserEventProducer is injected and used
grep -n "KafkaUserEventProducer" \
  src/main/java/com/microservice/service/UserService.java
```

Expected occurrences:
- Dependency injection in constructor
- Method calls in createUser, updateUser, deleteUser, deactivateUser

### Configuration
```bash
# Verify Kafka configuration in application.yml
grep -A 20 "kafka:" src/main/resources/application.yml
```

Expected:
- bootstrap-servers: localhost:9092
- Producer and Consumer configurations
- Topics configuration

## Docker Verification

### Docker Compose Services
```bash
# Check for new services in docker-compose.yml
grep "container_name:" docker-compose.yml
```

Expected services:
- user-microservice-mongodb
- user-microservice-mongo-express
- user-microservice-zookeeper
- user-microservice-kafka
- user-microservice-kafka-ui

### Volumes
```bash
# Check for new volumes
grep "volumes:" docker-compose.yml | head -5
```

Expected volumes:
- zookeeper_data
- zookeeper_logs
- kafka_data

## Build Verification

### Maven Build
```bash
# Clean build and verify dependencies resolve
mvn clean install

# Expected: BUILD SUCCESS
# If error: Check pom.xml syntax and Kafka dependency versions
```

## Runtime Verification

### Start Services
```bash
# Start all services
docker-compose up -d

# Verify services are running
docker-compose ps

# Expected: All services in "Up" state
```

### Start Application
```bash
# Start the Spring Boot application
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Expected output:
# - No Kafka connection errors
# - Application started successfully
# - Port 8080 is listening
```

### Verify Kafka Connection
```bash
# Check application logs for Kafka connection
grep -i "kafka" application_logs.txt

# Expected:
# - No errors related to Kafka bootstrap servers
# - Consumer group registered
# - Topic listener initialized
```

## Functional Verification

### Create User and Generate Event
```bash
# Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Test",
    "lastName": "User",
    "email": "test@example.com",
    "phoneNumber": "+1234567890",
    "address": "123 Test St",
    "city": "TestCity",
    "state": "TS",
    "zipCode": "12345",
    "country": "USA"
  }'

# Expected response:
# - 201 CREATED status
# - User ID in response body
```

### Verify Event in Kafka
```bash
# Check messages in Kafka topic
docker exec -it user-microservice-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic user-events \
  --from-beginning \
  --max-messages 1

# Expected:
# - JSON event message with eventType "CREATED"
# - User details in the message
```

### Test Other Events
```bash
# Update user (creates UPDATED event)
curl -X PUT http://localhost:8080/api/v1/users/{user_id} \
  -H "Content-Type: application/json" \
  -d '{...updated data...}'

# Deactivate user (creates DEACTIVATED event)
curl -X PATCH http://localhost:8080/api/v1/users/{user_id}/deactivate

# Delete user (creates DELETED event)
curl -X DELETE http://localhost:8080/api/v1/users/{user_id}
```

## Component Testing

### KafkaUserEventProducer Test
- [ ] `publishUserCreatedEvent()` publishes to Kafka
- [ ] `publishUserUpdatedEvent()` publishes to Kafka
- [ ] `publishUserDeletedEvent()` publishes to Kafka
- [ ] `publishUserDeactivatedEvent()` publishes to Kafka
- [ ] All events have correct structure

### KafkaUserEventConsumer Test
- [ ] Consumer receives messages from topic
- [ ] Correct event handler is called for each event type
- [ ] Manual acknowledgment is sent after processing
- [ ] Error handling works correctly

### Configuration Tests
- [ ] KafkaProducerConfig bean is created
- [ ] KafkaConsumerConfig bean is created
- [ ] KafkaTemplate is available for injection
- [ ] Consumer group is registered
- [ ] Topic is created or already exists

## Integration Tests

### End-to-End Flow
- [ ] Create user → MongoDB save → Kafka event published
- [ ] Event appears in Kafka topic within 5 seconds
- [ ] Event contains all required fields
- [ ] Event timestamp is correct
- [ ] Event source is set to application name

### Consumer Processing
- [ ] Consumer processes event after publication
- [ ] Event type handlers are called correctly
- [ ] No duplicate processing of same event
- [ ] Failed messages don't crash consumer

## Monitoring Verification

### Kafka UI Access
```bash
# Verify Kafka UI is accessible
curl http://localhost:8080/api/health || open http://localhost:8080
```

Expected:
- Kafka UI dashboard loads
- Topic list shows "user-events"
- Message count increases after user operations

### Logs
```bash
# Check application logs
docker logs user-microservice | grep -i kafka

# Check Kafka logs
docker logs user-microservice-kafka | tail -20
```

Expected:
- No error messages
- Consumer group connecting successfully
- Messages being processed

## Cleanup Verification

### Stop Services
```bash
# Stop all services
docker-compose down

# Verify all containers are stopped
docker ps | grep user-microservice

# Expected: No output (all containers stopped)
```

### Preserve Volumes (if needed)
```bash
# Keep data volumes for next run
docker-compose down
# Data persists in Docker volumes

# Remove volumes if needed
docker-compose down -v
```

## Documentation Verification

### Content Checks
- [ ] KAFKA_INTEGRATION.md explains architecture
- [ ] KAFKA_INTEGRATION.md includes event schemas
- [ ] KAFKA_INTEGRATION.md has troubleshooting section
- [ ] QUICKSTART.md mentions Kafka setup
- [ ] Setup scripts have clear instructions
- [ ] Postman collection has all endpoints
- [ ] Comments in code explain purpose

### Links Verification
- [ ] All markdown links are valid
- [ ] Code references point to correct files
- [ ] Examples are accurate
- [ ] Commands are copy-paste ready

## Performance Checks

### Producer Performance
```bash
# Monitor producer metrics
# Check batch sizes and latency in logs
# Expected: Batches of messages being sent together
```

### Consumer Performance
```bash
# Monitor consumer lag
docker exec user-microservice-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group user-service-group \
  --describe

# Expected: LAG should be 0 or very low
```

## Security Checks

- [ ] Credentials not hardcoded (use environment variables)
- [ ] No sensitive data in event logs
- [ ] Consumer acknowledges messages properly
- [ ] Error messages don't expose system details

## Final Verification Steps

1. **Clean Start Test**
   ```bash
   docker-compose down -v
   mvn clean
   docker-compose up -d
   mvn install
   mvn spring-boot:run
   # Test creating a user
   ```

2. **Performance Test**
   ```bash
   # Create 100 users in rapid succession
   # Verify all events appear in Kafka
   # Check consumer lag remains acceptable
   ```

3. **Error Handling Test**
   ```bash
   # Simulate network issues
   # Stop Kafka broker and restart
   # Verify reconnection logic works
   ```

4. **Documentation Test**
   ```bash
   # Follow KAFKA_INTEGRATION.md exactly
   # Verify all steps work
   # Check for typos or missing information
   ```

## Sign-Off

- [ ] All files verified and in place
- [ ] Code compiles without errors
- [ ] All services start successfully
- [ ] Basic functionality works
- [ ] Events are published to Kafka
- [ ] Events are visible in Kafka topic
- [ ] Documentation is complete and accurate
- [ ] Setup scripts are executable
- [ ] Integration is production-ready

---

## Notes

- Keep this checklist for future reference
- Use it before deployment to production
- Update as new features are added
- Share with team members for review
