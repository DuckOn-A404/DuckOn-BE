# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

COPY src/ src/
RUN ./gradlew --no-daemon clean bootJar -x test

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# S3 등 HTTPS 호출용 CA 인증서 설치
RUN apk add --no-cache ca-certificates && \
    update-ca-certificates

# 비루트 사용자
RUN addgroup -S app && adduser -S app -G app

# 빌드 산출물 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

LABEL org.opencontainers.image.source="https://github.com/DuckOn-A404/DuckOn-BE" \
      org.opencontainers.image.title="DuckOn Backend" \
      org.opencontainers.image.description="Spring Boot backend for DuckOn" \
      org.opencontainers.image.licenses="MIT"

EXPOSE 8080

USER app

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
