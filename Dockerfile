# Stage 1: Build (JDK 환경에서 애플리케이션 빌드)
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 라이브러리 캐싱을 위해 빌드 설정 파일만 먼저 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 의존성 미리 다운로드 (소스 변경 시 빌드 속도 최적화)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

# 소스 코드 복사 및 실행 가능한 JAR 생성 (테스트 제외)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Runtime (경량 JRE 환경에서 실행)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 빌드 스테이지에서 생성된 JAR 파일만 복사 (이미지 경량화)
COPY --from=builder /app/build/libs/*.jar app.jar

# Admin Service 포트 지정
EXPOSE 8084

# 컨테이너 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]