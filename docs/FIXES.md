# 🐛 FIXES — Codespaces + Spring Boot Troubleshooting Guide

> Real problems encountered while setting up this project, and exactly how each was fixed.

---

## Quick Reference

| # | Problem | Fix |
|---|---|---|
| 1 | `404` on `/api/hello/{name}` | Remove duplicate `@RequestMapping("/api")` |
| 2 | `502 Bad Gateway` on public URL | Add `address: 0.0.0.0` to `application.yaml` |
| 3 | Port not publicly accessible | Add `"visibility": "public"` to `devcontainer.json` |
| 4 | `DNS_PROBE_FINISHED_NXDOMAIN` | Change Windows DNS to `8.8.8.8` / `1.1.1.1` |

---

## Fix 1 — 404 Not Found on `/api/hello/{name}`

### Symptom

```bash
$ curl http://localhost:8080/api/hello/fahad

{"timestamp":"2026-06-09T19:48:34.172+00:00",
 "status":404,"error":"Not Found",
 "path":"/api/hello/fahad"}
```

### Root Cause

Double `/api` prefix. The context path was set in two places simultaneously:

> `application.yaml` had `server.servlet.context-path=/api` **AND** `ApiController` had `@RequestMapping("/api")` at class level. Spring combined them into `/api/api/hello/{name}` — which never matched any request.

### Fix

Remove `@RequestMapping("/api")` from the controller. Let `application.yaml` own the context path exclusively.

**Before (broken):**
```java
@RestController
@RequestMapping("/api")   // ❌ conflicts with context-path
@CrossOrigin(origins = "*")
public class ApiController {
    @GetMapping("/hello/{name}")
    public ResponseEntity<...> hello(@PathVariable String name) { ... }
}
```

**After (fixed):**
```java
@RestController
// @RequestMapping removed ✅
@CrossOrigin(origins = "*")
public class ApiController {
    @GetMapping("/hello/{name}")
    public ResponseEntity<...> hello(@PathVariable String name) { ... }
}
```

**`application.yaml`:**
```yaml
server:
  port: 8080
  servlet:
    context-path: /api   # single source of truth for /api prefix
```

> ✅ **Rule:** Set the context path in one place only — either `application.yaml` OR `@RequestMapping`, never both.

---

## Fix 2 — 502 Bad Gateway on Public URL

### Symptom

```bash
$ curl http://localhost:8080/api/hello/fahad
{"message":"Hello, fahad","timestamp":"..."}  ✅

$ curl https://stunning-orbit-xxxx-8080.app.github.dev/api/health
< HTTP/2 502
< content-length: 0             ❌
```

### Root Cause

> Spring Boot was binding to `127.0.0.1` (localhost only). The GitHub Codespaces tunnel proxy runs outside the container and cannot reach `127.0.0.1` — it needs the app bound to `0.0.0.0` (all interfaces).

### Fix

Add `server.address: 0.0.0.0` to `application.yaml`:

```yaml
server:
  port: 8080
  address: 0.0.0.0        # ← ADD THIS LINE
  servlet:
    context-path: /api
```

Verify after restart:

```bash
$ ss -tlnp | grep 8080
LISTEN  0.0.0.0:8080   ✅  ← correct
# NOT
LISTEN  127.0.0.1:8080 ❌  ← tunnel cannot reach this
```

---

## Fix 3 — Port Not Publicly Accessible

### Symptom

Port forwarded but requests silently dropped or returning 502 — because port visibility defaults to **Private**.

### Fix — `devcontainer.json`

Add `"visibility": "public"` to port 8080. Keep the DB port private:

```jsonc
{
  "name": "Spring Boot Development",
  "image": "mcr.microsoft.com/devcontainers/java:1-21",

  "forwardPorts": [8080, 5432],
  "portsAttributes": {
    "8080": {
      "label": "Spring Boot App",
      "visibility": "public",       // ← ADD THIS
      "onAutoForward": "openPreview"
    },
    "5432": {
      "label": "PostgreSQL",
      "visibility": "private",      // ← DB stays private
      "onAutoForward": "ignore"
    }
  }
}
```

> ⚠️ **Requires a container rebuild to take effect.**
> `Ctrl+Shift+P` → `"Codespaces: Rebuild Container"`

### Force visibility immediately (no rebuild needed)

```bash
gh codespace ports visibility 8080:public -c $CODESPACE_NAME

# Verify
gh codespace ports -c $CODESPACE_NAME
# PORT   VISIBILITY
# 8080   public      ✅
```

---

## Fix 4 — DNS_PROBE_FINISHED_NXDOMAIN in Browser

### Symptom

```
This site can't be reached
Check if there is a typo in stunning-orbit-xxxx-8080.app.github.dev
DNS_PROBE_FINISHED_NXDOMAIN
```

Curl inside the Codespace terminal works fine — only the browser fails.

### Root Cause

> Windows DNS server cannot resolve `*.app.github.dev`. Caused by a corporate/ISP DNS blocking GitHub Codespaces domains, or a stale DNS cache.

### Fix — Change Windows DNS

**Step 1:** Open Network Adapter Settings
```
Win + R  →  ncpa.cpl  →  Enter
```
Right-click active network → Properties → Internet Protocol Version 4 (TCP/IPv4) → Properties

**Step 2:** Set DNS servers
```
Preferred DNS:  8.8.8.8    (Google)
Alternate DNS:  1.1.1.1    (Cloudflare)
```

**Step 3:** Flush DNS — open CMD as Administrator
```cmd
ipconfig /flushdns
ipconfig /release
ipconfig /renew
```

**Step 4:** Diagnose with nslookup
```cmd
nslookup stunning-orbit-xxxx-8080.app.github.dev

# Returns an IP  → DNS works, firewall may be blocking
# NXDOMAIN       → DNS is blocking *.app.github.dev
```

### Alternative — VS Code Desktop

If on a corporate network that permanently blocks `*.app.github.dev`:

- Install **VS Code desktop** + **GitHub Codespaces extension**
- Connect to your Codespace from VS Code
- Port forwarding works through a secure tunnel that bypasses DNS restrictions

---

*Part of [codespaces-springboot-starter](./README.md) • Muhammed Fahad • June 2026*
