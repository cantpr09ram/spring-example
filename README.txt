Spring Boot Starter

Minimal Spring Boot 3.5 project initialization with Maven, PostgreSQL, Flyway, OpenAPI dependency, Docker Compose (dev/prod profiles), and Testcontainers support. just prove i know how to use spring

Prerequisites
- Java 21
- Docker (for local Postgres and tests)

Development
1. Copy env defaults:
    cp .env.example .env

2. Run app with dev profile (Spring Boot will start Postgres from `compose.yaml`):
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

Production-like run (Docker Compose profile)
docker compose --profile prod up --build

OpenAPI
Swagger UI: http://localhost:8080/swagger-ui/index.html
OpenAPI JSON: http://localhost:8080/v3/api-docs

Tests

./mvnw -q test
