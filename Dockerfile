# ============================================
# STAGE 1: BUILD STAGE
# ============================================
FROM gradle:8.5-jdk17-alpine AS build

WORKDIR /app

# Copy Gradle configuration first (for caching)
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN chmod +x gradlew

# Download dependencies
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src ./src

# Build the Spring Boot jar
RUN ./gradlew clean bootJar --no-daemon -x test


# ============================================
# STAGE 2: RUNTIME STAGE
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Spring Boot port
EXPOSE 8080

# Environment variables
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS=""

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
 CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
