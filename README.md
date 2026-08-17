# AI Message Data Pipeline

Spring Boot implementation of the initial ETL service described in [architecture.md](architecture.md).

## Modules

- `alert-models`: a plain, Spring-free jar containing the alert contract records accepted by the pipeline.
- `log-models`: a plain, Spring-free jar containing the log contract records and wrapped query response.
- `pipeline-service-models`: a plain, Spring-free jar containing pipeline-owned API response and error records.
- `pipeline-service`: the deployable Spring Boot WebFlux application; it depends on the model jars and owns the reactive controllers and Spring HTTP interfaces.

In a production repository layout, `alert-models` should be built and exported by the Alert service, which owns the alert schema. Likewise, `log-models` should be built and exported by the Log service, which owns the log schema. The pipeline should consume released versions of those artifacts rather than maintaining copies, ensuring contract changes are published by their owning services. The Spring HTTP interface remains in `pipeline-service`, so neither model jar has a Spring dependency.

## What is implemented

- `POST /v1/alerts`: validates the full alert contract, reactively queries the Log service for every distinct correlation key over the alert window plus a configurable tolerance, merges/deduplicates typed log records, then publishes to `alerts.enriched.v1` without blocking a request thread.
- `POST /v1/sync-logs`: reactively pulls one enabled namespace/time window and publishes every log to `logs.synced.v1` in order.
- `POST /v1/scheduler/sync-logs`: fans a time window out through the load balancer to `/v1/sync-logs` for every configured namespace, in parallel, then returns an aggregate summary.
- RedPanda messages are keyed by namespace; producer acknowledgements are `all` and idempotence is enabled.
- Actuator health/metrics, structured errors, and request validation.
- Prometheus metrics at `GET /actuator/prometheus`, including alert admission latency, per-namespace log export latency, and the number of logs exported per tenant.
- End-to-end WebFlux using `Mono`/`Flux`, including Spring HTTP service interfaces backed by `WebClient` and the JDK `java.net.http.HttpClient`, plus non-blocking Kafka acknowledgement handling.

The endpoints still wait for all required RedPanda acknowledgements before returning success. “Reactive” changes how that waiting is represented; it does not weaken the architecture's durability contract.

## Metrics

The Prometheus endpoint is enabled at `GET /actuator/prometheus`. The pipeline publishes:

| Micrometer metric | Prometheus series | Description |
|---|---|---|
| `pipeline.alert.admit.duration` | `pipeline_alert_admit_duration_seconds_{count,sum,max}` | End-to-end alert admission latency, including log correlation and the Redpanda acknowledgement. |
| `pipeline.logs.export.duration` | `pipeline_logs_export_duration_seconds_{count,sum,max}{namespace="..."}` | End-to-end latency for exporting one requested log window, tagged by enabled namespace. |
| `pipeline.logs.exported` | `pipeline_logs_exported_total{namespace="..."}` | Number of logs successfully exported to Redpanda, tagged by tenant namespace. |

The `namespace` tag is bounded by `pipeline.enabled-namespaces`; requests for other namespaces are rejected before tenant-tagged meters are created.

Reconciliation and deduplication across requests are intentionally not implemented because the architecture marks them as future work.

## Requirements

- Java 21+
- Maven 3.9+
- RedPanda/Kafka and a Log service implementing `GET /logs`
- Docker for the Redpanda Testcontainers integration tests; those tests are skipped when Docker is unavailable.

## Build and run

```bash
mvn clean verify
LOG_SERVICE_BASE_URL=http://localhost:8081 mvn -pl pipeline-service spring-boot:run
```

## TODO

- Add production-ready containerization for `pipeline-service`, including a Dockerfile and local infrastructure orchestration for RedPanda.
- Configure and document TLS for the pipeline-to-RedPanda connection, including the security protocol and certificate/truststore settings. The architecture assumes this connection is encrypted, but the current runtime configuration does not yet enforce it.

Important environment variables:

| Variable | Default |
|---|---|
| `REDPANDA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `LOG_SERVICE_BASE_URL` | `http://localhost:8081` |
| `CORRELATION_TOLERANCE` | `30s` |
| `LOG_SERVICE_CONNECT_TIMEOUT` | `2s` |
| `LOG_SERVICE_REQUEST_TIMEOUT` | `10s` |
| `PIPELINE_SERVICE_BASE_URL` | `http://localhost:8080` |
| `ENABLED_NAMESPACES` | empty comma-separated list |
| `PUBLISH_TIMEOUT` | `10s` |

API authentication is intentionally outside this service and is expected to be enforced by the API gateway.

Topic creation/retention is infrastructure-owned. Create `alerts.enriched.v1` and `logs.synced.v1` with the architecture's initial seven-day retention before production deployment.
