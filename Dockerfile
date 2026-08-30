# ==============================
# Build stage
# ==============================
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy Maven configuration
COPY pom.xml .

# Copy source code
COPY src ./src

# Build Spring Boot application
RUN mvn clean package -DskipTests

# ==============================
# Runtime stage
# ==============================
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Render will provide the PORT environment variable.
EXPOSE 8080

# Start Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]