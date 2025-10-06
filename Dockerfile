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

# BuildKit 캐시 사용: 의존성 미리 당겨와 캐싱
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon dependencies || true

# 소스 복사 후 빌드(캐시 재활용)
COPY src src
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar -x test

############################
# 2) Run Stage (경량 JRE 21)
############################
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 루트에서 tzdata 설치(로그/네이티브 라이브러리 시간대 정확성)
RUN apk add --no-cache tzdata \
 && ln -snf /usr/share/zoneinfo/Asia/Seoul /etc/localtime \
 && echo Asia/Seoul > /etc/timezone

# 보안: non-root 유저
RUN addgroup -S spring && adduser -S spring -G spring

# 빌드 산출물 복사 후 권한 부여
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
RUN chown spring:spring /app/app.jar
USER spring:spring

# 컨테이너 메모리 연동 옵션(21은 기본 컨테이너 인식, 퍼센트만 지정)
ENV TZ=Asia/Seoul \
    JAVA_OPTS="-XX:MaxRAMPercentage=75.0 -Xms256m -Xmx512m"

EXPOSE 8080

# (선택) Actuator 사용 시 주석 해제
# HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#   CMD wget -qO- http://127.0.0.1:8080/actuator/health | grep '"status":"UP"' || exit 1

# exec로 신호 전달/종료 처리 깔끔
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
