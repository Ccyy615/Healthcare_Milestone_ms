FROM gradle:8.14.4-jdk17 AS builder

WORKDIR /usr/src/app
COPY build.gradle .
COPY src ./src

RUN gradle bootJar --no-daemon

FROM eclipse-temurin:17-jdk

ARG JAR_FILE=build/libs/*.jar
COPY --from=builder /usr/src/app/${JAR_FILE} app.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker
ENTRYPOINT ["java", "-jar", "/app.jar"]
