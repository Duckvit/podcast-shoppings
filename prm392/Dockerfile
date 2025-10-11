# ===========================
# ️ Stage 1: Build the app
# ===========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Copy everything
WORKDIR /app
COPY pom.xml .
COPY src ./src

# Package project (skip tests for faster build)
RUN mvn clean package -DskipTests

# ===========================
# Stage 2: Run the app
# ===========================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# Expose port (same as your Spring Boot server.port)
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
