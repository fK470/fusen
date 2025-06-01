# Use a base image with Maven and Java 21
FROM maven:3.9-amazoncorretto-21 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files (pom.xml)
COPY apps/backend/pom.xml ./

# Download dependencies (this will be cached)
RUN mvn dependency:go-offline

# Copy the rest of the application code
COPY apps/backend/src ./src

# Build the application
RUN mvn package -DskipTests

# Stage 2: Runtime image
FROM amazoncorretto:21-alpine-jdk

WORKDIR /app

# Copy the JAR file from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your application runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"]
