# Task 5 — Payment ↔ Reservation Sync Flow (Benjamin)

## Non-trivial communication

When a payment status changes, the **payment-service** synchronously notifies the **reservation-service** so the reservation's lifecycle stays consistent with the financial outcome:

| Payment status change | Reservation effect    |
|-----------------------|-----------------------|
| `PENDING → COMPLETED` | `CREATED → CONFIRMED` |
| `PENDING → FAILED`    | `CREATED → CANCELLED` |
| `COMPLETED → REFUNDED` (via `refundPayment`) | `CONFIRMED → CANCELLED` |

This is non-trivial because the resulting state depends on **two databases** (`paymentdb` writes + `reservationdb` writes) and on the **availability** of the downstream service (handled via Resilience4j).

## Components
- `PaymentServiceImpl.updatePaymentStatus(...)` / `refundPayment(...)`
- `ReservationStatusGateway` — `@CircuitBreaker(name="reservation-service")` with a logging fallback
- `ReservationClient` — `@FeignClient(name="reservation-service")` (Eureka-resolved, no hardcoded host/port)
- `ReservationClientFallback` — bean returned when the breaker is OPEN
- `reservation-service` `PUT /api/reservations/{id}/status?status=…`

## Sequence diagram (Mermaid)

```mermaid
sequenceDiagram
    autonumber
    actor C as Client (HOST/ADMIN)
    participant GW as API Gateway
    participant PS as payment-service
    participant DB1 as paymentdb
    participant CB as Resilience4j CB
    participant EU as Eureka
    participant FC as ReservationClient (Feign)
    participant RS as reservation-service
    participant DB2 as reservationdb

    C->>GW: PUT /api/payments/{id}/status?status=COMPLETED + JWT
    GW->>PS: route (JWT forwarded)
    PS->>PS: @PreAuthorize hasRole('ADMIN')
    PS->>DB1: SELECT payment by id
    DB1-->>PS: Payment(PENDING)
    PS->>DB1: UPDATE status=COMPLETED, processedAt=now
    DB1-->>PS: ok

    PS->>CB: gateway.updateStatus(reservationId, "CONFIRMED")
    alt Circuit CLOSED
        CB->>FC: invoke
        FC->>EU: resolve "reservation-service"
        EU-->>FC: instance(host:port)
        FC->>RS: PUT /api/reservations/{id}/status?status=CONFIRMED + JWT
        RS->>RS: @PreAuthorize hasAnyRole('HOST','ADMIN')
        RS->>DB2: UPDATE reservation status=CONFIRMED, updatedAt=now
        DB2-->>RS: ok
        RS-->>FC: 200 OK
        FC-->>CB: success
        CB-->>PS: success
    else Circuit OPEN / call fails
        CB->>CB: fallback(reservationId, status, throwable)
        Note over CB: log warn — payment commit stands;<br/>reconciliation job catches up later
        CB-->>PS: void (silent)
    end

    PS-->>GW: 200 OK PaymentResponseDTO(COMPLETED)
    GW-->>C: 200 OK
```

## Failure handling rationale
- **Network blip / cold start**: the Resilience4j breaker stays CLOSED, the call retries on next event; system remains active.
- **`reservation-service` is down**: after 5 calls with ≥50% failure rate the breaker opens; subsequent payment status changes still **commit on the payment side** and the fallback only logs. A scheduled reconciliation (out of Task 5 scope, planned for the async-messaging task) will re-emit any missed status events.
- **Loose coupling**: the synchronous call is one-directional and the failure path swallows; payment processing never blocks on reservation availability. This is the pattern recommended in *Microservices From Design to Deployment* §4 — degrade gracefully, push consistency to a follow-up async event.
