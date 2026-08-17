# Retry Demo

A small Spring Boot project for trying out **Resilience4j Retry** with a real HTTP call between two services.

## Services

- **order-service** — calls the Inventory Service.
- **inventory-service** — simulates different responses so we can see how Retry behaves.

The project uses Java 21, Spring Boot, RestClient, Apache HttpClient 5, Resilience4j, and Docker Compose.

## What this demo covers

- Retry with `@Retry`
- Retryable and ignored exceptions
- HTTP 503 responses
- Connection and read timeouts
- Exponential backoff
- Jitter
- Spring AOP / Proxy integration
- Retry event logging
- Basic Actuator endpoints
- Docker Compose

## Retry configuration

The retry policy is configured in `order-service/application.yml`:

```yaml
resilience4j:
  retry:
    instances:
      inventory-service:
        max-attempts: 4
        wait-duration: 500ms

        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
        exponential-max-wait-duration: 5s

        enable-randomized-wait: true
        randomized-wait-factor: 0.5

        retry-exceptions:
          - org.springframework.web.client.ResourceAccessException
          - org.springframework.web.client.HttpServerErrorException

        ignore-exceptions:
          - org.springframework.web.client.HttpClientErrorException
```

The remote call itself is annotated with:

```java
@Retry(name = "inventory-service")
```

This keeps retry scoped to the remote operation rather than retrying the whole order business method.

`max-attempts: 4` means one initial attempt plus up to three retries.

The retry policy currently targets `ResourceAccessException` and server-side HTTP 5xx exceptions, while client-side HTTP 4xx exceptions are ignored.

`wait-duration` sets the initial delay between attempts. Exponential backoff increases this delay after each failed attempt, while jitter adds some randomness to avoid multiple clients retrying at exactly the same time.

## RetryEventLogger

`RetryEventLogger` listens to Resilience4j retry events and logs what happens during the retry process.

It is mainly there to make the retry behavior visible while running the demo.

It can show events such as:

- a retry being scheduled
- a retry succeeding
- all attempts being exhausted

`RetryEventLogger` does **not** perform the retry itself. Resilience4j's `Retry` is responsible for that. The logger only observes and logs the events.

## Test scenarios

The Inventory Service supports four simulation modes through the `mode` query parameter:

- `SUCCESS` — reservation succeeds with HTTP 201.
- `REJECT` — reservation is not made and the service returns `NOT_RESERVED` with HTTP 201. This is a business outcome, not a transient failure.
- `SERVICE_UNAVAILABLE` — returns HTTP 503. This is a transient server-side failure and is retryable by the Order Service configuration.
- `SLOW` — sleeps for 5 seconds. With the Order Service response timeout set to 2 seconds, the call results in a ResourceAccessException, which is configured as retryable.

Examples:

```text
POST http://localhost:8081/inventory/reservations?mode=SUCCESS
POST http://localhost:8081/inventory/reservations?mode=REJECT
POST http://localhost:8081/inventory/reservations?mode=SERVICE_UNAVAILABLE
POST http://localhost:8081/inventory/reservations?mode=SLOW
```

`test.http` contains requests for these scenarios.

For `mode=SUCCESS`, the Inventory Service successfully reserves the requested inventory and returns a normal successful response.

Since no exception occurs, Resilience4j does not perform any retry.

Example output:
```text
POST http://localhost:8080/orders?mode=SUCCESS

HTTP/1.1 201 Created
Content-Type: application/json

{
  "message": "Order created successfully",
  "productId": "1001",
  "quantity": 2
}
```
This is the normal successful path: the inventory reservation succeeds and the Order Service creates the order successfully.


For `mode=REJECT`, the Inventory Service returns `NOT_RESERVED` with HTTP 201. The Order Service treats this as a business failure and throws a custom `InventoryReservationFailedException`.

This exception is intentionally **not included** in the Resilience4j `retry-exceptions`, so the operation is not retried.
The exception is handled by the global exception handler and returned to the client as HTTP `409 Conflict`.

Example output:

```text
POST http://localhost:8080/orders?mode=REJECT

HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "timestamp": "2026-08-17T05:31:26.856451097Z",
  "status": 409,
  "error": "Conflict",
  "message": "Inventory reservation was not completed. status=NOT_RESERVED",
  "path": "/orders"
}
```
This demonstrates an important distinction: a business rejection is not necessarily a transient technical failure, so retrying it would not help

ّFor `mode=SERVICE_UNAVAILABLE`, the Inventory Service returns `503 Service Unavailable`. This is configured as retryable, so Resilience4j retries the call.

Example output:

```text
INFO OrderService    : Fetching inventory for productId=1001
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=1, wait=257ms, cause=ServiceUnavailable
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=2, wait=570ms, cause=ServiceUnavailable
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=3, wait=2031ms, cause=ServiceUnavailable
DEBUG InventoryClient  : Calling Inventory Service for 1001
ERROR RetryEventLogger            : Retry exhausted: name=inventory-service, retryAttempts=4, cause=ServiceUnavailable
```

The exact timestamps and delays vary because exponential backoff and jitter are enabled.

And for `mode=SLOW`, the Inventory Service intentionally takes longer than the configured `read-timeout`.

The HTTP call therefore fails with `ResourceAccessException`, which is configured as retryable.

Example output:

```text
INFO OrderService    : Fetching inventory for productId=1001
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=1, wait=709ms, cause=ResourceAccessException
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=2, wait=1180ms, cause=ResourceAccessException
DEBUG InventoryClient  : Calling Inventory Service for 1001
WARN RetryEventLogger            : Retry event: name=inventory-service, attempt=3, wait=2163ms, cause=ResourceAccessException
DEBUG InventoryClient  : Calling Inventory Service for 1001
ERROR RetryEventLogger            : Retry exhausted: name=inventory-service, retryAttempts=4, cause=ResourceAccessException
```

## Timeouts

Order Service uses Apache HttpClient 5 underneath Spring `RestClient`.

Example configuration:

```yaml
inventory:
  base-url: ${INVENTORY_BASE_URL:http://localhost:8081}
  connect-timeout: 1s
  read-timeout: 2s
```

- `connect-timeout` controls how long the client waits while establishing a connection.
- `read-timeout` controls how long it waits for the response.

A connection failure or timeout can result in `ResourceAccessException`, which is retryable in this demo.

## Run locally

Start `inventory-service` on port `8081` and `order-service` on port `8080`.

For local execution, Order Service uses:

```text
http://localhost:8081
```

## Run with Docker Compose

```bash
git clone https://github.com/shirinjamshidiyan/backend-patterns.git
cd backend-patterns/retry-demo
docker compose up --build
```

Then:

- Order Service: `http://localhost:8080`
- Inventory Service: `http://localhost:8081`

Inside Docker, Order Service reaches Inventory Service through:

```text
http://inventory-service:8081
```

This is configured with `INVENTORY_BASE_URL` in `docker-compose.yml`.

Stop the services with:

```bash
docker compose down
```

## Useful endpoints

```text
GET http://localhost:8080/actuator/health
GET http://localhost:8081/actuator/health
```

See `test.http` for the requests used to exercise the four scenarios.
