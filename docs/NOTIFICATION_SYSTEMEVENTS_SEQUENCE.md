# UML Sequence Diagram: Notification → System Events (Audit Log) Flow

## Overview

When a notification is created in `notification-service`, it synchronously calls `system-events-service` via OpenFeign to record an audit log entry. A Resilience4j Circuit Breaker protects this call — if `system-events-service` is unavailable, the fallback silently absorbs the error so notification creation still succeeds.

---

## Sequence Diagram

```
Client                  API Gateway             notification-service         system-events-service        MySQL (notificationdb)   MySQL (systemeventsdb)
  |                         |                           |                              |                            |                        |
  |  POST /api/notifications|                           |                              |                            |                        |
  |  Authorization: Bearer  |                           |                              |                            |                        |
  |------------------------>|                           |                              |                            |                        |
  |                         |  [JWT Validation]         |                              |                            |                        |
  |                         |  Valid? Yes               |                              |                            |                        |
  |                         |  Forward + X-User-Email   |                              |                            |                        |
  |                         |-------------------------->|                              |                            |                        |
  |                         |                           |  [JwtAuthenticationFilter]   |                            |                        |
  |                         |                           |  Authenticate request        |                            |                        |
  |                         |                           |                              |                            |                        |
  |                         |                           |  notificationRepository      |                            |                        |
  |                         |                           |  .save(notification)-------->|                            |                        |
  |                         |                           |                              |                            |  INSERT INTO           |
  |                         |                           |                              |                            |  notifications         |
  |                         |                           |                              |                            |----------------------->|
  |                         |                           |                              |                            |  OK (saved)            |
  |                         |                           |<----------------------------------------------------|                        |
  |                         |                           |                              |                            |                        |
  |                         |                           |  [AuditLogger.log()]         |                            |                        |
  |                         |                           |  via AuditLogClient (Feign)  |                            |                        |
  |                         |                           |  Circuit Breaker: CLOSED     |                            |                        |
  |                         |                           |  POST /api/audit-logs        |                            |                        |
  |                         |                           |  Authorization: Bearer <jwt> |                            |                        |
  |                         |                           |----------------------------->|                            |                        |
  |                         |                           |                              |  auditLogRepository        |                        |
  |                         |                           |                              |  .save(auditLog)---------->|                        |
  |                         |                           |                              |                            |                        INSERT INTO
  |                         |                           |                              |                            |                        audit_logs
  |                         |                           |                              |                            |                        |------->|
  |                         |                           |                              |                            |                        OK      |
  |                         |                           |                              |<-------------------------------------------|        |
  |                         |                           |  201 Created (AuditLog)      |                            |                        |
  |                         |                           |<-----------------------------|                            |                        |
  |                         |                           |                              |                            |                        |
  |                         |  201 Created              |                              |                            |                        |
  |                         |  (NotificationResponseDTO)|                              |                            |                        |
  |<------------------------|---------------------------|                              |                            |                        |
```

---

## Failure Branch: system-events-service is DOWN

```
Client                  API Gateway             notification-service         system-events-service
  |                         |                           |                              |
  |  POST /api/notifications|                           |                              |
  |------------------------>|-------------------------->|                              |
  |                         |                           |  notificationRepository      |
  |                         |                           |  .save(notification) → OK    |
  |                         |                           |                              |
  |                         |                           |  AuditLogClient (Feign)      |
  |                         |                           |  Circuit Breaker: CLOSED     |
  |                         |                           |  POST /api/audit-logs        |
  |                         |                           |----------------------------->|
  |                         |                           |                              |  [SERVICE DOWN]
  |                         |                           |  Connection timeout / 5xx    |
  |                         |                           |<-----------------------------|
  |                         |                           |                              |
  |                         |                           |  [Resilience4j CB opens]     |
  |                         |                           |  AuditLogClientFallback      |
  |                         |                           |  .logEvent() → returns null  |
  |                         |                           |  (error absorbed silently)   |
  |                         |                           |                              |
  |                         |  201 Created              |                              |
  |                         |  (Notification still OK)  |                              |
  |<------------------------|---------------------------|                              |
```

**Key point**: The notification is saved successfully regardless of whether the audit log call succeeds. The Circuit Breaker prevents cascading failures — after enough failures, it opens and subsequent calls go directly to the fallback without attempting the network call, until the half-open state allows retries.

---

## Circuit Breaker State Machine

```
         [Failure rate > 50% over 10 calls]
CLOSED -----------------------------------------> OPEN
  ^                                                  |
  |                                    [Wait 10s]    |
  |                                                  v
  |          [3 probe calls succeed]            HALF-OPEN
  +<--------------------------------------------|
                                    [probe calls fail]
                                         |
                                         v
                                       OPEN (reset timer)
```

---

## Components Involved

| Component | Role |
|---|---|
| `NotificationController` | Receives POST /api/notifications, delegates to service |
| `NotificationServiceImpl` | Saves notification, calls AuditLogger |
| `AuditLogger` | Wrapper that calls AuditLogClient with circuit breaker protection |
| `AuditLogClient` | Feign client — declares `POST /api/audit-logs` call to `system-events-service` |
| `AuditLogClientFallback` | Resilience4j fallback — absorbs errors when circuit is open |
| `FeignClientInterceptor` | Propagates JWT `Authorization` header to downstream Feign calls |
| `AuditLogController` | Receives the audit log creation request in `system-events-service` |
| `AuditLogServiceImpl` | Persists the audit log entry |
