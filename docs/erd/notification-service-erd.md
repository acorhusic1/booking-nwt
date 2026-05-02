# Notification Service – ERD Dijagram

**Baza podataka:** `notificationdb`

```mermaid
erDiagram
    notification {
        BIGINT id PK
        BIGINT user_id
        VARCHAR type
        VARCHAR title
        TEXT body
        BOOLEAN is_read
        BIGINT related_reservation_id
        DATETIME created_at
        DATETIME read_at
    }

    conversation {
        BIGINT id PK
        BIGINT guest_id
        BIGINT host_id
        BIGINT property_id
        BIGINT reservation_id
        DATETIME created_at
        DATETIME updated_at
    }

    message {
        BIGINT id PK
        BIGINT conversation_id FK
        BIGINT sender_id
        TEXT content
        BOOLEAN is_read
        DATETIME sent_at
        DATETIME read_at
    }

    conversation ||--o{ message : "sadrži poruke"
```

## Entiteti

| Entitet | Opis |
|---|---|
| `notification` | Push/in-app notifikacija korisniku (NOVA_REZERVACIJA, POTVRDA, PODSJETNIK...) |
| `conversation` | Konverzacija između gosta i domaćina vezana za nekretninu/rezervaciju |
| `message` | Pojedinačna poruka unutar konverzacije |
