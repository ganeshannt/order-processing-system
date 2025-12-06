#!/bin/bash
# Script to build the Docker image from the project root
# This is necessary because the build requires the parent pom.xml

# Get the absolute path of the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"

# Go to the project root (one level up)
cd "$SCRIPT_DIR/.." || exit

echo "Extracting version from pom.xml..."
VERSION=$(mvn -f order-processing-system/pom.xml help:evaluate -Dexpression=project.version -q -DforceStdout)
echo "Detected version: $VERSION"

IMAGE_NAME="order-processing-system:$VERSION"

echo "Building Docker image: $IMAGE_NAME..."
docker build -f order-processing-system/Dockerfile -t "$IMAGE_NAME" .

echo "Loading image into Minikube..."
minikube image load "$IMAGE_NAME"

echo "Done! Image $IMAGE_NAME built and loaded."
