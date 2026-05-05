# Security Architecture Report

**Authors**: Ahmed (User/Analytics), Kenan (Gateway), Benjamin (Reservations), Emir (Properties)

## 1. Overview
The BookingNWT system employs a **Stateless JWT-based security architecture** designed for high scalability and decentralized authorization. The architecture ensures that user credentials are never exposed beyond the authentication boundary and that each microservice remains responsible for its own access control.

## 2. Authentication Flow
Authentication is centralized in the `user-service`, which acts as the **Identity Provider (IdP)**.
1.  **Login**: Users provide credentials to `/api/auth/login`.
2.  **Token Generation**: Upon successful authentication, `user-service` generates a signed JWT containing:
    *   `sub`: User Email
    *   `roles`: List of user roles (e.g., `ROLE_GUEST`, `ROLE_HOST`, `ROLE_ADMIN`)
    *   `iat`, `exp`: Issued-at and Expiration timestamps.
3.  **Token Format**: Standard Bearer token in the `Authorization` header.

## 3. API Gateway Role
The `api-gateway` acts as the first line of defense:
*   **Integrity Check**: Verifies the JWT signature using the shared secret.
*   **Routing**: Routes requests only if the token is valid (or if the endpoint is public).
*   **Statelessness**: The gateway does not store sessions; it relies entirely on the incoming token.

## 4. Decentralized Authorization
We have implemented **Decentralized Authorization** across all services. 
*   Each microservice (e.g., `user-service`, `analytics-service`) implements its own Spring Security filter chain.
*   **@PreAuthorize**: Methods are protected using role-based expressions.
    *   `hasRole('ADMIN')`: For administrative tasks like deleting users or reports.
    *   `hasRole('HOST')`: For property management and revenue viewing.
*   **Benefit**: This prevents the Gateway from becoming a bottleneck and allows services to evolve their security rules independently.

## 5. Inter-Service Security (Token Propagation)
When one service calls another synchronously (via **OpenFeign**), the security context must be preserved:
*   **Feign Interceptor**: We implemented a `RequestInterceptor` that extracts the JWT from the current request's `SecurityContext` and injects it into the downstream request.
*   This ensures that the `user-service` still knows who the "original" caller is when `analytics-service` asks for host details.

## 6. Security Questions & Considerations
*   **Mobile Access**: JWTs are natively supported by mobile platforms (iOS/Android) via standard HTTP headers.
*   **Logout**: Handled by token deletion on the client side. For high-security revoking, a short TTL (1 hour) is used.
*   **Passwords**: All passwords are encrypted using the **BCrypt** hashing algorithm before storage.

## 7. Reservation & Payment Services (Benjamin)
Both services implement the same stateless JWT pattern as the rest of the system, with three concrete pieces:

* **`JwtTokenProvider`** parses the bearer token using `io.jsonwebtoken` and the shared `security.jwt.secret` (read from Config Server / env). The `roles` claim is mapped to `SimpleGrantedAuthority` with the `ROLE_` prefix added if missing, so `@PreAuthorize("hasRole('ADMIN')")` works directly.
* **`JwtAuthenticationFilter`** (a `OncePerRequestFilter`) reads the `Authorization: Bearer …` header, validates the token, and populates the `SecurityContext`. Invalid tokens silently clear the context — the request then hits the `authenticated()` rule and returns 401 / 403 through the `GlobalExceptionHandler`.
* **`SecurityConfig`** disables CSRF, sets `SessionCreationPolicy.STATELESS`, permits `/actuator/**` and Swagger paths, requires authentication for everything else, and enables `@EnableMethodSecurity` so per-endpoint role rules are enforced.

### Per-endpoint authorization matrix (decentralized)
| Endpoint                                        | Allowed roles            |
|-------------------------------------------------|--------------------------|
| `POST /api/reservations`                        | `USER`, `ADMIN`, `HOST`  |
| `GET /api/reservations` (list all)              | `ADMIN`                  |
| `PATCH /api/reservations/{id}`                  | `USER`, `ADMIN`, `HOST`  |
| `PUT /api/reservations/{id}/status`             | `HOST`, `ADMIN`          |
| `GET /api/reservations/host/{id}/revenue`       | `HOST`, `ADMIN`          |
| `POST /api/reservations/batch`                  | `ADMIN`                  |
| `POST /api/payments`                            | `USER`, `ADMIN`          |
| `PATCH /api/payments/{id}`                      | `ADMIN`                  |
| `PUT /api/payments/{id}/status`                 | `ADMIN`                  |
| `POST /api/payments/{id}/refund`                | `USER`, `ADMIN`          |
| `GET /api/payments/guest/{id}/total-spent`      | `USER`, `ADMIN`          |
| `POST /api/payments/batch`                      | `ADMIN`                  |

### Service-to-service security
The `payment-service` calls `reservation-service` over Feign (`@FeignClient(name="reservation-service")`). The team's shared Feign `RequestInterceptor` propagates the original caller's JWT, so the downstream `@PreAuthorize("hasAnyRole('HOST','ADMIN')")` on `PUT /api/reservations/{id}/status` still applies — the payment-service does not run as a privileged identity.

### Failure handling
`AccessDeniedException` and `AuthenticationException` are mapped by both services' `GlobalExceptionHandler` to standardized 403 / 401 JSON responses, keeping the error contract consistent with the rest of the system.

---
*This document serves as the technical documentation for Task 7 requirements.*
