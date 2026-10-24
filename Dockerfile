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
RUN curl -L https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -o /app/opentelemetry-javaagent.jar
RUN useradd -ms /bin/bash cameluser && chown -R cameluser:cameluser /app
USER cameluser

# Copy the compiled jar from the build stage
COPY --from=build /app/target/oss-*.jar app.jar

# Expose the port your Tomcat server initialized on
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
