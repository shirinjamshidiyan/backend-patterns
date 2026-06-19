<div align="center">

# gRPC Unary Service Call

</div>

This project contains two Spring Boot services that shows synchronous service to service communication using a unary gRPC call (One request → One response)

The Ride Service acts as the gRPC client. It receives passenger coordinates through an HTTP endpoint and sends them to the Driver Service using the `FindDriver` RPC.

The Driver Service acts as the gRPC server. It searches an in-memory repository, finds the nearest available driver, and returns that driver in a single gRPC response.

## Architecture

```text
HTTP Client
    |
    | POST /api/rides/find-driver
    v
Ride Service
    |
    | gRPC unary call
    | FindDriver(request)
    v
Driver Service
    |
    | Find nearest available driver
    v
In-Memory Driver Repository
```

## What This Project Focuses On

* A shared Protocol Buffers contract
* Java code generation from a `.proto` file
* A gRPC server implemented by the Driver Service
* A blocking gRPC client stub used by the Ride Service
* A deadline for the gRPC call
* Manual validation of protobuf request fields
* gRPC status based error handling
* Mapping gRPC errors to HTTP responses
* Running both services with Docker Compose

## gRPC and Protocol Buffers

gRPC is a framework for communication between services. It uses HTTP/2 for transport and commonly uses Protocol Buffers to define and serialize messages.

Both services use the same `.proto` contract, so they agree on the request and response structure. The Maven plugin generates the required Java classes from this contract, which reduces manual code and helps prevent incompatible message definitions.

The `.proto` file defines:

* Request and response messages
* The `DriverService` service
* The `FindDriver` RPC method
* Shared fields and enum values

```proto
service DriverService {
  rpc FindDriver(FindDriverRequest)
      returns (FindDriverResponse);
}
```

The generated code includes:

* Java request and response classes
* A server base class
* Blocking and asynchronous client stubs

The Driver Service (gRPC server) implements the generated server base class and handles the `FindDriver` RPC.

The Ride Service (gRPC client) creates a blocking stub and calls the remote method:

```java
stub.findDriver(request);
```

The stub handles message serialization, network communication, response deserialization, and gRPC status propagation.

## REST and gRPC

| REST                                      | gRPC                                          |
| ----------------------------------------- | --------------------------------------------- |
| Commonly uses JSON                        | Commonly uses Protocol Buffers                |
| Uses resource oriented endpoints          | Uses service and method contracts             |
| Easy to call from browsers                | Well suited to communication between services |
| The contract may be documented separately | The contract is defined in a `.proto` file    |
| Usually uses larger text payloads         | Uses compact binary messages                  |

This project uses:

* REST between the external HTTP client and the Ride Service
* gRPC between the Ride Service and the Driver Service

gRPC works well for internal service communication because both services share the same contract and Java code can be generated automatically. This makes communication clearer and reduces manual serialization code.

## Deadline

The Ride Service applies a deadline to the gRPC call:

```java
stub
    .withDeadlineAfter(2, TimeUnit.SECONDS)
    .findDriver(request);
```

The deadline limits how long the Ride Service waits for a response.

If the deadline expires, the RPC ends with:

```text
DEADLINE_EXCEEDED
```

## Validation and Error Handling

The Driver Service validates the latitude and longitude before searching for a driver.

The project handles the following statuses:

| Situation                  | gRPC status         | HTTP response |
| -------------------------- | ------------------- | ------------: |
| Invalid coordinates        | `INVALID_ARGUMENT`  |           400 |
| No available driver        | `NOT_FOUND`         |           404 |
| Deadline exceeded          | `DEADLINE_EXCEEDED` |           504 |
| Driver Service unavailable | `UNAVAILABLE`       |           503 |
| Unexpected error           | `INTERNAL`          |           500 |

The Ride Service catches gRPC errors and maps them to HTTP responses at the REST boundary.

Example:

```json
{
  "grpcStatus": "INVALID_ARGUMENT",
  "message": "Latitude must be between -90 and 90"
}
```

## How to Run Locally

Prerequisites

```text
Docker
Docker Compose
```

Start the system:

```powershell
git clone https://github.com/shirinjamshidiyan/backend-patterns.git
cd backend-patterns/grpc-unary-service-call
Copy-Item .env.example .env
docker compose up --build
```

The services will be available at:

* Ride Service HTTP API: `http://localhost:8082`
* Driver Service gRPC API: `localhost:9091`

### Test the Application

Run the request in:

```text
http/test.http
```

The request is sent to the Ride Service through HTTP. The Ride Service then calls the Driver Service using the unary `FindDriver` RPC.

Example response:

```json
{
  "id": 3,
  "name": "D3",
  "status": "DRIVER_STATUS_AVAILABLE",
  "latitude": 55.71,
  "longitude": 12.58
}
```

Stop the services:

```powershell
docker compose down
```

## Project Structure

```text
grpc-unary-service-call/
├── grpc-contracts/
│   └── Shared protobuf contract
├── driver-service/
│   └── gRPC server and in-memory driver repository
├── ride-service/
│   └── HTTP API and gRPC client
├── http/
│   └── Example HTTP request
├── docker-compose.yml
└── pom.xml
```

## Out of Scope

To keep the project focused on unary gRPC communication, it does not include:

* gRPC streaming
* TLS or mutual TLS
* Client and server interceptors
* Production grade geographic distance calculation
