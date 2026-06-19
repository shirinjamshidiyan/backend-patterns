
# Backend Patterns

This repository contains small, focused examples of backend engineering patterns and communication techniques.

Each folder shows one important backend or distributed systems concept in a small and practical way. The goal is to make each pattern easy to understand, run, inspect, and explain without the noise of a large application.

## Projects

| Project                                              | Focus                                                                                                               | Key Concepts                                                    |
| ---------------------------------------------------- |---------------------------------------------------------------------------------------------------------------------| --------------------------------------------------------------- |
| [Transactional Outbox](./transactional-outbox)       | Stores business data and outgoing Kafka events atomically, then publishes them through a scheduled outbox publisher | Transactional Outbox, Kafka, eventual consistency, retry        |
| [gRPC Unary Service Call](./grpc-unary-service-call) | Shows synchronous service to service communication using one gRPC request and one response                          | gRPC, Protocol Buffers, blocking stub, deadline, status mapping |
