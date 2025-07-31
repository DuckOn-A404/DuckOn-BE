# Build stage

FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY . .

RUN ./gradlew clean bootJar \
        && mv build/libs/*.jar app.jar


# Run stage

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]