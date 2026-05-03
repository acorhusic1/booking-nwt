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

---
*This document serves as the technical documentation for Task 7 requirements.*
