# Task 2 — gRPC Ping/Pong client

Spring Boot gRPC client for Task 2. It sends **one** `Ping` RPC (`id = 1`), prints the response to the console, and exits.

## Requirements

- Java 21+
- Maven 3.9+
- Server listening on **127.0.0.1:8080** (see [grpc-srv](../grpc-srv/README.md))

## Shared contract

Both client and server compile stubs from:

`task2/proto/ping_pong.proto`

## Run

Start the server first (separate terminal):

```bash
cd ../grpc-srv
mvn spring-boot:run
```

Then run the client:

```bash
mvn spring-boot:run
```
