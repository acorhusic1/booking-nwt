# Review Service – ERD Dijagram

**Baza podataka:** `reviewdb`

```mermaid
erDiagram
    review {
        BIGINT id PK
        BIGINT reservation_id UK
        BIGINT guest_id
        BIGINT property_id
        BIGINT host_id
        DECIMAL rating_cleanliness
        DECIMAL rating_location
        DECIMAL rating_communication
        DECIMAL rating_value
        DECIMAL rating_accuracy
        DECIMAL overall_rating
        TEXT comment
        TEXT host_reply
        DATETIME created_at
        DATETIME replied_at
    }
```

## Entiteti

| Entitet | Opis |
|---|---|
| `review` | Recenzija gosta nakon završene rezervacije; sadrži ocjene po kategorijama i prosječnu ocjenu; domaćin može odgovoriti |
