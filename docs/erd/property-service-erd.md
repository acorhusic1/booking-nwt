# Property Service – ERD Dijagram

**Baza podataka:** `propertydb`

```mermaid
erDiagram
    property {
        BIGINT id PK
        BIGINT host_id
        VARCHAR name
        TEXT description
        VARCHAR address
        VARCHAR city
        VARCHAR country
        DECIMAL latitude
        DECIMAL longitude
        INT max_guests
        BOOLEAN is_active
        DATETIME created_at
    }

    amenity {
        BIGINT id PK
        VARCHAR name UK
        ENUM category
    }

    property_amenity {
        BIGINT property_id FK
        BIGINT amenity_id FK
    }

    property_image {
        BIGINT id PK
        BIGINT property_id FK
        VARCHAR url
        BOOLEAN is_primary
        DATETIME uploaded_at
    }

    pricing_rule {
        BIGINT id PK
        BIGINT property_id FK
        DECIMAL base_price
        DECIMAL weekend_price
        INT min_stay_days
        INT max_stay_days
        INT long_stay_discount_pct
        INT long_stay_threshold
        DATETIME created_at
    }

    seasonal_rule {
        BIGINT id PK
        BIGINT property_id FK
        VARCHAR name
        DATE start_date
        DATE end_date
        INT price_modifier_pct
        INT min_nights
        DATETIME created_at
    }

    calendar_block {
        BIGINT id PK
        BIGINT property_id FK
        DATE start_date
        DATE end_date
        VARCHAR reason
        BIGINT created_by
    }

    wishlist {
        BIGINT id PK
        BIGINT guest_id
        VARCHAR name
        DATETIME created_at
    }

    wishlist_item {
        BIGINT id PK
        BIGINT wishlist_id FK
        BIGINT property_id
        DATETIME added_at
    }

    property ||--o{ property_image : "ima slike"
    property ||--o| pricing_rule : "ima cjenovnik"
    property ||--o{ seasonal_rule : "ima sezonska pravila"
    property ||--o{ calendar_block : "ima blokove kalendara"
    property }o--o{ amenity : "property_amenity"
    wishlist ||--o{ wishlist_item : "sadrži stavke"
```

## Entiteti

| Entitet | Opis |
|---|---|
| `property` | Nekretnina za iznajmljivanje |
| `amenity` | Sadržaj (WiFi, Parking, Bazen...) – BASIC/LUXURY/SAFETY |
| `property_amenity` | Veza many-to-many između property i amenity |
| `property_image` | Slike nekretnine |
| `pricing_rule` | Osnovna cijena, vikend cijena, popusti za duži boravak |
| `seasonal_rule` | Sezonski modifikatori cijena |
| `calendar_block` | Blokiranje datuma (renoviranje, privatna upotreba) |
| `wishlist` | Lista željenih nekretnina gosta |
| `wishlist_item` | Nekretnina u listi željenih |
