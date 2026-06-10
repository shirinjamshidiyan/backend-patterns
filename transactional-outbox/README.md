<div align="center">

# Transactional Outbox Pattern

</div>

A small Spring Boot project that focuses on the Transactional Outbox pattern using PostgreSQL and Kafka.

This project is extracted and simplified from the Transactional Outbox implementation used in [EventFlow Commerce: Outbox Saga](https://github.com/shirinjamshidiyan/eventflow-commerce-outbox-saga).


The original project uses Transactional Outbox in `order-service`, `inventory-service`, and `payment-service` as part of a larger event driven saga. This smaller project isolates the pattern so the transaction boundary, outbox table, row claiming, owner based protection, retry behavior, recovery job, Kafka headers, and outbox metrics can be understood without the full ecommerce workflow.

---

## What this project focuses on

The project shows one focused flow:

```text
POST /orders
  -> check requestId for idempotent order creation
  -> save Order
  -> save OrderCreated outbox event in the same database transaction
  -> scheduled outbox publisher claims eligible outbox rows
  -> claimed rows move to PROCESSING state for race-condition handling
  -> publisher sends the event to Kafka
  -> outbox row is marked PUBLISHED after Kafka acknowledgement
```

The subject of the project is how a service can persist a business change and publish the corresponding event without using a distributed transaction.

Covered engineering concerns:

```text
Transactional Outbox
Idempotent command handling with requestId
Atomic business write and outbox write
Outbox row claiming with SELECT ... FOR UPDATE SKIP LOCKED
Owner based protection for claimed rows
Multi instance outbox publisher safety
Kafka publish acknowledgement handling
Kafka headers extracted from the event envelope
Retry and linear backoff for failed publishing
Recovery of old PROCESSING rows
At least once publishing semantics
Kafka producer idempotency configuration
Correlation ID in logs
Outbox metrics with Micrometer and Actuator
Docker Compose based local environment
```

---

## Architecture

| Component       | Responsibility                                                             |
| --------------- | -------------------------------------------------------------------------- |
| `order-service` | Accepts order requests, saves orders, writes outbox rows, publishes events |
| `order-db`      | PostgreSQL database storing business records and outbox events             |
| Kafka           | Event broker receiving `OrderCreated` events                               |
| `kafka-init`    | Pre creates the Kafka topic                                                |
| Kafka UI        | Local topic and message browser                                            |


The business model is simplified to one `Order` entity and one `OrderCreated` event.

The service can run as a single instance, but the outbox design also considers multiple publisher instances. 

---

## Main flow

### 1. Create an order

The API receives a simple order request:

```http
POST http://localhost:8080/orders
Content-Type: application/json
X-Correlation-Id: aaaaaaaa-1111-2222-3333-aaaaaaaaaaaa
```

```json
{
  "requestId": "10000000-0000-0000-0000-000000000001",
  "customerId": "30000000-0000-0000-0000-000000000001",
  "totalAmount": 50.00
}
```

`requestId` is used as an idempotency key for order creation. If the same request is sent again, the service returns the existing order instead of creating a second order and a second outbox event.

This protects the API from duplicate client submissions.

### 2. Save business record and outbox row atomically

The service writes both rows in one local database transaction:

```text
begin transaction
    insert into orders
    insert into outbox_events
commit transaction
```

If the transaction commits, both records exist. If the transaction rolls back, both writes are discarded. 
This is the core guarantee of the Transactional Outbox pattern. The service does not publish to Kafka inside the same transaction. It only stores the outgoing event as data. Publishing happens later.

### 3. Claim outbox rows

Each outbox event has one of five states that show where it is in the publishing lifecycle.

| Status       | Meaning                                                                 |
| ------------ | ----------------------------------------------------------------------- |
| `PENDING`    | Event has been created but not published yet                            |
| `PROCESSING` | Event has been claimed by a publisher instance                          |
| `PUBLISHED`  | Kafka acknowledged the event and the database row was marked complete   |
| `FAILED`     | Previous publish attempt failed and the event can be retried            |
| `DEAD`       | Retry limit was reached and the event will not be retried automatically |


When an event is first created and stored in the database, it starts in the `PENDING` state.
A simple outbox demo can sometimes publish rows directly from `PENDING` to `PUBLISHED`. This project does not do that.
It uses `PROCESSING` because publishing the event does not happen immediately and because more than one publisher instance may run at the same time.



A scheduled publisher then looks for publishable rows, such as `PENDING` rows or `FAILED` rows whose retry time has passed, and claims them using PostgreSQL row locks.

```sql
SELECT *
FROM outbox_events
WHERE status = 'PENDING'
   OR (
       status = 'FAILED'
       AND (next_retry_at IS NULL OR next_retry_at <= NOW())
   )
ORDER BY created_at ASC
LIMIT :limit
FOR UPDATE SKIP LOCKED;
```

PostgreSQL pessimistic locking with `FOR UPDATE SKIP LOCKED` matters when more than one publisher instance is running. One instance locks and claims a row.
Other instances skip that locked row instead of waiting for it or claiming it again.
After claiming, the row changes to `PROCESSING` and the publisher writes its owner value into `processing_by`.

`PROCESSING` separates events that are ready to publish from events that are already claimed by a publisher instance. This makes multi instance publishing easier to control.

### 4. Publish to Kafka

The event is not published as a raw business (e.g. `OrderCreated`) payload. It is wrapped in an `EventEnvelope`, which adds common metadata such as event ID, event type, version, correlation ID, causation ID, and source around the business payload.

The payload carries business data. The envelope carries metadata for correlation, versioning, and event identity.

Example of `EventEnvelope`:

```json
{
  "eventId": "uuid",
  "eventType": "OrderCreated",
  "eventVersion": 1,
  "correlationId": "uuid",
  "causationId": null,
  "occurredAt": "time",
  "source": "order-service",
  "payload": {
    "orderId": "uuid",
    "customerId": "uuid",
    "amount": 50.00
  }
}
```

The full envelope is sent as the Kafka message body. Selected envelope fields are also copied to Kafka message headers:

```text
event-id
event-type
event-version
correlation-id
causation-id
source
```

This keeps the message body self-contained while also making important routing and observability metadata available as Kafka headers.
This makes the event easier to inspect in Kafka UI and easier to process by consumers in larger systems.


The project also enables Kafka producer idempotency. That protects against duplicate records caused by producer retry behavior inside a single producer session. It does not make the whole outbox workflow exactly once. If the service publishes successfully but fails before marking the row as `PUBLISHED`, the same outbox event can still be sent again later.

### 5. Publish result, retry, and recovery

After a publisher claims an outbox row, the row moves to `PROCESSING`. From this point, the event is no longer just waiting. It is owned by one publisher instance that is trying to send it to Kafka.

A successful publishing follows this path:

```text
PENDING -> PROCESSING -> PUBLISHED
```

If publishing fails, the event is changed back to `FAILED` and can be retried after its retry time has passed:

```
PENDING -> PROCESSING -> FAILED -> PROCESSING -> PUBLISHED
```

When publishing fails, the retry counter is increased, the error is stored, and the next retry time is calculated with linear backoff:

```text
first failure  -> retry after 10 seconds
second failure -> retry after 20 seconds
third failure  -> retry after 30 seconds
```

If the retry limit is reached, the event is changed to `DEAD` and is no longer retried automatically:

```
PENDING -> PROCESSING -> FAILED -> ... -> DEAD
```

There is also a recovery path:

```
PROCESSING -> FAILED
```

This is used when a service instance claims an event but then crashes or gets stuck before it can finish the publishing attempt. 
In this case, a claimed row may remain in `PROCESSING` for too long. 
The recovery job detects old `PROCESSING` rows:

```text
status = PROCESSING
processing_started_at older than the configured timeout
```

and moves them back to `FAILED` so they can be retried.

#### Owner guard protection

One key rule:

```text
A publisher can only update an outbox event if it still owns the claimed row.
```
Status updates are owner aware. A publisher can only mark an event as `PUBLISHED`, `FAILED`, or `DEAD` if it still owns the claimed row.

This rule is enforced at the database level by conditional update queries

```sql
WHERE id = :eventId
  AND status = 'PROCESSING'
  AND processing_by = :owner
```

This condition means the PROCESSING row must still belong to the same publisher instance.
If the recovery job or another publisher has already changed the row, the update affects zero rows.
The **owner guard** rule protects against races such as:

```text
1. publisher A claims an event
2. publisher A becomes slow or blocked
3. recovery job resets the stale PROCESSING row
4. publisher B later claims the same event
5. publisher A returns late and tries to mark the row PUBLISHED
```

With owner based update conditions, publisher A cannot overwrite the row unless it still owns it.

This owner guard is the reason the project keeps both `PROCESSING` and `processing_by`. They make the outbox publisher safer when more than one instance is running.

---

## Delivery behaviour

This project provides **at least once publishing**. This is expected behavior for a standard Transactional Outbox implementation.

Kafka Producer idempotency helps with producer level retries for the same send operation. It does not deduplicate separate application level send attempts that happen because the outbox row was not marked `PUBLISHED`.

Consumers must therefore be idempotent, usually by storing processed `eventId` values. 

---

## Metrics

The project exposes Actuator metrics and Prometheus format endpoints:

```text
/actuator/health
/actuator/metrics
/actuator/prometheus
```

Custom metrics:

| Metric                   | Type    | Tag      | Values                                                 | Purpose                                   |
| ------------------------ | ------- | -------- | ------------------------------------------------------ | ----------------------------------------- |
| `outbox.publish{result}` | Counter | `result` | `success`, `failed`                                    | Outbox publish success or failure         |
| `outbox.events{status}`  | Gauge   | `status` | `pending`, `processing`, `published`, `failed`, `dead` | Current number of outbox events by status |

`outbox.publish{result}` is a counter. It increases when the publisher records a successful or failed publish result.

`outbox.events{status}` is a gauge. It reports the current number of outbox rows in each state.

---

## How to run locally

Prerequisites:

```text
Docker
Docker Compose
```

Start the system:

```powershell
git clone https://github.com/shirinjamshidiyan/backend-patterns.git
cd backend-patterns/transactional-outbox
Copy-Item .env.example .env
docker compose up --build -d
```
and for reset everything:

```powershell
docker compose down -v
docker compose up --build -d
```

Open Kafka UI:  http://localhost:8085

Check service health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```
Create an order:

```powershell
Invoke-RestMethod `
  -Uri http://localhost:8080/orders `
  -Method POST `
  -ContentType "application/json" `
  -Headers @{"X-Correlation-Id"="aaaaaaaa-1111-2222-3333-aaaaaaaaaaaa"} `
  -Body '{"requestId":"10000000-0000-0000-0000-000000000001","customerId":"30000000-0000-0000-0000-000000000001","totalAmount":50.00}'
```

Or run the included HTTP file:

```text
http/create-order.http
```

---

## Expected result

After creating one order (or running http/create-order.http file) and waiting for the publisher to run:

```text
orders table: one order row
outbox_events table: one PUBLISHED row
Kafka topic order.created: one OrderCreated message
outbox.publish{result="success"}: 1
outbox.events{status="published"}: 1
```

Check metrics:

```powershell
Invoke-RestMethod "http://localhost:8080/actuator/metrics/outbox.publish?tag=result:success"
Invoke-RestMethod "http://localhost:8080/actuator/metrics/outbox.events?tag=status:published"
```

---

## Summary

This project shows how to write business data and an outgoing Kafka event atomically without using a distributed transaction.

It focuses on the producer side of Transactional Outbox: saving the event, claiming it safely, publishing it to Kafka, protecting claimed rows with owner based updates, retrying failed publishes, recovering stale processing rows, and exposing operational metrics.

The implementation is intentionally small, but the important failure cases are kept visible.
