# Task 2 — gRPC Ping/Pong server

Spring Boot gRPC server for Task 2. It exposes one RPC on port **8080**.

## Shared contract

Both server and client compile stubs from:

`task2/proto/ping_pong.proto`

## Run

```bash
mvn spring-boot:run
```
The server listens for gRPC on **localhost:8080**.
