# ===========================
# ️ Stage 1: Build the app
# ===========================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Copy everything
WORKDIR /app
COPY pom.xml .
COPY src ./src

# ✅ Fix lỗi encoding cho Render
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Package project (skip tests for faster build)
RUN mvn clean package -DskipTests

# ===========================
# Stage 2: Run the app
# ===========================
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy JAR from builder
COPY --from=builder /app/target/*.jar app.jar

# ✅ Giữ UTF-8 khi runtime
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Expose port (same as your Spring Boot server.port)
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
