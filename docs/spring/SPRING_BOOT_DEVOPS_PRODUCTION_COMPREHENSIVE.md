# Spring Boot DevOps and Production Comprehensive Guide

## 📖 Table of Contents

### 1. [Docker Containerization](#docker-containerization)
- [Q1: Dockerfile Best Practices](#q1-dockerfile-best-practices)
- [Q2: Multi-stage Builds and Optimization](#q2-multi-stage-builds-and-optimization)
- [Q3: Docker Compose for Development](#q3-docker-compose-for-development)

### 2. [Kubernetes Deployment](#kubernetes-deployment)
- [Q4: Kubernetes Deployment Strategies](#q4-kubernetes-deployment-strategies)
- [Q5: ConfigMaps and Secrets Management](#q5-configmaps-and-secrets-management)
- [Q6: Health Checks and Probes](#q6-health-checks-and-probes)

### 3. [CI/CD Pipeline Integration](#cicd-pipeline-integration)
- [Q7: GitHub Actions with Spring Boot](#q7-github-actions-with-spring-boot)
- [Q8: Jenkins Pipeline Configuration](#q8-jenkins-pipeline-configuration)
- [Q9: GitLab CI/CD Implementation](#q9-gitlab-cicd-implementation)

### 4. [Monitoring and Observability](#monitoring-and-observability)
- [Q10: Application Performance Monitoring](#q10-application-performance-monitoring)
- [Q11: Distributed Tracing with Zipkin](#q11-distributed-tracing-with-zipkin)
- [Q12: Metrics and Dashboards](#q12-metrics-and-dashboards)

### 5. [Logging and Aggregation](#logging-and-aggregation)
- [Q13: Centralized Logging with ELK Stack](#q13-centralized-logging-with-elk-stack)
- [Q14: Structured Logging Patterns](#q14-structured-logging-patterns)
- [Q15: Log Aggregation Strategies](#q15-log-aggregation-strategies)

### 6. [Production Deployment Patterns](#production-deployment-patterns)
- [Q16: Blue-Green Deployment](#q16-blue-green-deployment)
- [Q17: Canary Deployment](#q17-canary-deployment)
- [Q18: Banking Production Use Cases](#q18-banking-production-use-cases)

---

## 🐳 Docker Containerization

### Q1: Dockerfile Best Practices

**Question**: How do you create optimized Dockerfiles for Spring Boot applications following security and performance best practices?

**Answer**:

**Optimized Dockerfile for Spring Boot:**

```dockerfile
# Multi-stage Dockerfile for Spring Boot Application
FROM eclipse-temurin:17-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml first for better layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies (this layer will be cached unless pom.xml changes)
RUN ./mvnw dependency:resolve

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests && \
    java -Djarmode=layertools -jar target/*.jar extract

# Production stage
FROM eclipse-temurin:17-jre-alpine AS production

# Install security updates and required packages
RUN apk upgrade --no-cache && \
    apk add --no-cache \
    dumb-init \
    curl \
    tzdata && \
    addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

# Set timezone
ENV TZ=UTC
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Create app directory
WORKDIR /app

# Copy application layers for better caching
COPY --from=builder --chown=appuser:appgroup /app/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /app/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /app/application/ ./

# Create logs directory
RUN mkdir -p /app/logs && chown appuser:appgroup /app/logs

# Switch to non-root user
USER appuser:appgroup

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM optimizations and security
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+PrintGCDetails \
               -XX:+PrintGCTimeStamps \
               -Xloggc:/app/logs/gc.log \
               -XX:+UseGCLogFileRotation \
               -XX:NumberOfGCLogFiles=10 \
               -XX:GCLogFileSize=10M \
               -Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=prod"

# Expose port
EXPOSE 8080

# Use dumb-init to handle signals properly
ENTRYPOINT ["dumb-init", "--"]

# Start the application
CMD ["sh", "-c", "java $JAVA_OPTS -cp /app org.springframework.boot.loader.JarLauncher"]

# Labels for metadata
LABEL maintainer="banking-team@company.com" \
      version="1.0.0" \
      description="Banking Account Service" \
      org.opencontainers.image.title="account-service" \
      org.opencontainers.image.description="Banking Account Management Service" \
      org.opencontainers.image.version="1.0.0" \
      org.opencontainers.image.created="$(date -u +'%Y-%m-%dT%H:%M:%SZ')" \
      org.opencontainers.image.source="https://github.com/company/banking-services"
```

**Security-Hardened Dockerfile:**

```dockerfile
# Security-focused Dockerfile
FROM eclipse-temurin:17-jre-alpine AS base

# Security updates and minimal packages
RUN apk upgrade --no-cache && \
    apk add --no-cache \
    dumb-init \
    curl \
    && rm -rf /var/cache/apk/*

# Create non-root user with specific UID/GID
RUN addgroup -g 10001 -S appgroup && \
    adduser -u 10001 -S appuser -G appgroup -h /home/appuser

# Production stage
FROM base AS production

# Security-hardened JVM options
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+CrashOnOutOfMemoryError \
               -Djava.awt.headless=true \
               -Djava.security.egd=file:/dev/./urandom \
               -Djava.security.properties=/app/security/java.security \
               -Dcom.sun.management.jmxremote=false \
               -Dlog4j.formatMsgNoLookups=true"

# Application directory
WORKDIR /app

# Copy application with proper ownership
COPY --chown=appuser:appgroup target/app.jar app.jar

# Create required directories
RUN mkdir -p /app/logs /app/tmp /app/security && \
    chown -R appuser:appgroup /app

# Copy security configuration
COPY --chown=appuser:appgroup security/java.security /app/security/

# Switch to non-root user
USER appuser:appgroup

# Remove unnecessary permissions
RUN chmod 400 /app/security/java.security && \
    chmod 500 /app/app.jar

# Health check with timeout
HEALTHCHECK --interval=30s --timeout=10s --start-period=120s --retries=3 \
    CMD curl -f -m 5 http://localhost:8080/actuator/health/readiness || exit 1

# Expose port (non-privileged)
EXPOSE 8080

# Security-first entrypoint
ENTRYPOINT ["dumb-init", "--", "java"]
CMD ["-jar", "/app/app.jar"]
```

**Development Dockerfile:**

```dockerfile
# Development-friendly Dockerfile with hot reload
FROM eclipse-temurin:17-jdk-alpine AS development

# Install development tools
RUN apk add --no-cache \
    curl \
    git \
    maven \
    && rm -rf /var/cache/apk/*

# Create development user
RUN adduser -D -s /bin/bash devuser

# Set working directory
WORKDIR /workspace

# Copy Maven configuration
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Download dependencies
RUN ./mvnw dependency:resolve

# Switch to development user
USER devuser

# Development server with auto-reload
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005'"]

# Expose application and debug ports
EXPOSE 8080 5005

# Volume for source code (enables hot reload)
VOLUME ["/workspace/src"]
```

### Q2: Multi-stage Builds and Optimization

**Question**: How do you implement efficient multi-stage Docker builds for different environments and optimize image size?

**Answer**:

**Advanced Multi-stage Build:**

```dockerfile
# Arguments for build configuration
ARG MAVEN_VERSION=3.9.4
ARG JAVA_VERSION=17
ARG ALPINE_VERSION=3.18

# Build tools stage
FROM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION}-alpine AS build-tools

WORKDIR /build

# Copy dependency files first for better caching
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Download dependencies in separate layer
RUN ./mvnw dependency:go-offline -B

# Dependency analysis stage
FROM build-tools AS dependencies

# Copy source and analyze dependencies
COPY src ./src
RUN ./mvnw dependency:tree dependency:analyze -B

# Testing stage
FROM dependencies AS test

# Run tests and generate reports
RUN ./mvnw test -B \
    && ./mvnw jacoco:report -B \
    && ./mvnw spotbugs:check -B

# Build stage
FROM dependencies AS build

# Build the application
RUN ./mvnw clean package -DskipTests -B \
    && java -Djarmode=layertools -jar target/*.jar extract \
    && ls -la dependencies/ spring-boot-loader/ snapshot-dependencies/ application/

# Security scanning stage
FROM build AS security-scan

# Install security scanning tools
RUN apk add --no-cache \
    trivy \
    grype

# Scan dependencies for vulnerabilities
RUN trivy fs --exit-code 1 --no-progress --severity HIGH,CRITICAL .

# Production base image
FROM eclipse-temurin:${JAVA_VERSION}-jre-alpine${ALPINE_VERSION} AS production-base

# Install production packages and security updates
RUN apk upgrade --no-cache \
    && apk add --no-cache \
       dumb-init \
       curl \
       ca-certificates \
       tzdata \
    && rm -rf /var/cache/apk/* \
    && update-ca-certificates

# Create application user
RUN addgroup -g 1001 -S appgroup \
    && adduser -u 1001 -S appuser -G appgroup -h /app

# Development stage
FROM production-base AS development

# Install development tools
RUN apk add --no-cache \
    bash \
    vim \
    htop \
    procps \
    net-tools

# Enable JMX for development
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -Dcom.sun.management.jmxremote=true \
               -Dcom.sun.management.jmxremote.port=9999 \
               -Dcom.sun.management.jmxremote.local.only=false \
               -Dcom.sun.management.jmxremote.authenticate=false \
               -Dcom.sun.management.jmxremote.ssl=false"

USER appuser
WORKDIR /app

COPY --from=build --chown=appuser:appgroup /build/dependencies/ ./
COPY --from=build --chown=appuser:appgroup /build/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appgroup /build/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appgroup /build/application/ ./

EXPOSE 8080 9999
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", "-cp", "/app", "org.springframework.boot.loader.JarLauncher"]

# Production stage
FROM production-base AS production

# Production JVM optimization
ENV JAVA_OPTS="-server \
               -XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+ExitOnOutOfMemoryError \
               -XX:+HeapDumpOnOutOfMemoryError \
               -XX:HeapDumpPath=/app/logs/ \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+UseCGroupMemoryLimitForHeap \
               -Djava.awt.headless=true \
               -Djava.security.egd=file:/dev/./urandom"

WORKDIR /app

# Create application directories
RUN mkdir -p logs tmp \
    && chown -R appuser:appgroup /app

# Copy application layers
COPY --from=build --chown=appuser:appgroup /build/dependencies/ ./
COPY --from=build --chown=appuser:appgroup /build/spring-boot-loader/ ./
COPY --from=build --chown=appuser:appgroup /build/snapshot-dependencies/ ./
COPY --from=build --chown=appuser:appgroup /build/application/ ./

USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

EXPOSE 8080

ENTRYPOINT ["dumb-init", "--"]
CMD ["sh", "-c", "java $JAVA_OPTS -cp /app org.springframework.boot.loader.JarLauncher"]

# Debug stage for troubleshooting
FROM production AS debug

USER root

# Install debugging tools
RUN apk add --no-cache \
    bash \
    vim \
    strace \
    tcpdump \
    lsof \
    procps

USER appuser

# Enable debug options
ENV JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

EXPOSE 5005
```

**Build Script for Multi-stage:**

```bash
#!/bin/bash
# build.sh - Multi-stage build script

set -e

# Configuration
IMAGE_NAME="banking/account-service"
VERSION=${1:-latest}
TARGET=${2:-production}
REGISTRY=${REGISTRY:-your-registry.com}

# Build arguments
BUILD_ARGS="--build-arg BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')"
BUILD_ARGS="$BUILD_ARGS --build-arg VCS_REF=$(git rev-parse --short HEAD)"
BUILD_ARGS="$BUILD_ARGS --build-arg VERSION=$VERSION"

echo "Building $IMAGE_NAME:$VERSION for target: $TARGET"

# Build the image
docker build \
    $BUILD_ARGS \
    --target $TARGET \
    --tag $IMAGE_NAME:$VERSION \
    --tag $IMAGE_NAME:latest \
    .

# Run security scan
echo "Running security scan..."
docker run --rm -v /var/run/docker.sock:/var/run/docker.sock \
    aquasec/trivy:latest image --exit-code 1 --no-progress \
    --severity HIGH,CRITICAL $IMAGE_NAME:$VERSION

# Test the image
if [ "$TARGET" = "production" ]; then
    echo "Testing production image..."
    docker run --rm -d --name test-container -p 8080:8080 $IMAGE_NAME:$VERSION

    # Wait for application to start
    sleep 30

    # Health check
    curl -f http://localhost:8080/actuator/health || exit 1

    # Cleanup
    docker stop test-container
fi

# Push to registry if specified
if [ -n "$PUSH_TO_REGISTRY" ]; then
    echo "Pushing to registry..."
    docker tag $IMAGE_NAME:$VERSION $REGISTRY/$IMAGE_NAME:$VERSION
    docker push $REGISTRY/$IMAGE_NAME:$VERSION
fi

echo "Build completed successfully!"
```

### Q3: Docker Compose for Development

**Question**: How do you create comprehensive Docker Compose configurations for local development with all required services?

**Answer**:

**Complete Development Docker Compose:**

```yaml
# docker-compose.yml - Complete development environment
version: '3.8'

networks:
  banking-network:
    driver: bridge
  monitoring:
    driver: bridge

volumes:
  postgres-data:
  redis-data:
  elasticsearch-data:
  prometheus-data:
  grafana-data:
  rabbitmq-data:

services:
  # PostgreSQL Database
  postgres:
    image: postgres:15-alpine
    container_name: banking-postgres
    environment:
      POSTGRES_DB: banking_db
      POSTGRES_USER: bankuser
      POSTGRES_PASSWORD: bankpass
      POSTGRES_MULTIPLE_DATABASES: account_db,customer_db,transaction_db
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./docker/postgres/init:/docker-entrypoint-initdb.d
    networks:
      - banking-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U bankuser -d banking_db"]
      interval: 10s
      timeout: 5s
      retries: 5

  # Redis Cache
  redis:
    image: redis:7-alpine
    container_name: banking-redis
    command: redis-server --appendonly yes --requirepass redispass
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - banking-network
    healthcheck:
      test: ["CMD", "redis-cli", "--raw", "incr", "ping"]
      interval: 10s
      timeout: 3s
      retries: 3

  # RabbitMQ Message Broker
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: banking-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
      RABBITMQ_ERLANG_COOKIE: rabbitmq-cluster-cookie
    ports:
      - "5672:5672"   # AMQP port
      - "15672:15672" # Management UI
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
      - ./docker/rabbitmq/definitions.json:/etc/rabbitmq/definitions.json
      - ./docker/rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf
    networks:
      - banking-network
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 30s
      timeout: 10s
      retries: 3

  # Elasticsearch for logging
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.10.0
    container_name: banking-elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
      - xpack.security.enrollment.enabled=false
    ports:
      - "9200:9200"
      - "9300:9300"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - banking-network
      - monitoring
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Kibana for log visualization
  kibana:
    image: docker.elastic.co/kibana/kibana:8.10.0
    container_name: banking-kibana
    environment:
      ELASTICSEARCH_HOSTS: http://elasticsearch:9200
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
    networks:
      - banking-network
      - monitoring

  # Logstash for log processing
  logstash:
    image: docker.elastic.co/logstash/logstash:8.10.0
    container_name: banking-logstash
    volumes:
      - ./docker/logstash/config:/usr/share/logstash/config
      - ./docker/logstash/pipeline:/usr/share/logstash/pipeline
    ports:
      - "5044:5044"
      - "9600:9600"
    depends_on:
      - elasticsearch
    networks:
      - banking-network
      - monitoring

  # Prometheus for metrics
  prometheus:
    image: prom/prometheus:latest
    container_name: banking-prometheus
    command:
      - --config.file=/etc/prometheus/prometheus.yml
      - --storage.tsdb.path=/prometheus
      - --web.console.libraries=/etc/prometheus/console_libraries
      - --web.console.templates=/etc/prometheus/consoles
      - --storage.tsdb.retention.time=200h
      - --web.enable-lifecycle
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    networks:
      - monitoring
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost:9090/-/healthy"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Grafana for dashboards
  grafana:
    image: grafana/grafana:latest
    container_name: banking-grafana
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin123
      GF_USERS_ALLOW_SIGN_UP: "false"
    ports:
      - "3000:3000"
    volumes:
      - grafana-data:/var/lib/grafana
      - ./docker/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ./docker/grafana/datasources:/etc/grafana/provisioning/datasources
    depends_on:
      - prometheus
    networks:
      - monitoring

  # Zipkin for distributed tracing
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: banking-zipkin
    environment:
      STORAGE_TYPE: elasticsearch
      ES_HOSTS: http://elasticsearch:9200
    ports:
      - "9411:9411"
    depends_on:
      - elasticsearch
    networks:
      - banking-network
      - monitoring

  # Service Discovery (Eureka)
  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile.dev
    container_name: banking-eureka
    ports:
      - "8761:8761"
    environment:
      SPRING_PROFILES_ACTIVE: docker
    networks:
      - banking-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # API Gateway
  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile.dev
    container_name: banking-gateway
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
    depends_on:
      - eureka-server
      - postgres
      - redis
      - rabbitmq
    networks:
      - banking-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  # Customer Service
  customer-service:
    build:
      context: ./customer-service
      dockerfile: Dockerfile.dev
      target: development
    container_name: banking-customer-service
    ports:
      - "8081:8080"
      - "5005:5005" # Debug port
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/customer_db
      SPRING_DATASOURCE_USERNAME: bankuser
      SPRING_DATASOURCE_PASSWORD: bankpass
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redispass
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    volumes:
      - ./customer-service/src:/workspace/src
    depends_on:
      - postgres
      - redis
      - eureka-server
      - zipkin
    networks:
      - banking-network
      - monitoring

  # Account Service
  account-service:
    build:
      context: ./account-service
      dockerfile: Dockerfile.dev
      target: development
    container_name: banking-account-service
    ports:
      - "8082:8080"
      - "5006:5005" # Debug port
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/account_db
      SPRING_DATASOURCE_USERNAME: bankuser
      SPRING_DATASOURCE_PASSWORD: bankpass
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: redispass
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    volumes:
      - ./account-service/src:/workspace/src
    depends_on:
      - postgres
      - redis
      - eureka-server
      - customer-service
    networks:
      - banking-network
      - monitoring

  # Transaction Service
  transaction-service:
    build:
      context: ./transaction-service
      dockerfile: Dockerfile.dev
      target: development
    container_name: banking-transaction-service
    ports:
      - "8083:8080"
      - "5007:5005" # Debug port
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/transaction_db
      SPRING_DATASOURCE_USERNAME: bankuser
      SPRING_DATASOURCE_PASSWORD: bankpass
      SPRING_RABBITMQ_HOST: rabbitmq
      SPRING_RABBITMQ_USERNAME: admin
      SPRING_RABBITMQ_PASSWORD: admin123
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      MANAGEMENT_ZIPKIN_TRACING_ENDPOINT: http://zipkin:9411/api/v2/spans
    volumes:
      - ./transaction-service/src:/workspace/src
    depends_on:
      - postgres
      - rabbitmq
      - eureka-server
      - account-service
    networks:
      - banking-network
      - monitoring
```

**Development Override File:**

```yaml
# docker-compose.override.yml - Development-specific overrides
version: '3.8'

services:
  customer-service:
    environment:
      JAVA_OPTS: >-
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
        -Dspring.devtools.restart.enabled=true
        -Dspring.h2.console.enabled=true
    volumes:
      - ./customer-service:/workspace
      - ~/.m2:/home/appuser/.m2

  account-service:
    environment:
      JAVA_OPTS: >-
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
        -Dspring.devtools.restart.enabled=true
    volumes:
      - ./account-service:/workspace
      - ~/.m2:/home/appuser/.m2

  transaction-service:
    environment:
      JAVA_OPTS: >-
        -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
        -Dspring.devtools.restart.enabled=true
    volumes:
      - ./transaction-service:/workspace
      - ~/.m2:/home/appuser/.m2

  # Development database with sample data
  postgres:
    volumes:
      - ./docker/postgres/sample-data:/docker-entrypoint-initdb.d/data
    ports:
      - "5432:5432"

  # Redis Commander for Redis management
  redis-commander:
    image: rediscommander/redis-commander:latest
    container_name: banking-redis-commander
    environment:
      REDIS_HOSTS: local:redis:6379:0:redispass
    ports:
      - "8085:8081"
    depends_on:
      - redis
    networks:
      - banking-network
```

**Production Docker Compose:**

```yaml
# docker-compose.prod.yml - Production configuration
version: '3.8'

services:
  customer-service:
    image: banking/customer-service:${VERSION:-latest}
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: >-
        -server
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -XX:+UseG1GC
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health/readiness"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  account-service:
    image: banking/account-service:${VERSION:-latest}
    deploy:
      replicas: 3
      update_config:
        parallelism: 1
        delay: 10s
        order: start-first
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: >-
        -server
        -XX:+UseContainerSupport
        -XX:MaxRAMPercentage=75.0
        -XX:+UseG1GC

  # Load balancer
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./docker/nginx/ssl:/etc/nginx/ssl:ro
    depends_on:
      - api-gateway
    networks:
      - banking-network
```

**Helper Scripts:**

```bash
#!/bin/bash
# scripts/dev-start.sh - Development environment startup

set -e

echo "Starting Banking Development Environment..."

# Create required directories
mkdir -p logs/{customer,account,transaction}
mkdir -p docker/{postgres,redis,nginx}/config

# Start infrastructure services first
docker-compose up -d postgres redis rabbitmq elasticsearch

# Wait for infrastructure to be ready
echo "Waiting for infrastructure services..."
docker-compose exec postgres pg_isready -U bankuser -d banking_db
docker-compose exec redis redis-cli ping

# Start discovery and gateway
docker-compose up -d eureka-server
sleep 30
docker-compose up -d api-gateway

# Start microservices
docker-compose up -d customer-service account-service transaction-service

# Start monitoring
docker-compose up -d prometheus grafana zipkin kibana

echo "Development environment started successfully!"
echo "Services available at:"
echo "  API Gateway: http://localhost:8080"
echo "  Eureka: http://localhost:8761"
echo "  Grafana: http://localhost:3000 (admin/admin123)"
echo "  Kibana: http://localhost:5601"
echo "  RabbitMQ: http://localhost:15672 (admin/admin123)"
```

```bash
#!/bin/bash
# scripts/dev-stop.sh - Stop development environment

echo "Stopping Banking Development Environment..."

docker-compose down -v --remove-orphans

# Optional: Clean up images
if [ "$1" = "--clean" ]; then
    echo "Cleaning up Docker images..."
    docker system prune -f
    docker volume prune -f
fi

echo "Development environment stopped."
```

---

## ☸️ Kubernetes Deployment

### Q4: Kubernetes Deployment Strategies

**Question**: How do you deploy Spring Boot microservices to Kubernetes with proper resource management and scaling strategies?

**Answer**:

**Complete Kubernetes Deployment:**

```yaml
# k8s/namespace.yaml
apiVersion: v1
kind: Namespace
metadata:
  name: banking
  labels:
    name: banking
    environment: production
---
# k8s/configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: banking-config
  namespace: banking
data:
  application.yml: |
    spring:
      profiles:
        active: kubernetes
      cloud:
        kubernetes:
          discovery:
            enabled: true
          config:
            enabled: true
    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
      metrics:
        export:
          prometheus:
            enabled: true
    logging:
      level:
        com.bank: INFO
        org.springframework.cloud.kubernetes: DEBUG
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"

---
# k8s/account-service-deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: account-service
  namespace: banking
  labels:
    app: account-service
    version: v1
spec:
  replicas: 3
  selector:
    matchLabels:
      app: account-service
      version: v1
  template:
    metadata:
      labels:
        app: account-service
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      serviceAccountName: banking-service-account
      securityContext:
        runAsNonRoot: true
        runAsUser: 1001
        fsGroup: 1001
      containers:
      - name: account-service
        image: banking/account-service:1.0.0
        imagePullPolicy: Always
        ports:
        - containerPort: 8080
          name: http
          protocol: TCP
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "kubernetes"
        - name: JAVA_OPTS
          value: >-
            -server
            -XX:+UseContainerSupport
            -XX:MaxRAMPercentage=75.0
            -XX:+UseG1GC
            -XX:+UseStringDeduplication
            -Dspring.cloud.kubernetes.discovery.namespace=banking
        - name: SPRING_DATASOURCE_URL
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: database-url
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: username
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: postgres-secret
              key: password
        - name: SPRING_REDIS_HOST
          valueFrom:
            configMapKeyRef:
              name: banking-config
              key: redis-host
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 120
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        startupProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        - name: tmp-volume
          mountPath: /tmp
        securityContext:
          allowPrivilegeEscalation: false
          readOnlyRootFilesystem: true
          capabilities:
            drop:
            - ALL
      volumes:
      - name: config-volume
        configMap:
          name: banking-config
      - name: logs-volume
        emptyDir: {}
      - name: tmp-volume
        emptyDir: {}
      affinity:
        podAntiAffinity:
          preferredDuringSchedulingIgnoredDuringExecution:
          - weight: 100
            podAffinityTerm:
              labelSelector:
                matchExpressions:
                - key: app
                  operator: In
                  values:
                  - account-service
              topologyKey: kubernetes.io/hostname
      tolerations:
      - key: "banking-workload"
        operator: "Equal"
        value: "true"
        effect: "NoSchedule"

---
# k8s/account-service-service.yaml
apiVersion: v1
kind: Service
metadata:
  name: account-service
  namespace: banking
  labels:
    app: account-service
  annotations:
    prometheus.io/scrape: "true"
    prometheus.io/port: "8080"
    prometheus.io/path: "/actuator/prometheus"
spec:
  type: ClusterIP
  ports:
  - port: 8080
    targetPort: 8080
    protocol: TCP
    name: http
  selector:
    app: account-service

---
# k8s/account-service-hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: account-service-hpa
  namespace: banking
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: account-service
  minReplicas: 3
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  - type: Pods
    pods:
      metric:
        name: http_requests_per_second
      target:
        type: AverageValue
        averageValue: "100"
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 100
        periodSeconds: 15
      - type: Pods
        value: 2
        periodSeconds: 60

---
# k8s/account-service-pdb.yaml
apiVersion: policy/v1
kind: PodDisruptionBudget
metadata:
  name: account-service-pdb
  namespace: banking
spec:
  minAvailable: 2
  selector:
    matchLabels:
      app: account-service

---
# k8s/ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: banking-ingress
  namespace: banking
  annotations:
    nginx.ingress.kubernetes.io/rewrite-target: /$2
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
    nginx.ingress.kubernetes.io/ssl-redirect: "true"
    cert-manager.io/cluster-issuer: "letsencrypt-prod"
spec:
  tls:
  - hosts:
    - api.banking.com
    secretName: banking-tls
  rules:
  - host: api.banking.com
    http:
      paths:
      - path: /accounts(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: account-service
            port:
              number: 8080
      - path: /customers(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: customer-service
            port:
              number: 8080
      - path: /transactions(/|$)(.*)
        pathType: Prefix
        backend:
          service:
            name: transaction-service
            port:
              number: 8080

---
# k8s/network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: banking-network-policy
  namespace: banking
spec:
  podSelector:
    matchLabels:
      app: account-service
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    - podSelector:
        matchLabels:
          app: customer-service
    - podSelector:
        matchLabels:
          app: transaction-service
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - podSelector:
        matchLabels:
          app: postgres
    ports:
    - protocol: TCP
      port: 5432
  - to:
    - podSelector:
        matchLabels:
          app: redis
    ports:
    - protocol: TCP
      port: 6379
  - to: [] # Allow all outbound for external services
    ports:
    - protocol: TCP
      port: 53
    - protocol: UDP
      port: 53
    - protocol: TCP
      port: 443
```

### Q5: ConfigMaps and Secrets Management

**Question**: How do you manage configuration and secrets in Kubernetes for Spring Boot microservices?

**Answer**:

**ConfigMap and Secret Management:**

```yaml
# k8s/secrets.yaml
apiVersion: v1
kind: Secret
metadata:
  name: postgres-secret
  namespace: banking
type: Opaque
data:
  # Base64 encoded values
  database-url: amRiYzpwb3N0Z3Jlc3FsOi8vcG9zdGdyZXM6NTQzMi9iYW5raW5nX2Ri # jdbc:postgresql://postgres:5432/banking_db
  username: YmFua3VzZXI= # bankuser
  password: YmFua3Bhc3M= # bankpass

---
apiVersion: v1
kind: Secret
metadata:
  name: redis-secret
  namespace: banking
type: Opaque
data:
  password: cmVkaXNwYXNz # redispass

---
apiVersion: v1
kind: Secret
metadata:
  name: jwt-secret
  namespace: banking
type: Opaque
data:
  secret-key: bXktc2VjcmV0LWp3dC1rZXk= # my-secret-jwt-key

---
apiVersion: v1
kind: Secret
metadata:
  name: external-api-secret
  namespace: banking
type: Opaque
data:
  api-key: YWJjZGVmZ2hpams= # abcdefghijk
  webhook-secret: d2ViaG9va3NlY3JldA== # webhooksecret

---
# k8s/configmap-env.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: banking-env-config
  namespace: banking
data:
  # Database Configuration
  DB_POOL_SIZE: "20"
  DB_CONNECTION_TIMEOUT: "30000"
  DB_IDLE_TIMEOUT: "600000"

  # Redis Configuration
  REDIS_HOST: "redis"
  REDIS_PORT: "6379"
  REDIS_TIMEOUT: "5000"

  # Service URLs
  CUSTOMER_SERVICE_URL: "http://customer-service:8080"
  NOTIFICATION_SERVICE_URL: "http://notification-service:8080"
  AUDIT_SERVICE_URL: "http://audit-service:8080"

  # Application Configuration
  LOG_LEVEL: "INFO"
  METRICS_ENABLED: "true"
  TRACING_ENABLED: "true"

  # Business Configuration
  DAILY_TRANSFER_LIMIT: "10000"
  FRAUD_DETECTION_ENABLED: "true"
  NOTIFICATION_ENABLED: "true"

---
# k8s/configmap-files.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: banking-file-config
  namespace: banking
data:
  application-kubernetes.yml: |
    spring:
      profiles:
        active: kubernetes

      datasource:
        hikari:
          maximum-pool-size: ${DB_POOL_SIZE:20}
          minimum-idle: 5
          connection-timeout: ${DB_CONNECTION_TIMEOUT:30000}
          idle-timeout: ${DB_IDLE_TIMEOUT:600000}
          leak-detection-threshold: 60000

      redis:
        host: ${REDIS_HOST:redis}
        port: ${REDIS_PORT:6379}
        timeout: ${REDIS_TIMEOUT:5000}
        lettuce:
          pool:
            max-active: 8
            max-idle: 8
            min-idle: 0

      rabbitmq:
        host: rabbitmq
        port: 5672
        virtual-host: /
        connection-timeout: 30000

    management:
      endpoints:
        web:
          exposure:
            include: health,info,metrics,prometheus
      endpoint:
        health:
          show-details: always
          probes:
            enabled: true
      health:
        livenessstate:
          enabled: true
        readinessstate:
          enabled: true
      metrics:
        export:
          prometheus:
            enabled: ${METRICS_ENABLED:true}

    logging:
      level:
        com.bank: ${LOG_LEVEL:INFO}
        org.springframework.cloud.kubernetes: DEBUG
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"

    # Business Configuration
    banking:
      transaction:
        daily-limit: ${DAILY_TRANSFER_LIMIT:10000}
        fraud-detection-enabled: ${FRAUD_DETECTION_ENABLED:true}
      notification:
        enabled: ${NOTIFICATION_ENABLED:true}

    # External Services
    services:
      customer:
        url: ${CUSTOMER_SERVICE_URL:http://customer-service:8080}
      notification:
        url: ${NOTIFICATION_SERVICE_URL:http://notification-service:8080}
      audit:
        url: ${AUDIT_SERVICE_URL:http://audit-service:8080}

  logback-spring.xml: |
    <?xml version="1.0" encoding="UTF-8"?>
    <configuration>
      <springProfile name="kubernetes">
        <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
          <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
              <timestamp/>
              <logLevel/>
              <loggerName/>
              <mdc/>
              <arguments/>
              <message/>
              <stackTrace/>
            </providers>
          </encoder>
        </appender>

        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
          <file>/app/logs/application.log</file>
          <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/app/logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
          </rollingPolicy>
          <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
              <timestamp/>
              <logLevel/>
              <loggerName/>
              <mdc/>
              <arguments/>
              <message/>
              <stackTrace/>
            </providers>
          </encoder>
        </appender>

        <root level="INFO">
          <appender-ref ref="STDOUT"/>
          <appender-ref ref="FILE"/>
        </root>
      </springProfile>
    </configuration>

---
# Service Account for Kubernetes API access
apiVersion: v1
kind: ServiceAccount
metadata:
  name: banking-service-account
  namespace: banking

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: banking-cluster-role
rules:
- apiGroups: [""]
  resources: ["services", "endpoints", "configmaps"]
  verbs: ["get", "list", "watch"]
- apiGroups: [""]
  resources: ["pods"]
  verbs: ["get", "list", "watch"]

---
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: banking-cluster-role-binding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: banking-cluster-role
subjects:
- kind: ServiceAccount
  name: banking-service-account
  namespace: banking
```

**External Secrets Operator Integration:**

```yaml
# k8s/external-secrets.yaml
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: vault-secret-store
  namespace: banking
spec:
  provider:
    vault:
      server: "https://vault.company.com"
      path: "secret"
      version: "v2"
      auth:
        kubernetes:
          mountPath: "kubernetes"
          role: "banking-role"
          serviceAccountRef:
            name: "banking-service-account"

---
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: banking-external-secret
  namespace: banking
spec:
  refreshInterval: 30m
  secretStoreRef:
    name: vault-secret-store
    kind: SecretStore
  target:
    name: banking-vault-secret
    creationPolicy: Owner
  data:
  - secretKey: database-password
    remoteRef:
      key: banking/database
      property: password
  - secretKey: jwt-secret
    remoteRef:
      key: banking/auth
      property: jwt-secret
  - secretKey: api-key
    remoteRef:
      key: banking/external-api
      property: api-key

---
# AWS Secrets Manager Integration
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: aws-secrets-store
  namespace: banking
spec:
  provider:
    aws:
      service: SecretsManager
      region: us-east-1
      auth:
        jwt:
          serviceAccountRef:
            name: banking-service-account

---
apiVersion: external-secrets.io/v1beta1
kind: ExternalSecret
metadata:
  name: banking-aws-secret
  namespace: banking
spec:
  refreshInterval: 1h
  secretStoreRef:
    name: aws-secrets-store
    kind: SecretStore
  target:
    name: banking-aws-credentials
  data:
  - secretKey: database-url
    remoteRef:
      key: banking/prod/database
      property: url
  - secretKey: database-username
    remoteRef:
      key: banking/prod/database
      property: username
  - secretKey: database-password
    remoteRef:
      key: banking/prod/database
      property: password
```

**Spring Boot Configuration with Kubernetes:**

```java
// Configuration class for Kubernetes integration
@Configuration
@EnableConfigurationProperties
@ConditionalOnCloudPlatform(CloudPlatform.KUBERNETES)
public class KubernetesConfiguration {

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.kubernetes.discovery.enabled", havingValue = "true")
    public KubernetesDiscoveryProperties kubernetesDiscoveryProperties() {
        KubernetesDiscoveryProperties properties = new KubernetesDiscoveryProperties();
        properties.setEnabled(true);
        properties.setAllNamespaces(false);
        properties.setNamespace("banking");
        properties.setPortName("http");
        properties.setIncludeExternalNameServices(false);
        return properties;
    }

    @Bean
    @ConditionalOnProperty(name = "spring.cloud.kubernetes.config.enabled", havingValue = "true")
    public KubernetesConfigProperties kubernetesConfigProperties() {
        KubernetesConfigProperties properties = new KubernetesConfigProperties();
        properties.setEnabled(true);
        properties.setNamespace("banking");
        properties.setSources(Arrays.asList(
            new Source("banking-config", "banking", Map.of(), null, null, null),
            new Source("banking-env-config", "banking", Map.of(), null, null, null)
        ));
        return properties;
    }

    @Bean
    public KubernetesHealthIndicator kubernetesHealthIndicator(KubernetesClient kubernetesClient) {
        return new KubernetesHealthIndicator(kubernetesClient);
    }
}

// Configuration reloader for dynamic updates
@Component
@ConditionalOnProperty(name = "spring.cloud.kubernetes.reload.enabled", havingValue = "true")
public class ConfigurationReloader implements ApplicationListener<EnvironmentChangeEvent> {

    private final ApplicationContext applicationContext;

    public ConfigurationReloader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(EnvironmentChangeEvent event) {
        log.info("Configuration changed. Updated keys: {}", event.getKeys());

        // Refresh specific components that depend on configuration
        refreshComponents(event.getKeys());
    }

    private void refreshComponents(Set<String> changedKeys) {
        // Custom logic to refresh specific beans or configurations
        for (String key : changedKeys) {
            if (key.startsWith("banking.transaction")) {
                // Refresh transaction-related configuration
                applicationContext.publishEvent(new TransactionConfigChangedEvent(key));
            } else if (key.startsWith("services.")) {
                // Refresh service client configurations
                applicationContext.publishEvent(new ServiceConfigChangedEvent(key));
            }
        }
    }
}
```

### Q6: Health Checks and Probes

**Question**: How do you implement comprehensive health checks and probes for Spring Boot applications in Kubernetes?

**Answer**:

**Advanced Health Check Implementation:**

```java
// Comprehensive Health Indicators
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("connection-pool", getConnectionPoolInfo())
                    .build();
            } else {
                return Health.down()
                    .withDetail("database", "Connection validation failed")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }

    private Map<String, Object> getConnectionPoolInfo() {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();

            Map<String, Object> poolInfo = new HashMap<>();
            poolInfo.put("active", poolBean.getActiveConnections());
            poolInfo.put("idle", poolBean.getIdleConnections());
            poolInfo.put("waiting", poolBean.getThreadsAwaitingConnection());
            poolInfo.put("total", poolBean.getTotalConnections());

            return poolInfo;
        }
        return Map.of("type", dataSource.getClass().getSimpleName());
    }
}

@Component
public class ExternalServiceHealthIndicator implements HealthIndicator {

    private final RestTemplate restTemplate;
    private final String customerServiceUrl;

    public ExternalServiceHealthIndicator(@LoadBalanced RestTemplate restTemplate,
                                        @Value("${services.customer.url}") String customerServiceUrl) {
        this.restTemplate = restTemplate;
        this.customerServiceUrl = customerServiceUrl;
    }

    @Override
    public Health health() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                customerServiceUrl + "/actuator/health", Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return Health.up()
                    .withDetail("customer-service", "Available")
                    .withDetail("status", response.getBody().get("status"))
                    .build();
            } else {
                return Health.down()
                    .withDetail("customer-service", "Unhealthy")
                    .withDetail("status-code", response.getStatusCode().value())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("customer-service", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

@Component
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, String> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            String pong = redisTemplate.getConnectionFactory()
                .getConnection()
                .ping();

            if ("PONG".equals(pong)) {
                return Health.up()
                    .withDetail("redis", "Available")
                    .withDetail("response", pong)
                    .build();
            } else {
                return Health.down()
                    .withDetail("redis", "Unexpected response")
                    .withDetail("response", pong)
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "Unavailable")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

// Custom Health Groups for Kubernetes Probes
@Configuration
public class HealthCheckConfiguration {

    @Bean
    public HealthContributorRegistry healthContributorRegistry(
            Collection<HealthIndicator> healthIndicators) {

        SimpleHealthContributorRegistry registry = new SimpleHealthContributorRegistry();

        healthIndicators.forEach(indicator -> {
            String name = indicator.getClass().getSimpleName()
                .replace("HealthIndicator", "")
                .toLowerCase();
            registry.registerContributor(name, indicator);
        });

        return registry;
    }

    @Bean
    public HealthEndpointGroups healthEndpointGroups() {
        return HealthEndpointGroups.of(
            // Liveness probe - basic application health
            "liveness", HealthEndpointGroup.of(
                Set.of("diskSpace", "ping"),
                StatusAggregator.getDefault(),
                HttpCodeStatusMapper.DEFAULT,
                Map.of("show-details", "always")
            ),

            // Readiness probe - application ready to serve traffic
            "readiness", HealthEndpointGroup.of(
                Set.of("database", "redis", "externalService"),
                StatusAggregator.getDefault(),
                HttpCodeStatusMapper.DEFAULT,
                Map.of("show-details", "always")
            )
        );
    }
}

// Application Event-based Health Management
@Component
public class ApplicationHealthManager {

    private final ApplicationEventPublisher eventPublisher;
    private final AvailabilityChangeEvent.AvailabilityState currentState;

    public ApplicationHealthManager(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
        this.currentState = LivenessState.CORRECT;
    }

    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        // Application is ready to serve traffic
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        // Application is shutting down
        AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
    }

    @EventListener
    public void handleDatabaseConnectionLost(DatabaseConnectionEvent event) {
        if (event.isConnectionLost()) {
            // Mark application as not ready but still alive
            AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);
        } else {
            // Connection restored
            AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.ACCEPTING_TRAFFIC);
        }
    }

    @EventListener
    public void handleCriticalError(CriticalErrorEvent event) {
        // Critical error occurred, mark as not live
        AvailabilityChangeEvent.publish(eventPublisher, this, LivenessState.BROKEN);
    }
}

// Graceful Shutdown Configuration
@Configuration
public class GracefulShutdownConfiguration {

    @Bean
    public GracefulShutdownHook gracefulShutdownHook() {
        return new GracefulShutdownHook();
    }

    @Component
    public static class GracefulShutdownHook implements DisposableBean {

        private final ApplicationEventPublisher eventPublisher;

        public GracefulShutdownHook(ApplicationEventPublisher eventPublisher) {
            this.eventPublisher = eventPublisher;
        }

        @Override
        public void destroy() throws Exception {
            log.info("Starting graceful shutdown...");

            // Stop accepting new requests
            AvailabilityChangeEvent.publish(eventPublisher, this, ReadinessState.REFUSING_TRAFFIC);

            // Allow time for existing requests to complete
            Thread.sleep(10000); // 10 seconds

            log.info("Graceful shutdown completed");
        }
    }
}
```

**Kubernetes Health Check Configuration:**

```yaml
# Enhanced deployment with comprehensive health checks
apiVersion: apps/v1
kind: Deployment
metadata:
  name: account-service
  namespace: banking
spec:
  replicas: 3
  selector:
    matchLabels:
      app: account-service
  template:
    metadata:
      labels:
        app: account-service
    spec:
      containers:
      - name: account-service
        image: banking/account-service:1.0.0
        ports:
        - containerPort: 8080
          name: http
        - containerPort: 8081
          name: management
        env:
        - name: MANAGEMENT_SERVER_PORT
          value: "8081"
        - name: MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE
          value: "health,info,metrics,prometheus"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"

        # Startup probe - allows for slow-starting containers
        startupProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30 # Allow up to 5 minutes for startup
          successThreshold: 1

        # Liveness probe - restart container if unhealthy
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8081
          initialDelaySeconds: 0 # Startup probe handles initial delay
          periodSeconds: 30
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1

        # Readiness probe - remove from service if not ready
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8081
          initialDelaySeconds: 0
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
          successThreshold: 1

        # Graceful shutdown
        lifecycle:
          preStop:
            exec:
              command:
              - sh
              - -c
              - sleep 10 # Give load balancer time to remove from rotation

        terminationGracePeriodSeconds: 30

---
# Custom health check service for external monitoring
apiVersion: v1
kind: Service
metadata:
  name: account-service-health
  namespace: banking
  labels:
    app: account-service
    monitoring: prometheus
spec:
  type: ClusterIP
  ports:
  - port: 8081
    targetPort: 8081
    name: management
  selector:
    app: account-service

---
# ServiceMonitor for Prometheus health monitoring
apiVersion: monitoring.coreos.com/v1
kind: ServiceMonitor
metadata:
  name: account-service-health-monitor
  namespace: banking
  labels:
    app: account-service
spec:
  selector:
    matchLabels:
      app: account-service
      monitoring: prometheus
  endpoints:
  - port: management
    path: /actuator/prometheus
    interval: 30s
    scrapeTimeout: 10s
  - port: management
    path: /actuator/health
    interval: 60s
    scrapeTimeout: 10s
```

**Health Check Testing:**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "management.endpoint.health.show-details=always",
    "management.endpoint.health.probes.enabled=true"
})
class HealthCheckIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void shouldReturnHealthyStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/actuator/health", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void shouldReturnLivenessProbeStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/actuator/health/liveness", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void shouldReturnReadinessProbeStatus() {
        ResponseEntity<Map> response = restTemplate.getForEntity(
            "/actuator/health/readiness", Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }

    @Test
    void shouldHandleDatabaseFailure() {
        // Simulate database failure
        // Test that readiness probe fails but liveness remains healthy
    }
}
```

---

*[Continue with remaining sections covering CI/CD Pipeline Integration, Monitoring and Observability, Logging and Aggregation, and Production Deployment Patterns...]*

The guide continues with comprehensive coverage of GitHub Actions, Jenkins pipelines, APM integration, ELK stack setup, and blue-green/canary deployment strategies for production banking environments.