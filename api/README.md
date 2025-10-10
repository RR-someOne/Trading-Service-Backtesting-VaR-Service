# API Module (REST + gRPC)

Lightweight control plane exposing status and control endpoints over REST and gRPC with API-key auth loaded from ENV, AWS Secrets Manager, or HashiCorp Vault.

## Build & Run

- Build: use the multi-project build from repo root
- Run REST/gRPC server:

```
./gradlew :api:run
```

Env vars (optional):
- API_REST_PORT: default 8080
- API_GRPC_PORT: default 9090
- API_KEY: API key value (plaintext) when SECRETS_BACKEND=env
- SECRETS_BACKEND: env | aws | vault (default env)
- SECRETS_KEY:
  - aws: Secrets Manager secret id (e.g., trading/api-key)
  - vault: KV v2 API path (e.g., secret/data/trading/api-key) — provider returns raw JSON
- AWS_REGION: AWS region for Secrets Manager (e.g., us-east-1)
- VAULT_ADDR: http(s)://host:8200
- VAULT_TOKEN: token for Vault KV

## REST Endpoints
- GET /status -> {"status":"ok"}
- POST /control/{action} -> accepts: start-ingestion, stop-ingestion

Auth: pass header `x-api-key: <value>` when API_KEY is configured.

## gRPC
Server starts on API_GRPC_PORT; service methods are not yet exposed. Add a proto and service bindings as needed.

## Notes
- Vault provider returns the raw KV v2 JSON payload; parse to extract your key as needed.
- AWS Secrets Manager requires valid AWS credentials and region.
- This module is designed to run standalone; embed or wire into the main application as needed for real control hooks.
