# Apache Camel Communicate (oss)

Spring Boot 4 + Apache Camel 4.20 app. REST API to publish / read / drain messages on RabbitMQ, with OpenTelemetry traces sent to Jaeger.

## What it does

- Expose REST endpoints to trigger Camel routes.
- Publish messages to a RabbitMQ queue (`hello`).
- Read one message or drain all available messages.
- Trace requests with OpenTelemetry → Jaeger.
- Create RabbitMQ topology (queue, exchange, binding, dead-letter) at startup.

## Stack

- Java 21, Spring Boot 4.0.7, Apache Camel 4.20.0
- RabbitMQ (Spring AMQP)
- OpenTelemetry Java agent 2.11.0 + Micrometer tracing
- Jaeger (OTLP HTTP)
- Maven, Docker

## Run with Docker (recommended)

```bash
docker compose up --build
```

Services:
- RabbitMQ: AMQP 5672, management UI 15672
- Jaeger UI: http://localhost:16686
- App: http://localhost:8080
- Actuator health: http://localhost:8080/actuator/health

Set a password before running (see Auth).

## Run locally

Need RabbitMQ (and Jaeger optional) running. Then:

```bash
mvn spring-boot:run
```

Or build and run:

```bash
mvn clean package -DskipTests
java -javaagent:/path/to/opentelemetry-javaagent.jar -jar target/oss-*.jar
```

## Auth

Spring Security basic auth. Default username `admin` (`APP_SECURITY_USERNAME`). Password from `APP_SECURITY_PASSWORD` (default empty — set it).

Example:

```bash
export APP_SECURITY_PASSWORD=change-me
curl -u admin:change-me http://localhost:8080/...
```

## API

All endpoints require basic auth.

| Method | Path | Query | Description |
|---|---|---|---|
| GET | `/trigger` | `message` | Send message to `direct:Subscribe` (logs, then publishes) |
| GET | `/publish` | `message` | Publish message to queue |
| GET | `/read` | — | Read one message from queue |
| GET | `/drain` | — | Read up to 100 messages; returns JSON list |

`message` max length 10000 chars; blank rejected (400).

Examples:

```bash
curl -u admin:change-me "http://localhost:8080/publish?message=hello"
curl -u admin:change-me "http://localhost:8080/read"
curl -u admin:change-me "http://localhost:8080/drain"
```

## RabbitMQ topology

Created as Spring beans (`RabbitMqConfig`):
- Queue `hello` (durable), DLX → `hello.dlq`
- Queue `hello.dlq` (durable)
- Direct exchange `hello`, DLX `hello.dlx`
- Binding `hello` queue to `hello` exchange, routing key `hello`

## Tracing

- Micrometer tracing bridge + OTLP exporter (`management.otlp.tracing.endpoint`).
- Java agent attached in Docker (`JAVA_TOOL_OPTIONS`).
- Traces viewable in Jaeger UI.

## Tests

```bash
mvn test
```

Includes context-load test and Testcontainers-based route integration test (needs Docker).

## Config (env)

- `APP_SECURITY_USERNAME` / `APP_SECURITY_PASSWORD`
- `SPRING_RABBITMQ_HOST` / `SPRING_RABBITMQ_PORT`
- `management.otlp.tracing.endpoint` (default `http://jaeger:4318/v1/traces`)
- `OTEL_SERVICE_NAME`, `OTEL_EXPORTER_OTLP_ENDPOINT` (in compose)

## Notes

- Sensible production hardening (roles, CSRF, HTTPS) still pending.
- Test coverage beyond context load minimal.