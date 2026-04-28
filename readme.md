# HelloV2 — Observability Playground (Spring Boot)

HelloV2 is a small Spring Boot playground used to learn and validate an observability setup before applying it in a production backend.

This repo focuses on the three core pillars of observability:
- **Logs**: structured JSON logs written to a local file → shipped by Fluent Bit → stored in Elasticsearch → explored in Kibana
- **Tracing**: request traces/spans exported to Zipkin
- (Next step) **Metrics**: Prometheus/Grafana/Alertmanager (planned)

---

## What’s running in Docker

Docker Compose stack (in `observability/docker-compose.yml`) starts:

- **Elasticsearch** (log storage & indexing)
    - URL: `http://localhost:9200`
- **Kibana** (UI for searching/exploring logs in Elasticsearch)
    - URL: `http://localhost:5601`
- **Fluent Bit** (tails JSON log files and forwards them to Elasticsearch)
- **Zipkin** (distributed tracing UI)
    - URL: `http://localhost:9411`

---

## Prerequisites

- Java + Maven (for running the Spring Boot app)
- Docker Desktop (or Docker Engine) with Compose v2 (`docker compose ...`)
- Free ports on localhost: `9200`, `5601`, `9411`

---

## Project structure

- `src/` — Spring Boot application
- `logs/` — runtime log output (ignored by Git)
- `observability/` — local observability stack
    - `docker-compose.yml`
    - `fluent-bit/fluent-bit.conf`
    - `fluent-bit/parsers.conf`

---

## Quick start

### 1) Configure local secrets

From `observability/`:

```shell
copy .env.example .env
```

Fill `SMTP_*` values, create `secrets/smtp_password`, and set `SMTP_PASSWORD_FILE=./secrets/smtp_password` if you want email alerts to work locally. See `docs/observability-security.md` for details.

Without a real SMTP secret, the stack can still be used for log, metrics, and tracing checks, but email notifications will not be delivered.

### 2) Start the Docker observability stack

From `observability/`:

```shell
docker compose up -d
```
```shell
docker compose ps
```

### 3) Run the Spring Boot application

From project root:
```shell
mvn spring-boot:run
```

The application writes JSON logs to ./logs/ and exports traces to Zipkin.

### 4) Generate some traffic (browser)
   http://localhost:8080/
   http://localhost:8080/hello?name=Kasia
   http://localhost:8080/simulate-error (intentionally returns HTTP 500)


###  5) Where to find data

#### Tracing (Zipkin)
   UI: http://localhost:9411
   Zipkin stores and shows traces/spans (request flow + timings + errors).
   It is not a log storage system.

#### Logs (Kibana)
   UI: http://localhost:5601
   Kibana reads data from Elasticsearch.

#### Logs (Elasticsearch directly)

List indices:
curl http://localhost:9200/_cat/indices?v
Fetch sample documents:
curl "http://localhost:9200/hello-logs-*/_search?size=5&pretty"
Fluent Bit

Fluent Bit does not store logs. It:

tails local JSON log files (read-only mount)
parses them
forwards events to Elasticsearch
stores tail offsets in a small DB (Docker volume) so it can resume after restarts

Check Fluent Bit logs:

```shell
cd .\observability
docker compose logs -f fluent-bit
Docker Compose command cheat sheet
```

Run these commands from HelloV2/observability unless stated otherwise.

Start everything
```shell
docker compose up -d
```
Show status
```shell
docker compose ps
```
View logs (all services)
```shell
docker compose logs -f
```
View logs (single service)
```shell
docker compose logs -f fluent-bit
```
Restart services
```shell
docker compose restart
```
Stop/remove containers (keep volumes/data)
```shell
docker compose down
```

Full reset (remove containers + volumes/data)
This removes Elasticsearch data (esdata) and Fluent Bit tail DB (fluentbit_db):
```shell
docker compose down -v --remove-orphans
```

List volumes / remove volumes manually
```shell
docker volume ls
docker volume rm observability_esdata
docker volume rm observability_fluentbit_db
```

Remove all unused volumes globally (careful)
```shell
docker volume prune
```
