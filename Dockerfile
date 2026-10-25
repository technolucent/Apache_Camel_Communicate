# --- Stage 1: Build the Application ---
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies (cached layer)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the package
COPY src ./src
RUN mvn clean package -DskipTests

# --- Stage 2: Runtime Environment ---
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Create a non-root system user for security
USER root
ARG OTEL_JAVA_AGENT_VERSION=2.11.0
RUN curl -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar -o /app/opentelemetry-javaagent.jar
RUN useradd -ms /bin/bash cameluser && chown -R cameluser:cameluser /app
USER cameluser

# Copy the compiled jar from the build stage
COPY --from=build /app/target/oss-*.jar app.jar

# Expose the port your Tomcat server initialized on
EXPOSE 8080

# Run the Spring Boot application with OpenTelemetry agent
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
