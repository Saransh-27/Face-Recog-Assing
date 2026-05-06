# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create uploads directory for video/frame storage
RUN mkdir -p /app/uploads

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Environment variables (override at runtime)
ENV MONGODB_URI=mongodb://localhost:27017/facedb
ENV UPLOAD_DIR=/app/uploads

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
