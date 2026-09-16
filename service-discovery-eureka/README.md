# Eureka Service Discovery Demo

A small Spring Boot demo showing **service registration, discovery, and client-side load balancing with Eureka**.

## Services

This demo has three Spring Boot applications:

- **eureka-server** — provides the service registry used for service discovery.
- **order-service** — calls the Inventory Service through its Eureka service ID.
- **inventory-service** — exposes simple GET and POST endpoints and is started as two instances.


## What this demo shows

Docker Compose runs two Inventory replicas:

```text
inventory-1
inventory-2
```
Both Inventory instances register in Eureka under the same service ID:

```text
inventory-service
```

`order-service` calls that logical service name instead of a fixed host and port:

```text
http://inventory-service/api/v1/inventory
```

Spring Cloud LoadBalancer then resolves that service name through Eureka and selects an available instance.

With the default round-robin load balancer, repeated requests are distributed across the two Inventory instances:

```text
inventory-1
inventory-2
inventory-1
inventory-2
```

The API response includes the processing instance ID, so the selected replica is visible during testing.

## Running the demo

Build and start everything:

```bash
docker compose up --build
```

Eureka dashboard:

```text
http://localhost:8761
```

Order Service:

```text
http://localhost:8080
```

Inventory replicas can also be called directly:

```text
http://localhost:8081
http://localhost:8082
```

## Testing

Use the requests in the project HTTP test file.

Run the same GET or POST request several times through Order Service and check the instance ID in the response. The value should alternate between `inventory-1` and `inventory-2`.

This confirms the request flow:

```text
Order Service
      |
      v
Spring Cloud LoadBalancer
      |
      v
Eureka Registry
   /       \
inventory-1 inventory-2
```

## RestClient configuration

Order Service has two `RestClient.Builder` beans:

- a normal `@Primary` builder
- a `@LoadBalanced` builder used by `InventoryClient`

The separate normal builder is needed because Eureka itself also uses `RestClient`. If the only available builder is load-balanced, a URL such as `http://localhost:8761/eureka/` can be treated as a service-discovery request for a service named `localhost`.

`InventoryClient` therefore injects the load-balanced builder explicitly:

```java
public InventoryClient(@LoadBalanced RestClient.Builder builder, ...) {
    ...
}
```

## Technologies
- Java 21
- Spring Boot 4.0.7
- Spring Cloud 2025.1.3
- Netflix Eureka
- Spring Cloud LoadBalancer
- RestClient
- Maven
- Docker Compose

## Notes

Several Eureka settings are written explicitly in the YAML files even when they match default or common values. They are kept there as reference so the important registration, registry refresh, heartbeat, lease, cache, and eviction settings are easy to see and change during later experiments.
