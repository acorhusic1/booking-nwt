# syntax=docker/dockerfile:1.6

# ─── Stage 1: build (only re-runs when pom.xml or src changes) ────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Dependency cache layer — invalidira se samo kad se pom.xml mijenja
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

# ─── Stage 2: runtime (minimalno: JRE + app.jar) ──────────────────────
FROM eclipse-temurin:21-jre-alpine
# wget je potreban za docker-compose healthcheck
RUN apk add --no-cache wget && \
    addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar
USER spring

# JVM optimizacije za kontejner:
#   MaxRAMPercentage=75       — JVM koristi 75% docker mem_limit-a (container-aware)
#   TieredStopAtLevel=1       — JIT samo L1 (brzi startup, manje code-cache memorije)
#   -Xss256k                  — thread stack 256k (default 1MB × 80 threads = 80MB)
#   UseSerialGC               — jednostavni GC za male heap-ove, manje metadata overhead
#   /dev/./urandom            — sprjecava blokirano startup-a na nekim Linux sistemima
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+TieredCompilation -XX:TieredStopAtLevel=1 -Xss256k -XX:+UseSerialGC -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["java", "-jar", "app.jar"]
