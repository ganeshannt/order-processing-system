#!/bin/bash
# Script to run the Docker image locally

IMAGE_NAME="order-processing-system"
TAG="1.0.1-SNAPSHOT"

echo "Running $IMAGE_NAME:$TAG..."
docker run -p 8080:8080 $IMAGE_NAME:$TAG
