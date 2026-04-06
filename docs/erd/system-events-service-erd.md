# System Events Service – ERD Dijagram

**Baza podataka:** `systemeventsdb`

```mermaid
erDiagram
    audit_log {
        BIGINT id PK
        BIGINT user_id
        VARCHAR action
        VARCHAR entity_type
        BIGINT entity_id
        TEXT details
        VARCHAR ip_address
        DATETIME created_at
    }
```

## Entiteti

| Entitet | Opis |
|---|---|
| `audit_log` | Audit trail svih sistemskih akcija (CREATE/UPDATE/DELETE) nad entitetima (PROPERTY, RESERVATION, PAYMENT, REVIEW...) |
