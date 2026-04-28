# Observability security notes

## Local SMTP configuration

Alertmanager uses `observability/alertmanager/alertmanager.yml` as a template. Docker Compose renders it at container startup and reads the SMTP password from a mounted secret file.

For local runs:

1. Copy `observability/.env.example` to `observability/.env`.
2. Set `SMTP_SMARTHOST`, `SMTP_FROM`, `SMTP_USERNAME`, and `SMTP_TO` in `observability/.env`.
3. Create `observability/secrets/smtp_password` with the real SMTP password or app password.
4. Set `SMTP_PASSWORD_FILE=./secrets/smtp_password` in `observability/.env`.
5. Replace the example `GF_SECURITY_ADMIN_PASSWORD` with a local password in `observability/.env`.

The Docker Compose file contains safe local defaults matching `observability/.env.example`, and Docker Compose overlays values from `observability/.env` when present. The example values are placeholders for local startup only; real credentials belong in `.env` and `observability/secrets/`.

Do not commit `observability/.env` or files in `observability/secrets/` that contain real credentials.

The previously exposed SMTP password must be rotated in the mail provider before this repository is shared or deployed.

## Production recommendations

- Use Docker secrets, a platform secret manager, or another managed secret store for SMTP, Grafana, Elasticsearch, and Kibana credentials.
- Keep TLS enabled for SMTP and use authenticated access to Elasticsearch, Kibana, Grafana, and Alertmanager.
- Do not expose Elasticsearch, Kibana, Grafana, Alertmanager, or Fluent Bit endpoints directly to the public internet without authentication and network restrictions.
- Bind local-only services to localhost where possible, and use firewall rules, VPN, reverse proxy authentication, or cloud security groups for server deployments.
- Review log contents for personal data, tokens, authorization headers, cookies, and other sensitive fields before shipping logs to Elasticsearch.
