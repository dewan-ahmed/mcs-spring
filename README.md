# Notify Service (Spring)

Java counterpart of the NotifyService CI/CD demo. Logic is intentionally small. **Stores and queues are in-memory stand-ins** — nothing is shared across processes, and nothing talks to a database, broker, or cloud provider.

Authentication and logging are production-shaped: the core API issues signed JWTs, the public API requires an API key, and every HTTP call carries a correlation id.

## Workloads

| Module | Role |
| --- | --- |
| `notify-core-api` | `POST /api/auth/login` issues a JWT. Authenticated callers `POST /api/notifications` and `GET /api/notifications/{id}`. Health: `/health`, `/health/ready`. |
| `notify-public-api` | `POST /api/callbacks/status` and `GET /api/notifications/{id}/status`, both requiring `X-Api-Key`. Same health endpoints. |
| `notify-worker` | Dequeues items, calls the sender, updates status. On shutdown it finishes the in-flight item, then exits. Enqueues a synthetic notification on an interval so deployed logs still show activity. |

The in-memory queue does **not** cross process boundaries, so the worker cannot see items posted to Core. That is expected.

Each process logs `notify.app-environment` at startup. HTTP access lines include `correlationId` (request header `X-Correlation-Id`, generated when absent). Set `LOGGING_STRUCTURED_FORMAT_CONSOLE=ecs` for JSON logs.

## Interface → production mapping

| Interface | This repo | Production |
| --- | --- | --- |
| `NotificationStore` | `ConcurrentHashMap` | SQL database |
| `NotificationQueue` | `LinkedBlockingQueue` | Message broker |
| `NotificationSender` | SLF4J no-op | SMS / email provider |
| Core authentication | HMAC JWT (`HS256`) | Identity provider |
| Public authentication | `X-Api-Key` | API gateway |

Allowed callback statuses: `queued`, `sent`, `failed`, `delivered`.

## Run locally

Java 21+ and Maven 3.9+.

```bash
mvn verify
```

Core API (port 5080). The `local` profile is a demo-only credential set; do not use it outside a workstation.

```bash
mvn -pl notify-core-api spring-boot:run -Dspring-boot.run.profiles=local
```

```bash
TOKEN=$(curl -s -X POST http://localhost:5080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"notify-admin","password":"local-dev-password"}' \
  | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')

curl -X POST http://localhost:5080/api/notifications \
  -H "Authorization: Bearer $TOKEN" \
  -H 'Content-Type: application/json' \
  -d '{"recipient":"user@example.com","body":"hello"}'
```

Public API (port 5081) and worker:

```bash
mvn -pl notify-public-api spring-boot:run -Dspring-boot.run.profiles=local
mvn -pl notify-worker spring-boot:run
```

```bash
curl http://localhost:5081/api/notifications/<id>/status \
  -H 'X-Api-Key: local-dev-api-key'
```

Without the `local` profile the APIs refuse to start until secrets are set:

| Variable | Used by |
| --- | --- |
| `JWT_SECRET` | Core API. At least 32 bytes. |
| `NOTIFY_ADMIN_USERNAME` / `NOTIFY_ADMIN_PASSWORD` | Core API login. Username defaults to `notify-admin`. |
| `NOTIFY_API_KEY` | Public API `X-Api-Key`. |
| `APP_ENVIRONMENT` | Logged at startup. Defaults to `local`. |

Passwords are checked with BCrypt and are never written to logs. Notification bodies are stored but not written to info logs.

## Containers

Images expect a packaged jar as the Docker build context (`mvn -pl notify-core-api -am package`, then `docker build` that module). `docker compose up --build` starts all three with demo credentials supplied as environment variables. Override those variables before using the compose file anywhere else.
