# Reservation Service – ERD Dijagram

**Baza podataka:** `reservationdb`

```mermaid
erDiagram
    reservation {
        BIGINT id PK
        BIGINT guest_id
        BIGINT host_id
        BIGINT property_id
        DATE check_in
        DATE check_out
        INT num_guests
        DECIMAL total_price
        ENUM status
        BIGINT cancellation_policy_id FK
        BIGINT promo_code_id FK
        DATETIME created_at
        DATETIME updated_at
    }

    cancellation_policy {
        BIGINT id PK
        BIGINT property_id
        VARCHAR name
        INT free_cancel_days
        INT partial_refund_pct
        BOOLEAN no_refund
        DATETIME created_at
    }

    promo_code {
        BIGINT id PK
        VARCHAR code UK
        VARCHAR description
        ENUM discount_type
        DECIMAL discount_value
        INT min_nights
        DATE valid_from
        DATE valid_to
        INT max_uses
        INT usage_count
        BIGINT created_by
        DATETIME created_at
    }

    problem_report {
        BIGINT id PK
        BIGINT reservation_id FK
        BIGINT reporter_id
        VARCHAR category
        TEXT description
        ENUM status
        DATETIME reported_at
        DATETIME resolved_at
    }

    reservation }o--|| cancellation_policy : "primjenjuje politiku"
    reservation }o--o| promo_code : "koristi promo kod"
    reservation ||--o{ problem_report : "ima prijave problema"
```

## Entiteti

| Entitet | Opis |
|---|---|
| `reservation` | Rezervacija smještaja (CREATED/CONFIRMED/COMPLETED/CANCELLED) |
| `cancellation_policy` | Politika otkazivanja (Fleksibilna/Umjerena/Stroga) |
| `promo_code` | Promotivni kodovi sa popustom (PERCENTAGE/FIXED) |
| `problem_report` | Prijave problema tokom boravka (REPORTED/IN_PROGRESS/RESOLVED) |
