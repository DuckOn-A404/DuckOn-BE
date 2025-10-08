# syntax=docker/dockerfile:1

# --- Build stage -------------------------------------------------------------
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /workspace

# 캐시 최적화: 래퍼/설정 먼저 복사
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew

# 소스 복사 후 빌드
COPY src/ src/
RUN ./gradlew --no-daemon clean bootJar -x test

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# 비루트 사용자
RUN addgroup -S app && adduser -S app -G app

# 빌드 산출물 복사
COPY --from=builder /workspace/build/libs/*.jar /app/app.jar

# OCI 라벨(선택)
LABEL org.opencontainers.image.source="https://github.com/DuckOn-A404/DuckOn-BE" \
      org.opencontainers.image.title="DuckOn Backend" \
      org.opencontainers.image.description="Spring Boot backend for DuckOn" \
      org.opencontainers.image.licenses="MIT"

EXPOSE 8080

# 비루트로 실행
USER app

# JAVA_OPTS 지원
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
