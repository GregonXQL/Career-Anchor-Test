# syntax=docker/dockerfile:1

FROM maven:3.9.16-eclipse-temurin-21-alpine AS build
WORKDIR /workspace

COPY pom.xml ./
RUN mvn -B -ntp dependency:go-offline

COPY src ./src
RUN mvn -B -ntp -Dmaven.test.skip=true clean package

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build --chown=app:app /workspace/target/career-anchor-server-*.jar /app/app.jar
RUN mkdir -p /app/logs && chown -R app:app /app

USER app
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseContainerSupport"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
