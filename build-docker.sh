#!/bin/bash

# Build script for job-navigator-service Docker image
# This script builds the application locally first, then creates Docker image

echo "🔨 Building job-navigator-service JAR..."
export JAVA_HOME=$(/usr/libexec/java_home)
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo "❌ Gradle build failed!"
    exit 1
fi

echo "🐳 Building Docker image..."
docker build -t asyncsite/job-navigator-service:latest .

if [ $? -ne 0 ]; then
    echo "❌ Docker build failed!"
    exit 1
fi

echo "✅ Build complete! Image: asyncsite/job-navigator-service:latest"
echo ""
echo "To run the service locally with Docker Compose:"
echo "  docker-compose up -d"
echo ""
echo "To run the service standalone (with existing infrastructure):"
echo "  docker-compose -f docker-compose.job-navigator-only.yml up -d"