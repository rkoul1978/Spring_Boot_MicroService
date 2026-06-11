#!/bin/bash

echo "Starting User Microservice Setup..."

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "Error: Docker is not installed. Please install Docker first."
    exit 1
fi

echo "Starting MongoDB containers..."
docker-compose up -d

echo "Waiting for MongoDB to be ready..."
sleep 5

echo "Building Spring Boot application..."
mvn clean install

if [ $? -ne 0 ]; then
    echo "Error: Maven build failed!"
    exit 1
fi

echo ""
echo "=========================================="
echo "Setup completed successfully!"
echo "=========================================="
echo ""
echo "MongoDB Details:"
echo "  URI: mongodb://admin:password@localhost:27017"
echo "  Express UI: http://localhost:8081"
echo ""
echo "To start the application, run:"
echo "  mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'"
echo ""
echo "Or run the JAR:"
echo "  java -jar target/user-microservice-1.0.0.jar --spring.profiles.active=dev"
echo ""
