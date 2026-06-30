<div align="center">

# gRPC Server Streaming

</div>

This project contains two Spring Boot services that demonstrate service-to-service communication using gRPC server streaming:

```text
One request → Multiple responses
```

The Ride Service acts as the gRPC client. It receives passenger coordinates through HTTP, starts a gRPC stream, and forwards the received driver location updates to the HTTP client using Server-Sent Events (SSE).

The Driver Service acts as the gRPC server. It reads available drivers from an in-memory repository and sends location updates periodically.

This project builds on the basic gRPC setup used in the [gRPC Unary Service Call](../grpc-unary-service-call/README.md) project. The focus here is the streaming lifecycle, especially normal completion and client cancellation.

## Architecture

```text
HTTP Client
    ^
    | Server-Sent Events
    |
Ride Service
    ^
    | gRPC server stream
    |
Driver Service
    |
    | Periodic driver location updates
    v
In-Memory Driver Repository
```

The external client communicates with Ride Service over HTTP.
Ride Service communicates with Driver Service through gRPC and converts the received protobuf messages into SSE events.

The project supports two streaming modes: Limited Stream and Unlimited Stream

### Limited Stream

The client specifies how many updates it wants to receive. For example, when the client requests five updates:

```text
Client sends one request
        |
        v
Server sends update 1
Server sends update 2
Server sends update 3
Server sends update 4
Server sends update 5
        |
        v
Server calls onCompleted()
```

The server controls the normal end of the stream. The client can still cancel the RPC before all requested updates have been received.

### Unlimited Stream

The client starts the stream without specifying an update count.
Driver Service continues generating updates until the client cancels the active gRPC call:

```text
Client sends one request
        |
        v
Server sends update 1
Server sends update 2
Server sends update 3
...
        |
        v
Client cancels the RPC
```

In this case, the client controls the lifetime of the stream.

Cancellation is propagated to Driver Service, but gRPC does not stop application code automatically. Driver Service must explicitly cancel the scheduled task that produces the updates.

```text
Client cancels the RPC
        |
        v
Driver Service detects cancellation
        |
        v
Cancellation handler runs
        |
        v
Scheduled update task stops
```

## gRPC Contract

The shared `.proto` file defines two server-streaming RPC methods:

```proto
service DriverService {

  rpc WatchLimitedDriverLocations(DriversLimitedUpdateRequest)
    returns (stream DriverLocationUpdate);

  rpc WatchUnlimitedDriverLocations(DriversUnlimitedUpdateRequest)
    returns (stream DriverLocationUpdate);
}
```

Both methods receive one request and return multiple `DriverLocationUpdate` messages.

| RPC | Normal end of the stream |
| --- | --- |
| `WatchLimitedDriverLocations` | Driver Service completes the stream after the requested number of updates |
| `WatchUnlimitedDriverLocations` | The stream continues until the client cancels the call |

## Ride Service and SSE

Ride Service uses an **asynchronous gRPC stub** and receives updates through callback methods:

- `onNext()` receives each location update
- `onCompleted()` handles normal server completion
- `onError()` handles failures and cancellation

Each gRPC update is forwarded to the external HTTP client through SSE.

```text
gRPC onNext(update)
        |
        v
SseEmitter.send(update)
        |
        v
HTTP client receives the event
```

If the HTTP client closes the SSE connection, Ride Service cancels the active gRPC call so Driver Service does not continue producing updates that nobody is receiving.

In practice, a disconnected SSE client may be detected when Ride Service attempts the next write.

## Simulated Driver Movement

The location updates are intentionally simple.

For each message, the example slightly changes the stored coordinates:

```java
latitude + sequence * 0.0001
longitude + sequence * 0.0001
```

This is not a real driver-tracking algorithm. It only creates visible changes between updates so the streaming behaviour can be tested.

The passenger coordinates are validated, but they are not currently used to calculate proximity or select the nearest driver. The repository simply returns the available drivers.

## Validation and Error Handling

Driver Service validates the request before starting the stream.

| Situation                                     | gRPC status |
|-----------------------------------------------| --- |
| Invalid latitude or longitude or update_count | `INVALID_ARGUMENT` |
| No available drivers                          | `NOT_FOUND` |
| Failure while generating an update            | `INTERNAL` |
| Driver Service unavailable                    | `UNAVAILABLE` |
| Client cancels the stream                     | `CANCELLED` |

`CANCELLED` is expected when the client ends an active stream. For an unlimited stream, this is the normal stopping mechanism.

## HTTP Endpoints

### Limited stream

```http
GET /api/rides/driver-locations/limited
```

Example:

```http
GET http://localhost:8082/api/rides/driver-locations/limited?latitude=55.67&longitude=12.56&updates=5
Accept: text/event-stream
```

The connection closes normally after five events.

### Unlimited stream

```http
GET /api/rides/driver-locations/unlimited
```

Example:

```http
GET http://localhost:8082/api/rides/driver-locations/unlimited?latitude=55.67&longitude=12.56
Accept: text/event-stream
```

The connection remains open until the client disconnects or an error occurs.

## How to Run

Prerequisites:

```text
Docker
Docker Compose
```

Start the project:

```powershell
git clone https://github.com/shirinjamshidiyan/backend-patterns.git
cd backend-patterns/grpc-server-streaming
Copy-Item .env.example .env
docker compose up --build
```

The services will be available at:

- Ride Service HTTP API: `http://localhost:8082`
- Driver Service gRPC API: `localhost:9091`

Stop the project:

```powershell
docker compose down
```

## Testing

Example HTTP requests are available in:

```text
http/test.http
```

All tests are sent through the Ride Service HTTP API.
The request file includes:

Normal completion of a limited stream
Early cancellation of a limited stream
Client-controlled cancellation of an unlimited stream
Driver Service unavailability

The gRPC methods can also be tested directly by importing the `.proto` file into Postman.

## Project Structure

```text
grpc-server-streaming/
├── grpc-contracts/
│   └── Shared protobuf contract
├── driver-service/
│   └── gRPC server, scheduler, and in-memory driver repository
├── ride-service/
│   └── Asynchronous gRPC client and SSE endpoints
├── http/
│   └── Example HTTP and gRPC requests
├── docker-compose.yml
└── pom.xml
```

## Out of Scope

To keep the project focused on gRPC server streaming, it does not include:

- Real GPS tracking, route or distance calculation
- Persistent storage
- TLS or mutual TLS
