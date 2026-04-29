# HelloV2 - Observability Playground (Spring Boot)

HelloV2 is a small Spring Boot playground used to learn and validate a local observability setup before applying it in a production backend.

This repo currently covers the main observability areas:

- **Logs**: structured JSON logs written to a local file -> shipped by Fluent Bit -> stored in Elasticsearch -> explored in Kibana
- **Tracing**: request traces/spans exported to Zipkin
- **Metrics**: Spring Boot Actuator + Micrometer + Prometheus
- **Alerting**: Prometheus alert rules -> Alertmanager email receiver
- **Dashboards**: Grafana container is started locally, but Prometheus datasource and dashboards are not provisioned in this repo yet

---

## What's running in Docker

Docker Compose stack (in `observability/docker-compose.yml`) starts:

- **Elasticsearch** (log storage and indexing)
    - URL: `http://localhost:9200`
- **Kibana** (UI for searching/exploring logs in Elasticsearch)
    - URL: `http://localhost:5601`
- **Fluent Bit** (tails JSON log files and forwards them to Elasticsearch)
- **Zipkin** (distributed tracing UI)
    - URL: `http://localhost:9411`
- **Prometheus** (scrapes Spring Boot Actuator metrics and evaluates alert rules)
    - URL: `http://localhost:9090`
- **Alertmanager** (receives Prometheus alerts and can send email notifications)
    - URL: `http://localhost:9093`
- **Grafana** (local dashboard UI)
    - URL: `http://localhost:3000`
    - Prometheus datasource and dashboards must be added manually

---

## Prerequisites

- Java 21 + Maven (for running the Spring Boot app)
- Docker Desktop (or Docker Engine) with Compose v2 (`docker compose ...`)
- Free ports on localhost: `8080`, `9200`, `5601`, `9411`, `9090`, `9093`, `3000`

Prometheus scrapes the Spring Boot application running on the host through `host.docker.internal:8080/actuator/prometheus`.
This works out of the box on Docker Desktop. On a plain Linux Docker Engine installation, `host.docker.internal` may require extra Docker configuration.

---

## Project structure

- `src/` - Spring Boot application
- `logs/` - runtime log output (ignored by Git)
- `observability/` - local observability stack
    - `docker-compose.yml`
    - `.env.example` - local environment template for SMTP and Grafana credentials
    - `fluent-bit/` - Fluent Bit pipeline and JSON parser config
    - `prometheus/` - Prometheus scrape config and alert rules
    - `alertmanager/` - Alertmanager email receiver template and render script
    - `secrets/` - local secret files (real secrets ignored by Git)
- `docs/observability-security.md` - local secrets and security notes

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

The application writes JSON logs to `./logs/`, exports traces to Zipkin, and exposes Actuator/Micrometer endpoints for Prometheus.

### 4) Generate some traffic

Open in a browser or call with `curl`:

```text
http://localhost:8080/
http://localhost:8080/hello?name=Kasia
http://localhost:8080/slow?ms=500
http://localhost:8080/simulate-error
```

`/simulate-error` intentionally returns HTTP 500 and is useful for log, trace, metric, and alert checks.

---

## Actuator and Micrometer

Spring Boot Actuator is enabled and exposes selected endpoints:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/metrics`
- `http://localhost:8080/actuator/prometheus`

Micrometer Prometheus registry is included in the application dependencies. Prometheus scrapes:

```text
host.docker.internal:8080/actuator/prometheus
```

from inside the Docker Compose network.

---

## Logs

### Kibana

UI: `http://localhost:5601`

Kibana reads log data from Elasticsearch.

### Elasticsearch directly

List indices:

```shell
curl http://localhost:9200/_cat/indices?v
```

Fetch sample documents:

```shell
curl "http://localhost:9200/hello-logs-*/_search?size=5&pretty"
```

### Fluent Bit

Fluent Bit does not store logs. It:

- tails local JSON log files from the read-only `../logs` mount
- parses them with `observability/fluent-bit/parsers.conf`
- forwards events to Elasticsearch
- stores tail offsets in a Docker volume so it can resume after restarts

Check Fluent Bit logs from `observability/`:

```shell
docker compose logs -f fluent-bit
```

---

## Tracing

Zipkin UI: `http://localhost:9411`

Zipkin stores and shows traces/spans, including request flow, timings, and errors. It is not a log storage system.

---

## Metrics and alerting

### Prometheus

Prometheus UI: `http://localhost:9090`

Prometheus scrapes the Spring Boot application through:

```text
host.docker.internal:8080/actuator/prometheus
```

Configured alert rules:

- `HelloV2Down` - fires when Prometheus cannot scrape the application
- `HelloV2HighErrorRate` - fires when more than 20% of HTTP requests are 5xx in a 1 minute window

### Alertmanager and email alerts

Alertmanager UI: `http://localhost:9093`

Alertmanager is configured with an email receiver. For real email delivery, configure these values in `observability/.env`:

- `SMTP_SMARTHOST`
- `SMTP_FROM`
- `SMTP_USERNAME`
- `SMTP_TO`
- `SMTP_PASSWORD_FILE`

`SMTP_PASSWORD_FILE` should point to a local secret file, for example `./secrets/smtp_password`.

Without a real SMTP configuration, Prometheus and Alertmanager still start and can be used for local checks, but email alerts will not be sent.

---

## Grafana

Grafana UI: `http://localhost:3000`

- Login: `admin` by default
- Password: value of `GF_SECURITY_ADMIN_PASSWORD` from `observability/.env` or the Compose default
- Prometheus datasource must be added manually as `http://prometheus:9090`
- Dashboard provisioning is not present in this repo yet

---

## Docker Compose command cheat sheet

Run these commands from `HelloV2/observability` unless stated otherwise.

Start everything:

```shell
docker compose up -d
```

Show status:

```shell
docker compose ps
```

View logs for all services:

```shell
docker compose logs -f
```

View logs for one service:

```shell
docker compose logs -f fluent-bit
```

Restart services:

```shell
docker compose restart
```

Stop/remove containers while keeping volumes/data:

```shell
docker compose down
```

Full reset, including containers, volumes, and data:

```shell
docker compose down -v --remove-orphans
```

This removes Elasticsearch data (`esdata`) and Fluent Bit tail DB (`fluentbit_db`).

List volumes / remove volumes manually:

```shell
docker volume ls
docker volume rm observability_esdata
docker volume rm observability_fluentbit_db
```

Remove all unused volumes globally:

```shell
docker volume prune
```
