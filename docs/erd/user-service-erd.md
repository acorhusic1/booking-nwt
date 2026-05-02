# User Service – ERD Dijagram

**Baza podataka:** `userdb`

```mermaid
erDiagram
    users {
        BIGINT id PK
        VARCHAR email UK
        VARCHAR password_hash
        VARCHAR first_name
        VARCHAR last_name
        VARCHAR phone
        ENUM role
        BOOLEAN is_active
        DATETIME last_login
        DATETIME created_at
        DATETIME updated_at
    }

    identity_verification {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR document_type
        VARCHAR document_number
        ENUM status
        DATETIME submitted_at
        DATETIME verified_at
        BIGINT verified_by
    }

    user_preference {
        BIGINT id PK
        BIGINT user_id FK
        VARCHAR preferred_language
        VARCHAR property_type
        DECIMAL min_price
        DECIMAL max_price
        DATETIME updated_at
    }

    users ||--o{ identity_verification : "ima verifikacije"
    users ||--o| user_preference : "ima preferencije"
```

## Entiteti

| Entitet | Opis |
|---|---|
| `users` | Registrovani korisnici (GUEST, HOST, ADMIN) |
| `identity_verification` | Dokumenti za verifikaciju domaćina (PENDING/APPROVED/REJECTED) |
| `user_preference` | Korisničke preferencije za pretrage (jezik, tip smještaja, raspon cijena) |
