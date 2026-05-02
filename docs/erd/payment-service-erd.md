# Payment Service – ERD Dijagram

**Baza podataka:** `paymentdb`

```mermaid
erDiagram
    wallet {
        BIGINT id PK
        BIGINT user_id UK
        DECIMAL balance
        VARCHAR currency
        DATETIME created_at
        DATETIME updated_at
    }

    payment {
        BIGINT id PK
        BIGINT reservation_id
        BIGINT guest_id
        DECIMAL amount
        VARCHAR currency
        ENUM status
        VARCHAR payment_method
        DATETIME processed_at
        BIGINT related_payment_id FK
    }

    wallet_transaction {
        BIGINT id PK
        BIGINT wallet_id FK
        DECIMAL amount
        ENUM transaction_type
        VARCHAR description
        BIGINT payment_id FK
        DATETIME created_at
    }

    wallet ||--o{ wallet_transaction : "ima transakcije"
    payment ||--o{ wallet_transaction : "povezana transakcija"
    payment }o--o| payment : "povezano plaćanje (povrat)"
```

## Entiteti

| Entitet | Opis |
|---|---|
| `wallet` | Virtualni novčanik korisnika |
| `payment` | Plaćanje rezervacije (PENDING/COMPLETED/FAILED/REFUNDED) |
| `wallet_transaction` | Transakcija na novčaniku (DEPOSIT/WITHDRAWAL/PAYMENT/PAYOUT/REFUND) |
