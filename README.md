# 🚀 codespaces-springboot-starter

> A beginner-friendly Spring Boot template for GitHub Codespaces — with best practices baked in.

**By Muhammed Fahad • June 2026**

---

## 📖 About

This repo is a hands-on starter for running a Spring Boot REST API inside GitHub Codespaces. It covers environment setup, best practices, and real-world gotchas — designed for developers new to Codespaces.

---

## 📁 Project Structure

```
spring-boot-app/
├── .devcontainer/
│   └── devcontainer.json       ← Codespaces environment config
├── .vscode/
│   ├── launch.json
│   └── settings.json
├── src/
│   ├── main/java/com/example/
│   │   ├── SpringBootAppApplication.java
│   │   └── controller/
│   │       └── ApiController.java
│   ├── main/resources/
│   │   └── application.yaml    ← Server config
│   └── test/
├── pom.xml
├── docker-compose.yml
└── README.md
```

---

## ⚡ Quick Start

```bash
# 1. Open in Codespaces (dependencies auto-install via postCreateCommand)

# 2. Run the app
mvn spring-boot:run

# 3. Test endpoints
curl http://localhost:8080/api/health
curl http://localhost:8080/api/hello/yourname
```

---

## ✅ Testing All Endpoints

```bash
# Health check
curl https://<your-codespace-url>/api/health
# → {"service":"Spring Boot Application","status":"UP"}

# Hello endpoint
curl https://<your-codespace-url>/api/hello/fahad
# → {"message":"Hello, fahad","timestamp":"..."}

# Welcome / API index
curl https://<your-codespace-url>/api
# → {"message":"Welcome to Spring Boot REST API!","version":"1.0.0",...}
```

---

## 💡 Codespaces Best Practices

### `devcontainer.json`
- Always set `"visibility": "public"` for API ports
- Keep database ports (`5432`, `3306`) as `"visibility": "private"`
- Use `"onAutoForward": "openPreview"` for the main app port
- Add `postCreateCommand` to pre-build dependencies (`mvn clean install`)

### `application.yaml`
- Always set `server.address: 0.0.0.0` for Codespaces tunnel compatibility
- Use a single source of truth for `context-path` — either yaml or `@RequestMapping`, not both
- Set `server.port` explicitly even if using the default `8080`

### Controller Design
- Never repeat the context-path in `@RequestMapping` if `application.yaml` already defines it
- Use `@CrossOrigin(origins = "*")` for Codespaces public URL access from browser
- Return `ResponseEntity<>` for proper HTTP status code control

---

## 🌿 Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable, fully working starter — what beginners clone |
| `develop` | Active development and new features |
| `feature/app-dockerized-db` | Spring Boot runs normally + DB in Docker |
| `feature/fully-dockerized` | Both app AND DB via docker-compose |
| `docs/guides` | README and documentation improvements |

---

## 🐛 Ran into issues?

See [FIXES.md](./FIXES.md) for a full troubleshooting guide covering every real problem encountered during setup.

---

*Built and documented by Muhammed Fahad • June 2026*
