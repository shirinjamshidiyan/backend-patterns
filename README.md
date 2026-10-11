# Backend Patterns

This repository contains small, focused examples of backend engineering patterns and communication techniques.

Each project shows one important backend or distributed systems concept in a small, practical way. The goal is to make each pattern easy to understand, run, inspect, and explain without the noise of a large application.

## Projects

| Project | Focus | Key Concepts                                                                                     |
|---|---|--------------------------------------------------------------------------------------------------|
| [Transactional Outbox](./transactional-outbox) | Stores business data and outgoing Kafka events atomically, then publishes them through a scheduled outbox publisher | Transactional Outbox, Kafka, eventual consistency, retry                                         |
| [gRPC Unary Service Call](./grpc-unary-service-call) | Shows synchronous service-to-service communication using one gRPC request and one response | gRPC, Protocol Buffers, blocking stub, deadline, status mapping                                  |
| [gRPC Server Streaming](./grpc-server-streaming) | Shows one request producing multiple streamed responses from the server | gRPC, Protocol Buffers, async stub, server-controlled completion, client-controlled cancellation |
| [Eureka Service Discovery](./service-discovery-eureka) | Shows service registration, discovery, and client-side load balancing across multiple service instances | Netflix Eureka, Spring Cloud LoadBalancer, RestClient, service discovery, registration           |
| [Retry Demo](./retry-demo) | Shows how a service handles transient failures when calling another service | Resilience4j Retry, Spring AOP, RestClient, timeouts, 503 responses, exponential backoff, jitter |
| [Gateway Rate Limiter Demo](./gateway-ratelimiter-demo) | Shows API gateway routing, filtering, request tracing, service discovery integration, and distributed rate limiting | Spring Cloud Gateway, WebFlux, Eureka, Spring Cloud LoadBalancer, global/default/custom filters, correlation ID, OpenFeign, Redis RequestRateLimiter, Actuator|



## Tech Stack

Technologies used across the repository include:

- Java 21
- Spring Boot
- Spring Cloud
- Spring Cloud Gateway
- Netflix Eureka
- Spring Cloud OpenFeign
- Spring Cloud LoadBalancer
- Resilience4j
- Apache Kafka
- gRPC and Protocol Buffers
- Redis
- Maven
- Docker and Docker Compose

  Individual projects use only the technologies required for the pattern they demonstrate.

## Running a Project

Each project has its own README with architecture notes, configuration details, and run/test instructions.

Projects can be started either directly with Maven or through Docker Compose, depending on the required infrastructure.
