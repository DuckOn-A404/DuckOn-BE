# syntax=docker/dockerfile:1.6

############################
# 1) Build Stage (JDK 21)
############################
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace

# Gradle wrapper & 메타파일 먼저 복사(의존성 캐시 최적화)
COPY gradlew gradlew
COPY gradle  gradle
COPY build.gradle settings.gradle* gradle.properties* ./
RUN chmod +x gradlew

# (선택) 의존성 미리 받기 — 캐시층 생성
# 실패해도 빌드 계속하도록 || true
RUN ./gradlew --no-daemon dependencies || true

# 소스 복사 후 빌드
COPY src src
RUN ./gradlew --no-daemon clean bootJar -x test

############################
# 2) Run Stage (경량 JRE 21)
############################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# non-root 유저로 실행
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# 빌드 산출물 하나를 app.jar로 복사
COPY --from=build /workspace/build/libs/*.jar /app/app.jar

# 기본 환경 (필요 시 컨테이너 실행 시 -e 로 덮어씀)
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# (actuator 있으면) 헬스체크 쓰고 싶다면 아래 주석 해제하고
# apk add는 root에서만 되므로 위 USER 설정 전에 설치하면 됨.
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep '"status":"UP"' || exit 1

ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar"]
