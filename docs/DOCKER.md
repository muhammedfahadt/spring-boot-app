# 🐳 Dockerizing Your Spring Boot App

> Step-by-step guide to containerizing a Spring Boot application with Docker and Docker Compose.

---

## Quick Reference — Docker Commands

```bash
docker build -t spring-boot-app:1.0.0 .      # build image
docker run -d -p 8080:8080 --name my-app spring-boot-app:1.0.0  # run container
docker ps                                      # view running containers
docker logs -f my-app                          # view logs
docker stop my-app                             # stop container
docker rm my-app                               # remove container
docker images                                  # view all images
```

---

## Step 1 — Create a `Dockerfile`

Create a file named **exactly** `Dockerfile` (capital D, lowercase f) in your project root:

```dockerfile
# ── Stage 1: Build ──────────────────────────────────────
FROM maven:3.9.5-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy pom first (layer caching — avoids re-downloading deps)
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Run ────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**What each stage does:**

| Stage | Base Image | Purpose |
|---|---|---|
| `builder` | `maven:3.9.5-eclipse-temurin-21-alpine` | Compiles Java code, runs Maven |
| `runtime` | `eclipse-temurin:21-jre-alpine` | Runs only the compiled JAR |

> ✅ **Multi-stage build** keeps the final image small — the Maven toolchain is discarded after build.

---

## Step 2 — Create `.dockerignore`

Prevent unnecessary files from being copied into the build context:

```
# Maven
target/
.m2/
*.jar
*.war

# IDE
.idea/
.vscode/
*.iml

# Git
.git/
.gitignore

# Codespaces
.devcontainer/
```

---

## Step 3 — Build the Docker Image

```bash
# Build with version tag
docker build -t spring-boot-app:1.0.0 .

# Or with latest tag
docker build -t spring-boot-app:latest .
```

Expected output:

```
[+] Building 45.3s (11/11) FINISHED
 => [builder 1/5] FROM maven:3.9.5-eclipse-temurin-21-alpine
 => [builder 2/5] WORKDIR /app
 => [builder 3/5] COPY pom.xml .
 => [builder 4/5] COPY src ./src
 => [builder 5/5] RUN mvn clean package -DskipTests
 => [stage-1 6/6] COPY --from=builder /app/target/*.jar app.jar
 => exporting to docker image
 => naming to docker.io/library/spring-boot-app:1.0.0
```

> ⚠️ **Common mistake:** `Dockerfile` must be lowercase `f`. `DockerFile` will cause a build error.

---

## Step 4 — Run the Container

```bash
# Run in detached mode
docker run -d \
  --name spring-boot-container \
  -p 8080:8080 \
  spring-boot-app:1.0.0

# View running containers
docker ps

# View logs (live)
docker logs -f spring-boot-container

# Stop container
docker stop spring-boot-container

# Remove container
docker rm spring-boot-container

# Remove image
docker rmi spring-boot-app:1.0.0
```

---

## Step 5 — Test the Container

```bash
# Health check
curl http://localhost:8080/api/health
# → {"status":"UP","service":"Spring Boot Application"}

# Hello endpoint
curl http://localhost:8080/api/hello/Docker
# → {"message":"Hello, Docker","timestamp":"..."}

# Inspect container details
docker inspect spring-boot-container
```

---

## Step 6 — Docker Compose (App + PostgreSQL)

For running both the Spring Boot app and PostgreSQL together:

**`docker-compose.yml`:**

```yaml
version: '3.8'

services:

  app:
    build: .
    container_name: springboot-app
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/appdb
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    depends_on:
      db:
        condition: service_healthy
    networks:
      - app-network

  db:
    image: postgres:16-alpine
    container_name: postgres-db
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=appdb
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - app-network

volumes:
  postgres-data:

networks:
  app-network:
    driver: bridge
```

**Run with Docker Compose:**

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app

# Stop all services
docker-compose down

# Stop and delete DB volume (fresh start)
docker-compose down -v
```

---

## Step 7 — Update `application.yaml` for Docker

Use `${VAR:default}` syntax so the app works both locally and inside Docker:

```yaml
server:
  port: 8080
  address: 0.0.0.0
  servlet:
    context-path: /api

spring:
  application:
    name: spring-boot-app
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/appdb}
    username: ${SPRING_DATASOURCE_USERNAME:postgres}
    password: ${SPRING_DATASOURCE_PASSWORD:postgres}
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

> ✅ `${VAR:default}` reads the environment variable when running in Docker, falls back to the default when running locally with `mvn spring-boot:run`.

---

## All Useful Docker Commands

```bash
# ── Images ──────────────────────────────────────────────
docker build -t spring-boot-app:1.0.0 .       # build image
docker images                                  # list all images
docker rmi spring-boot-app:1.0.0              # remove image

# ── Containers ──────────────────────────────────────────
docker run -d -p 8080:8080 --name my-app spring-boot-app:1.0.0
docker ps                                      # running containers
docker ps -a                                   # all containers (incl. stopped)
docker logs -f my-app                          # live logs
docker exec -it my-app sh                      # shell inside container
docker stop my-app                             # stop container
docker start my-app                            # start stopped container
docker rm my-app                               # remove container

# ── Docker Hub ──────────────────────────────────────────
docker tag spring-boot-app:1.0.0 username/spring-boot-app:1.0.0
docker push username/spring-boot-app:1.0.0
docker pull username/spring-boot-app:1.0.0
```

---

*Part of [codespaces-springboot-starter](https://github.com/muhammedfahadt/spring-boot-app/blob/main/README.md) • Muhammed Fahad • June 2026*
