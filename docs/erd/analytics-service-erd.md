# Analytics Service – ERD Dijagram

**Baza podataka:** `analyticsdb`

```mermaid
erDiagram
    property_statistics {
        BIGINT id PK
        BIGINT property_id
        BIGINT host_id
        INT year
        INT month
        INT total_reservations
        DECIMAL total_revenue
        DECIMAL average_rating
        DECIMAL occupancy_rate
        INT view_count
        INT cancellation_count
        DATETIME created_at
        DATETIME updated_at
    }

    revenue_report {
        BIGINT id PK
        BIGINT host_id
        INT year
        INT month
        DECIMAL total_revenue
        DECIMAL platform_commission
        DECIMAL net_revenue
        INT total_reservations
        INT total_cancellations
        INT total_properties
        DECIMAL average_occupancy_rate
        DATETIME created_at
        DATETIME updated_at
    }
```

## Entiteti

| Entitet | Opis |
|---|---|
| `property_statistics` | Mjesečne statistike po nekretnini (broj rezervacija, prihod, popunjenost, ocjena) |
| `revenue_report` | Mjesečni finansijski izvještaj domaćina (ukupan prihod, provizija platforme, neto prihod) |
