# 🚀 Deploying Spring Boot App to Render

> Step-by-step guide to deploying a Dockerized Spring Boot app on Render using Docker Hub.

---

## Prerequisites

- ✅ Docker installed and running
- ✅ Docker Hub account ([hub.docker.com](https://hub.docker.com))
- ✅ Render account ([render.com](https://render.com))
- ✅ Spring Boot app built and working locally

---

## Overview

```
Local App → Docker Image → Docker Hub → Render (Live URL)
```

---

## Step 1 — Build the Docker Image

Make sure you're in the directory containing your `Dockerfile`:

```bash
# Confirm Dockerfile exists
ls Dockerfile

# Confirm you're on the right branch
git branch
# → feature/fully-dockerized

# Build the image
docker build -t spring-boot-app:1.0.0 .
```

Expected output:
```
[+] Building 35.4s (16/16) FINISHED
 => [builder 5/5] RUN mvn clean package -DskipTests
 => [stage-1 3/3] COPY --from=builder /app/target/*.jar app.jar
 => naming to docker.io/library/spring-boot-app:1.0.0  ✅
```

---

## Step 2 — Push Image to Docker Hub

### 2.1 Generate Docker Hub Access Token

```
hub.docker.com → Account Settings
→ Security → New Access Token
→ Name: "codespaces"
→ Permissions: Read & Write
→ Generate → Copy the token (starts with dckr_pat_...)
```

> ⚠️ **Use the access token as your password** — Docker Hub no longer accepts plain passwords via CLI since 2023.

### 2.2 Login to Docker Hub

```bash
docker login -u <your-dockerhub-username>
# Enter your access token when prompted for password
```

Expected output:
```
Login Succeeded ✅
```

### 2.3 Tag the Image

```bash
# Must use full docker.io/ prefix
docker tag spring-boot-app:1.0.0 <your-dockerhub-username>/spring-boot-app:1.0.0
```

### 2.4 Push to Docker Hub

```bash
docker push <your-dockerhub-username>/spring-boot-app:1.0.0
```

Expected output:
```
1.0.0: digest: sha256:5c19ea35... size: 856  ✅
```

---

## Step 3 — Make Image Public on Docker Hub

By default Docker Hub images are **private**. Render needs public access:

```
hub.docker.com → Repositories
→ <your-username>/spring-boot-app
→ Settings → Visibility → Public
→ Save
```

Verify it's public:
```bash
# Logout and try pulling — simulates public access
docker logout
docker pull <your-dockerhub-username>/spring-boot-app:1.0.0

# If pulls without login → public ✅
# If asks for credentials → still private ❌
```

---

## Step 4 — Deploy on Render

### 4.1 Create a New Web Service

```
render.com → New → Web Service
→ Deploy an existing image from a registry
```

### 4.2 Enter Image URL

> ⚠️ **Always use the full `docker.io/` prefix** — Render requires it:

```
✅ Correct:  docker.io/fahadlxisoft/spring-boot-app:1.0.0
❌ Wrong:    fahadlxisoft/spring-boot-app:1.0.0
```

### 4.3 Configure the Service

```
Name:           spring-boot-app
Region:         pick closest to you
Instance Type:  Free
```

### 4.4 Add Environment Variables

In Render dashboard → Environment → Add the following:

```
SERVER_PORT=8080
SERVER_ADDRESS=0.0.0.0
SERVER_SERVLET_CONTEXT_PATH=/api
```

If using PostgreSQL, add:
```
SPRING_DATASOURCE_URL=<render-postgres-url>
SPRING_DATASOURCE_USERNAME=<username>
SPRING_DATASOURCE_PASSWORD=<password>
```

### 4.5 Deploy

```
→ Click "Deploy Web Service"
→ Wait for build logs to show "Your service is live" ✅
```

---

## Step 5 — Test Live URL

```bash
# Health check
curl https://<your-render-url>.onrender.com/api/health
# → {"service":"Spring Boot Application","status":"UP"}

# Hello endpoint
curl https://<your-render-url>.onrender.com/api/hello/fahad
# → {"message":"Hello, fahad","timestamp":"..."}

# API index
curl https://<your-render-url>.onrender.com/api
# → {"message":"Welcome to Spring Boot REST API!","version":"1.0.0",...}
```

---

## Common Errors & Fixes

| Error | Cause | Fix |
|---|---|---|
| `No public image found` | Using short image URL | Use `docker.io/username/image:tag` |
| `No public image found` | Image is private | Set visibility to Public on Docker Hub |
| `authentication required - insufficient scopes` | Wrong Docker credentials | Use access token, not password |
| `No such image` | Image not built yet | Run `docker build` first |
| `502 Bad Gateway` | App not bound to `0.0.0.0` | Add `SERVER_ADDRESS=0.0.0.0` env var |
| `404 on /` | No root mapping | Use `/api/health` not `/` |

---

## Useful Docker Hub Commands

```bash
# Build image
docker build -t spring-boot-app:1.0.0 .

# Tag for Docker Hub
docker tag spring-boot-app:1.0.0 docker.io/<username>/spring-boot-app:1.0.0

# Push to Docker Hub
docker push docker.io/<username>/spring-boot-app:1.0.0

# Pull from Docker Hub (anyone can run your app)
docker pull docker.io/<username>/spring-boot-app:1.0.0
docker run -p 8080:8080 docker.io/<username>/spring-boot-app:1.0.0
```

---

## Free Tier Limitations on Render

| Feature | Free Tier |
|---|---|
| Hours | 750 hrs/month |
| Sleep on inactivity | ⚠️ spins down after 15 mins |
| Wake up time | ~30 seconds on first request |
| Custom domain | ✅ supported |
| PostgreSQL | ✅ free managed DB |
| Auto-deploy | ✅ on image update |

> 💡 **Tip:** Free tier spins down after inactivity. Use [UptimeRobot](https://uptimerobot.com) (free) to ping your app every 10 minutes to keep it awake.

---

*Part of [codespaces-springboot-starter](https://github.com/muhammedfahadt/spring-boot-app/blob/main/README.md) • Muhammed Fahad • June 2026*
