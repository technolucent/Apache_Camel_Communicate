# How to run and use this project

## Prerequisites
- Java 21
- Maven
- Docker + Docker Compose (optional for full local stack)
- RabbitMQ
- Jaeger (optional if using Docker Compose)

## 1) Run with Docker Compose
```bash
docker compose up --build
```

Notes:
- Starts RabbitMQ, Jaeger, and the Spring Boot app.
- App URL: http://localhost:8080
- RabbitMQ admin: http://localhost:15672
- Jaeger UI: http://localhost:16686
- Health check: http://localhost:8080/actuator/health

Set credentials before running in a real environment:
```bash
export APP_SECURITY_USERNAME=admin
export APP_SECURITY_PASSWORD=change-me
```

## 2) Run locally without Docker
Start RabbitMQ and optionally Jaeger first, then run:
```bash
export APP_SECURITY_USERNAME=admin
export APP_SECURITY_PASSWORD=change-me
mvn spring-boot:run
```

## 3) Build jar
```bash
mvn clean package -DskipTests
```

## 4) Run packaged jar
```bash
export APP_SECURITY_USERNAME=admin
export APP_SECURITY_PASSWORD=change-me
java -javaagent:/path/to/opentelemetry-javaagent.jar -jar target/oss-*.jar
```

## 5) Test commands
```bash
mvn test
```

Note: the integration test requires Docker/Testcontainers support.

## 6) API examples
All requests require HTTP Basic Auth.

Publish a message:
```bash
curl -u admin:change-me "http://localhost:8080/publish?message=hello"
```

Trigger a route:
```bash
curl -u admin:change-me "http://localhost:8080/trigger?message=hello"
```

Read one queued message:
```bash
curl -u admin:change-me http://localhost:8080/read
```

Drain messages:
```bash
curl -u admin:change-me http://localhost:8080/drain
```

Check app health:
```bash
curl -u admin:change-me http://localhost:8080/actuator/health
```

## 7) Notes
- Default username is admin.
- Default password is change-me unless overridden with APP_SECURITY_PASSWORD.
- Do not leave default credentials in production.
- For real deployments, use stronger auth, HTTPS, and least-privilege roles.
