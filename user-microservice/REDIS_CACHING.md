# Redis Caching Integration Guide

## Overview

Redis caching has been integrated into the user-microservice to improve performance by reducing database queries and response times. The service automatically caches user-related queries with intelligent cache invalidation on write operations.

## Architecture

### Caching Strategy

```
Request → Check Redis Cache
    ↓
    ├─ Cache Hit → Return cached data (fast!)
    │
    └─ Cache Miss → Query MongoDB → Cache result → Return data
    
Write Operations (Create/Update/Delete/Deactivate)
    → Update MongoDB → Invalidate related caches
```

### Cache Layers

```
┌──────────────────────────────────────┐
│      User API Request                │
└──────────────────┬───────────────────┘
                   │
        ┌──────────▼──────────┐
        │   Spring Cache      │
        │   (Annotations)     │
        └──────────┬──────────┘
                   │
        ┌──────────▼──────────┐
        │   Redis Cache       │
        │   (TTL: 1 hour)     │
        └──────────┬──────────┘
                   │
        ┌──────────▼──────────┐
        │   MongoDB Database  │
        └─────────────────────┘
```

## Cached Operations

The following operations are cached with automatic TTL management:

### Read Operations (Cached)

| Cache Key | Method | TTL | Parameters |
|-----------|--------|-----|------------|
| `users` | `getAllUsers()` | 1 hour | None |
| `userById:{id}` | `getUserById(id)` | 1 hour | User ID |
| `userByEmail:{email}` | `getUserByEmail(email)` | 1 hour | Email address |
| `usersByCity:{city}` | `getUsersByCity(city)` | 1 hour | City name |
| `activeUsers` | `getActiveUsers()` | 1 hour | None |
| `usersByName:{firstName}_{lastName}` | `getUsersByName(firstName, lastName)` | 1 hour | First and last name |

### Write Operations (Cache Invalidation)

| Operation | Method | Caches Invalidated |
|-----------|--------|-------------------|
| Create | `createUser()` | `users`, `activeUsers` |
| Update | `updateUser()` | All caches |
| Delete | `deleteUser()` | All caches |
| Deactivate | `deactivateUser()` | All caches |

## Project Structure

New/Modified files:

```
src/main/java/com/microservice/
├── config/
│   └── RedisCacheConfig.java          # New: Redis configuration
└── service/
    └── UserService.java               # Updated: Added @Cacheable, @CacheEvict

src/main/resources/
└── application.yml                    # Updated: Redis configuration

docker-compose.yml                     # Updated: Added Redis and Redis Commander
```

## Configuration

### application.yml - Redis Configuration

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    timeout: 60000ms
    password: # Leave empty if no authentication required
    database: 0
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        shutdown-timeout: 100ms
    cache:
      ttl: 3600  # Cache TTL in seconds (1 hour)
      null-ttl: 300  # TTL for null values (5 minutes)
```

### Connection Parameters

- **Host**: `localhost` (default)
- **Port**: `6379` (default Redis port)
- **Database**: `0` (default)
- **Password**: Empty (no authentication by default)
- **Connection Pool**: 8 max connections
- **Cache TTL**: 1 hour (3600 seconds) for most caches
- **Null TTL**: 5 minutes (300 seconds) for null values

### Environment-Specific Configuration

#### Development (application-dev.yml)
```yaml
spring:
  redis:
    host: localhost
    port: 6379
```

#### Production
```yaml
spring:
  redis:
    host: redis-cluster.example.com
    port: 6379
    password: ${REDIS_PASSWORD}  # Use environment variables
    timeout: 60000ms
    lettuce:
      pool:
        max-active: 32
        max-idle: 16
```

## Getting Started

### 1. Start Redis Service with Docker Compose

```bash
# Start all services including Redis
docker-compose up -d

# Verify Redis is running
docker ps | grep redis
```

Expected output:
```
user-microservice-redis          redis:7-alpine
user-microservice-redis-commander rediscommander/redis-commander
```

### 2. Build and Run Application

```bash
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"
```

### 3. Verify Cache Configuration

Application logs should show:
```
2024-01-15 10:30:45 - Spring caching enabled
2024-01-15 10:30:46 - Redis connection established
2024-01-15 10:30:46 - Cache manager configured with Redis backend
```

### 4. Access Redis UI

**Redis Commander** - Web-based Redis management UI
- URL: http://localhost:8082
- View all cached keys
- Monitor cache hits/misses
- Inspect cache values
- Manually evict cache

## Cache Statistics and Monitoring

### View Cache Content

```bash
# Connect to Redis CLI
docker exec -it user-microservice-redis redis-cli

# View all keys
KEYS *

# Get cache statistics
INFO stats

# View specific cache
GET userById:user-123

# View cache TTL
TTL userById:user-123

# Clear all caches
FLUSHDB

# Monitor cache operations in real-time
MONITOR
```

### Cache Hit/Miss Monitoring

Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    org.springframework.cache: DEBUG
    org.springframework.data.redis: DEBUG
```

Log output will show:
```
[CACHE] Getting from cache: userById:user-123
[CACHE] Cache miss for key: userById:user-123 - fetching from database
[CACHE] Caching result for key: userById:user-123
[CACHE] Cache hit for key: userById:user-123
```

### Performance Metrics

Track cache performance:
```bash
# View cache statistics
docker exec user-microservice-redis redis-cli INFO stats

# View memory usage
docker exec user-microservice-redis redis-cli INFO memory

# View keyspace statistics
docker exec user-microservice-redis redis-cli INFO keyspace
```

## Cache Operations

### Explicit Cache Eviction

While cache eviction is automatic on write operations, you can manually clear caches:

```bash
# Clear specific cache
docker exec user-microservice-redis redis-cli DEL userById:user-123

# Clear all user-related caches
docker exec user-microservice-redis redis-cli FLUSHDB

# Pattern-based deletion (all users caches)
docker exec user-microservice-redis redis-cli EVAL \
  "return redis.call('del', unpack(redis.call('keys', ARGV[1])))" \
  0 "userBy*"
```

### Cache TTL Management

Default TTLs can be modified in `RedisCacheConfig.java`:

```java
// Current TTL: 1 hour (3600 seconds)
.entryTtl(Duration.ofSeconds(cacheTtlSeconds))

// Examples:
Duration.ofMinutes(30)      // 30 minutes
Duration.ofHours(2)         // 2 hours
Duration.ofSeconds(300)     // 5 minutes
Duration.ofDays(1)          // 1 day
```

## Testing Cache Integration

### 1. Test Cache Hit Performance

```bash
# First request (cache miss) - measure response time
curl -w "\n%{time_total}\n" http://localhost:8080/api/v1/users

# Second request (cache hit) - should be faster
curl -w "\n%{time_total}\n" http://localhost:8080/api/v1/users

# Third request (cache hit) - consistent fast response
curl -w "\n%{time_total}\n" http://localhost:8080/api/v1/users
```

Expected: Subsequent requests are 10-100x faster due to caching.

### 2. Test Cache Invalidation on Update

```bash
# 1. Create a user
curl -X POST http://localhost:8080/api/v1/users \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com",...}'

# 2. Get the user (cached)
curl http://localhost:8080/api/v1/users/USER_ID

# 3. Update the user (cache should be invalidated)
curl -X PUT http://localhost:8080/api/v1/users/USER_ID \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane",...}'

# 4. Get user again (new value from database)
curl http://localhost:8080/api/v1/users/USER_ID

# 5. Verify in Redis UI: cache should be empty or have new TTL
```

### 3. Cache Hit Rate Test

```bash
# Generate multiple requests
for i in {1..100}; do
  curl http://localhost:8080/api/v1/users
done

# Check Redis statistics
docker exec user-microservice-redis redis-cli INFO stats | grep hits
```

## Spring Cache Annotations

### @Cacheable
Reads from cache, or fetches from database if cache misses:
```java
@Cacheable(value = "userById", key = "#id")
public UserDTO getUserById(String id) { ... }
```

### @CacheEvict
Removes specific entries from cache:
```java
@CacheEvict(value = "userById", key = "#id")
public void deleteUser(String id) { ... }
```

### @CacheEvict with allEntries
Clears entire cache:
```java
@CacheEvict(value = {"users", "activeUsers"}, allEntries = true)
public UserDTO createUser(UserDTO userDTO) { ... }
```

## Troubleshooting

### Issue: Redis Connection Failed

```bash
# Check if Redis is running
docker ps | grep redis

# If not running, start it
docker-compose up -d redis

# Check Redis logs
docker logs user-microservice-redis

# Test connection
docker exec user-microservice-redis redis-cli ping
# Expected output: PONG
```

### Issue: Cache Not Being Used

```bash
# Check Redis CLI connection
docker exec -it user-microservice-redis redis-cli

# Verify keys exist
KEYS *

# Check if caching is enabled in application
# Look for this in logs: "Spring caching enabled"

# Enable debug logging in application.yml
logging:
  level:
    org.springframework.cache: DEBUG
```

### Issue: Cache Size Growing Too Large

```bash
# Check memory usage
docker exec user-microservice-redis redis-cli INFO memory

# View all keys
docker exec user-microservice-redis redis-cli KEYS "*" | wc -l

# Clear cache if needed
docker exec user-microservice-redis redis-cli FLUSHDB

# Configure eviction policy in application (already configured with LRU)
# See: maxmemory-policy allkeys-lru in docker-compose.yml
```

### Issue: Stale Cache Data

```bash
# Manually evict all caches
docker exec user-microservice-redis redis-cli FLUSHDB

# Or evict specific cache
docker exec user-microservice-redis redis-cli DEL userById:*
```

## Performance Benefits

### Typical Performance Improvement

| Operation | Without Cache | With Cache | Improvement |
|-----------|---------------|-----------|-------------|
| Get by ID | 50-100ms | 5-10ms | 10x faster |
| Get all users | 100-200ms | 10-20ms | 10x faster |
| Get by email | 50-100ms | 5-10ms | 10x faster |
| Get by city | 100-200ms | 10-20ms | 10x faster |
| Get active users | 100-200ms | 10-20ms | 10x faster |

### Resource Benefits

- **Reduced Database Load**: 80-90% fewer database queries
- **Lower Response Times**: 10-100x faster response times
- **Better Scalability**: Handle more concurrent users
- **Reduced Bandwidth**: Less data transfer between services

## Production Considerations

### 1. Redis Persistence
Configure AOF (Append-Only File) persistence:
```yaml
# In docker-compose.yml
command: redis-server --appendonly yes --appendfsync everysec
```

### 2. Redis Replication
Set up Redis Sentinel or Cluster for high availability:
```yaml
services:
  redis-primary:
    image: redis:7-alpine
  
  redis-replica:
    image: redis:7-alpine
    command: redis-server --slaveof redis-primary 6379
```

### 3. Redis Authentication
Enable password protection:
```yaml
spring:
  redis:
    password: ${REDIS_PASSWORD}

# In docker-compose.yml
command: redis-server --requirepass ${REDIS_PASSWORD}
```

### 4. Monitoring and Alerting
Implement monitoring:
- Cache hit/miss rate
- Memory usage
- Connection count
- Eviction rate

### 5. Cache Warming
Pre-load frequently accessed data on startup:
```java
@Component
public class CacheWarmer {
    @Autowired
    private UserService userService;
    
    @PostConstruct
    public void warmCache() {
        userService.getActiveUsers();
        userService.getAllUsers();
    }
}
```

## Best Practices

1. **Cache Invalidation**: Ensure all write operations invalidate related caches
2. **TTL Configuration**: Set appropriate TTLs based on data volatility
3. **Null Caching**: Cache null values with shorter TTL to avoid repeated DB queries
4. **Key Design**: Use consistent, hierarchical key naming (e.g., `userById:123`)
5. **Monitoring**: Monitor cache hit rates and memory usage
6. **Error Handling**: Handle cache failures gracefully; fall back to database
7. **Testing**: Test cache behavior under load
8. **Documentation**: Document cache strategies for team

## CLI Commands Reference

```bash
# Connect to Redis
docker exec -it user-microservice-redis redis-cli

# Common commands
PING                    # Test connection
KEYS *                  # List all keys
GET key                 # Get value
SET key value EX 3600   # Set with TTL
DEL key                 # Delete key
FLUSHDB                 # Clear all keys
DBSIZE                  # Number of keys
INFO                    # Server information
MONITOR                 # Monitor all commands
QUIT                    # Exit CLI
```

## Additional Resources

- [Redis Documentation](https://redis.io/documentation)
- [Spring Cache Documentation](https://spring.io/guides/gs/caching/)
- [Lettuce Documentation](https://lettuce.io/)
- [Redis Commander](https://joeferner.github.io/redis-commander/)

## Support

For issues or questions:
1. Check application logs: `docker logs user-microservice`
2. Check Redis logs: `docker logs user-microservice-redis`
3. View cache status: Redis Commander UI (http://localhost:8082)
4. Verify Redis connection: `docker exec user-microservice-redis redis-cli PING`
