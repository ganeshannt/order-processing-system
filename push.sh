#!/bin/bash
# Script to push the Docker image to Docker Hub

if [ -z "$1" ]; then
    echo "Usage: $0 <docker-hub-username>"
    exit 1
fi

DOCKER_USERNAME=$1
IMAGE_NAME="order-processing-system"
TAG="latest" # You can change this or make it an argument if needed

echo "Logging in to Docker Hub..."
docker login

echo "Tagging image..."
docker tag $IMAGE_NAME $DOCKER_USERNAME/$IMAGE_NAME:$TAG

echo "Pushing image to Docker Hub..."
docker push $DOCKER_USERNAME/$IMAGE_NAME:$TAG

echo "Done! Image pushed to $DOCKER_USERNAME/$IMAGE_NAME:$TAG"
