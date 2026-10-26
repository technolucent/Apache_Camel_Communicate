# How to run and use this project

## Prerequisites
- Java 21
- Maven
- Docker + Docker Compose (optional for full local stack)
- RabbitMQ
- Jaeger (optional if using Docker Compose)

## 1) Run with Docker Compose
Create a `.env` file first — compose fails fast without it:
```bash
cp .env.example .env
# edit .env and set: RABBITMQ_USER, RABBITMQ_PASS, APP_SECURITY_USERNAME, APP_SECURITY_PASSWORD
docker compose up --build
```

Notes:
- Starts RabbitMQ, Jaeger, and the Spring Boot app.
- App URL: http://localhost:8080
- RabbitMQ admin: http://localhost:15672 (log in with `RABBITMQ_USER`/`RABBITMQ_PASS` from `.env`)
- Jaeger UI: http://localhost:16686
- Health check: http://localhost:8080/actuator/health (public; details require auth)

## 2) Run locally without Docker
Start RabbitMQ and optionally Jaeger first, then run:
```bash
export SPRING_RABBITMQ_USERNAME=<rabbit-user>
export SPRING_RABBITMQ_PASSWORD=<rabbit-pass>
export APP_SECURITY_USERNAME=<app-user>
export APP_SECURITY_PASSWORD=<app-pass>
mvn spring-boot:run
```

## 3) Build jar
```bash
mvn clean package -DskipTests
```

## 4) Run packaged jar
```bash
export APP_SECURITY_USERNAME=<app-user>
export APP_SECURITY_PASSWORD=<app-pass>
java -javaagent:/path/to/opentelemetry-javaagent.jar -jar target/oss-*.jar
```

## 5) Test commands
```bash
mvn test
```

Note: the integration test requires Docker/Testcontainers support.

## 6) API examples
All requests except `/actuator/health` and `/actuator/info` require HTTP Basic Auth
using the `APP_SECURITY_USERNAME`/`APP_SECURITY_PASSWORD` values you set.

Publish a message:
```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" "http://localhost:8080/publish?message=hello"
```

Trigger a route:
```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" "http://localhost:8080/trigger?message=hello"
```

Read one queued message:
```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" http://localhost:8080/read
```

Drain messages:
```bash
curl -u "$APP_SECURITY_USERNAME:$APP_SECURITY_PASSWORD" http://localhost:8080/drain
```

Check app health (public, no auth; component details only shown to authenticated users):
```bash
curl http://localhost:8080/actuator/health
```

## 7) Notes
- There are no default credentials: the app fails fast at startup if `APP_SECURITY_USERNAME` or `APP_SECURITY_PASSWORD` is unset/blank.
- Never commit real secrets — `.env` is git-ignored; keep `.env.example` as the template.
- Do not reuse credentials between the RabbitMQ broker and the app auth.
- For real deployments, use stronger auth, HTTPS, and least-privilege roles.
