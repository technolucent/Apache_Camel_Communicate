# Handoff Notes (for Gemini / next agent)

## Project
Spring Boot 4.0.7 + Apache Camel 4.20.0 (Java 21) app. RabbitMQ publish/read/drain via REST, OpenTelemetry -> Jaeger.
Working dir: /home/technolucent/camelOSS/oss. Branch master, clean at review.

## Where things are
- src/main/java/com/camel/oss/OssApplication.java (entrypoint)
- src/main/java/com/camel/oss/router/RouteA.java (direct:Subscribe)
- src/main/java/com/camel/oss/router/RouteB.java (RabbitMQ routes + @Bean queue/exchange/binding)
- src/main/java/com/camel/oss/Controller/TestController.java (/trigger, /publish, /read)
- src/main/resources/application.properties
- docker-compose.yml, Dockerfile

## Flaws identified (review only, not fixed)
1. Dockerfile downloads opentelemetry-javaagent.jar from "latest" at build time (supply-chain/repro risk; pin version).
2. REST endpoints (/publish, /read, /trigger) have no auth/validation; exposed anonymously.
3. camel.main.run-controller=true likely unnecessary in Spring Boot; may cause odd lifecycle.
4. @Bean queue/exchange/binding defined inside Camel RouteBuilder (mixes config with routing); works but poor separation.
5. Custom UUID "myTraceId" header is not actually used for tracing; OpenTelemetry agent handles trace. Could confuse.
6. Swallowed exceptions in read/drain; no HTTP error surface/retry.
7. No /drain REST endpoint although direct:drain route exists (orphaned route).
8. camel-app in compose does not wait for jaeger (minor; exporter retries).
9. @SpringBootTest context load may depend on broker availability (untested).
10. Actuator/tracing properties may need management.endpoints exposure for verification.

## Fixed (2026-10-25)
- Pinned OTel agent version 2.11.0 in Dockerfile via ARG.
- Added Spring Security basic auth (default admin/change-me, env-overridable).
- Moved RabbitMQ queue/exchange/binding beans to RabbitMqConfig (@Configuration).
- Removed `camel.main.run-controller=true` and startup print.
- Added input validation + 400 responses in TestController.
- Removed fake UUID trace id; rely on OTel tracing.
- Added `/drain` endpoint.
- Made read/drain propagate exceptions instead of swallowing.
- Added actuator endpoints exposure + camel-app waits for Jaeger in compose.

## Remaining
- Real role-based/auth hardening and CSRF/HTTPS for production.
- Test coverage beyond context load.

## Latest (README)
- Created README.md documenting project, run, auth, API endpoints, RabbitMQ topology, tracing, tests, config.
