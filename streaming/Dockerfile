# 1. Gradle 빌드 환경 설정
FROM gradle:7.6.0-jdk17 AS build
WORKDIR /app

# 2. 전체 프로젝트 복사 및 WebSocket 서버 빌드
COPY .. .
RUN gradle :streaming:bootJar --no-daemon

# 3. 경량 JDK 이미지로 전환
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# 4. 빌드된 JAR 파일 복사
COPY --from=build /app/streaming/build/libs/streaming-*.jar streaming.jar

# 5. WebSocket 서버 실행
ENTRYPOINT ["java", "-jar", "streaming.jar"]