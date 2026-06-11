#!/bin/bash

# Kafka Setup and Testing Script for User Microservice

set -e

echo "========================================"
echo "Kafka Setup and Testing Script"
echo "========================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${GREEN}[✓]${NC} $1"
}

print_error() {
    echo -e "${RED}[✗]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[i]${NC} $1"
}

# Check if Docker is running
print_info "Checking Docker daemon..."
if ! docker ps > /dev/null 2>&1; then
    print_error "Docker daemon is not running. Please start Docker and try again."
    exit 1
fi
print_status "Docker is running"

# Function to start services
start_services() {
    print_info "Starting Docker containers..."
    docker-compose up -d
    
    print_info "Waiting for services to be ready..."
    sleep 15
    
    # Check if all services are running
    if docker-compose ps | grep -q "Up"; then
        print_status "All services are running"
    else
        print_error "Some services failed to start"
        docker-compose logs
        exit 1
    fi
}

# Function to create Kafka topic
create_kafka_topic() {
    print_info "Creating Kafka topics..."
    
    docker exec user-microservice-kafka kafka-topics \
        --bootstrap-server localhost:9092 \
        --create \
        --topic user-events \
        --partitions 3 \
        --replication-factor 1 \
        --config retention.ms=604800000 \
        || print_status "Topic already exists"
    
    print_status "Kafka topics configured"
}

# Function to list Kafka topics
list_topics() {
    print_info "Available Kafka topics:"
    docker exec user-microservice-kafka kafka-topics \
        --bootstrap-server localhost:9092 \
        --list
}

# Function to describe topic
describe_topic() {
    print_info "Topic configuration:"
    docker exec user-microservice-kafka kafka-topics \
        --bootstrap-server localhost:9092 \
        --describe \
        --topic user-events
}

# Function to monitor messages in real-time
monitor_messages() {
    print_info "Monitoring messages from user-events topic..."
    print_info "Press Ctrl+C to stop monitoring"
    sleep 2
    
    docker exec -it user-microservice-kafka kafka-console-consumer \
        --bootstrap-server localhost:9092 \
        --topic user-events \
        --from-beginning
}

# Function to test producer
test_producer() {
    print_info "Testing message production..."
    
    # Create a test message
    TEST_MESSAGE='{"event_id":"test-123","event_type":"CREATED","user_id":"user-123","email":"test@example.com","first_name":"John","last_name":"Doe","timestamp":"2024-01-15T10:00:00","source":"user-microservice"}'
    
    echo "$TEST_MESSAGE" | docker exec -i user-microservice-kafka kafka-console-producer \
        --broker-list localhost:9092 \
        --topic user-events \
        --property "parse.key=false"
    
    print_status "Test message sent"
}

# Function to check consumer group status
check_consumer_status() {
    print_info "Checking consumer group status..."
    docker exec user-microservice-kafka kafka-consumer-groups \
        --bootstrap-server localhost:9092 \
        --group user-service-group \
        --describe || print_info "Consumer group not active yet"
}

# Function to show service URLs
show_urls() {
    echo ""
    echo "========================================"
    echo "Service URLs"
    echo "========================================"
    echo -e "${GREEN}Application:${NC} http://localhost:8080/api"
    echo -e "${GREEN}MongoDB Express:${NC} http://localhost:8081"
    echo -e "${GREEN}Kafka UI:${NC} http://localhost:8080"
    echo -e "${GREEN}Kafka Bootstrap:${NC} localhost:9092"
    echo -e "${GREEN}Zookeeper:${NC} localhost:2181"
    echo ""
}

# Main menu
show_menu() {
    echo ""
    echo "========================================"
    echo "Kafka Setup Options"
    echo "========================================"
    echo "1. Start all services"
    echo "2. Create Kafka topics"
    echo "3. List Kafka topics"
    echo "4. Describe user-events topic"
    echo "5. Monitor messages (real-time)"
    echo "6. Send test message"
    echo "7. Check consumer status"
    echo "8. Show service URLs"
    echo "9. Stop all services"
    echo "0. Exit"
    echo ""
    read -p "Select an option (0-9): " choice
    
    case $choice in
        1) start_services ;;
        2) create_kafka_topic ;;
        3) list_topics ;;
        4) describe_topic ;;
        5) monitor_messages ;;
        6) test_producer ;;
        7) check_consumer_status ;;
        8) show_urls ;;
        9) 
            print_info "Stopping all services..."
            docker-compose down
            print_status "Services stopped"
            ;;
        0) 
            print_status "Exiting"
            exit 0
            ;;
        *)
            print_error "Invalid option"
            ;;
    esac
    
    # Show menu again
    show_menu
}

# Run initial setup
print_info "Starting Kafka setup..."
print_info "This script will help you set up and test Kafka integration"
echo ""

# Check if services are already running
if docker-compose ps | grep -q "user-microservice-kafka"; then
    print_status "Services already running"
else
    print_info "Starting services for the first time..."
    start_services
    create_kafka_topic
    show_urls
fi

# Show menu
show_menu
