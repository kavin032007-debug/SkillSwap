# ==========================================
# Multi-Stage Dockerfile for SkillSwap
# Pure Java 17, Zero External Runtime Dependencies
# Suitable for Render, Railway, Fly.io, Cloud Run
# ==========================================

# Stage 1: Build
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /workspace

# Copy source tree and build script
COPY src ./src
COPY build.sh ./

# Grant execution rights and compile JAR
RUN chmod +x build.sh && ./build.sh

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Non-root user and persistent storage directory setup
RUN addgroup --system skillswap && adduser --system --ingroup skillswap skillswap \
    && mkdir -p /app/data \
    && chown -R skillswap:skillswap /app

# Copy compiled standalone executable JAR from builder
COPY --chown=skillswap:skillswap --from=builder /workspace/target/skillswap.jar /app/skillswap.jar

USER skillswap

# Data directory for file persistence (can be mounted as volume)
VOLUME /app/data

# Default port for hosting platforms
ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "skillswap.jar"]
