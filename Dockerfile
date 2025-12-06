# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# IMPORTANT: This Dockerfile must be built from the PROJECT ROOT directory
# Command: docker build -f order-processing-system/Dockerfile -t order-processing-system .

# Only copy root pom first to leverage Docker cache
COPY pom.xml .

# Copy module POM
COPY order-processing-system/pom.xml order-processing-system/

# Download dependencies (this layer will be cached unless POMs change)
RUN mvn -f order-processing-system/pom.xml dependency:go-offline -B

# Copy sources
COPY order-processing-system/src order-processing-system/src

# Build only the module, skip tests for faster CI
RUN mvn -f order-processing-system/pom.xml -Dmaven.test.skip=true clean package

# ---- Run Stage ----
# Use Alpine JRE to reduce size
FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Copy only the built JAR
COPY --from=build /app/order-processing-system/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]