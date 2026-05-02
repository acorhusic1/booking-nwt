# Property Service - REST API Dokumentacija

## Bazni URL
```
http://localhost:8082/api
```

## Error Format
```json
{
    "timestamp": "2026-04-14T01:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Nekretnina sa ID 99 nije pronađena"
}
```

### Validacijske greške (400):
```json
{
    "timestamp": "2026-04-14T01:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": {
        "name": "Naziv je obavezan",
        "city": "Grad je obavezan"
    }
}
```

---

## 1. Property Controller (`/api/properties`)

### GET /api/properties
Dohvati sve nekretnine.

**Response:** `200 OK`

---

### GET /api/properties/{id}
Dohvati nekretninu po ID-u.

**Response:** `200 OK` | `404 Not Found`

---

### GET /api/properties/host/{hostId}
Dohvati nekretnine po host ID-u.

**Response:** `200 OK`

---

### GET /api/properties/city/{city}
Dohvati aktivne nekretnine po gradu.

**Response:** `200 OK`

---

### POST /api/properties
Kreiraj novu nekretninu.

**Request Body:**
```json
{
    "hostId": 1,
    "name": "Apartman Centar",
    "description": "Lijep apartman u centru",
    "address": "Ferhadija 1",
    "city": "Sarajevo",
    "country": "BiH",
    "latitude": 43.856,
    "longitude": 18.413,
    "maxGuests": 4
}
```

**Response:** `201 Created`
```json
{
    "id": 4,
    "hostId": 1,
    "name": "Apartman Centar",
    "description": "Lijep apartman u centru",
    "address": "Ferhadija 1",
    "city": "Sarajevo",
    "country": "BiH",
    "latitude": 43.856,
    "longitude": 18.413,
    "maxGuests": 4,
    "isActive": true,
    "createdAt": "2026-04-14T01:45:00"
}
```

**Validacijska greška (prazno tijelo):** `400 Bad Request`

---

### PUT /api/properties/{id}
Ažuriraj nekretninu.

**Request Body:** Isto kao POST.

**Response:** `200 OK` | `404 Not Found`

---

### DELETE /api/properties/{id}
Obriši nekretninu.

**Response:** `204 No Content` | `404 Not Found`

---

## 2. Amenity Controller (`/api/amenities`)

### GET /api/amenities
Dohvati sve sadržaje.

**Response:** `200 OK`
```json
[
    { "id": 1, "name": "WiFi", "category": "BASIC" },
    { "id": 2, "name": "Parking", "category": "BASIC" },
    { "id": 3, "name": "Bazen", "category": "LUXURY" }
]
```

---

### GET /api/amenities/{id}
**Response:** `200 OK` | `404 Not Found`

---

### POST /api/amenities
```json
{
    "name": "Sauna",
    "category": "LUXURY"
}
```
Kategorije: `BASIC`, `LUXURY`, `SAFETY`

**Response:** `201 Created` | `400 Bad Request`

---

## 3. Property Image Controller (`/api/properties/{propertyId}/images`)

### GET /api/properties/1/images
Dohvati slike nekretnine.

**Response:** `200 OK`
```json
[
    { "id": 1, "propertyId": 1, "url": "https://example.com/img1.jpg", "isPrimary": true, "uploadedAt": "2026-04-14T01:00:00" }
]
```

---

### POST /api/properties/1/images
```json
{
    "url": "https://example.com/nova-slika.jpg",
    "isPrimary": false
}
```
**Response:** `201 Created` | `404 Not Found` (ako nekretnina ne postoji)

---

### DELETE /api/properties/1/images/{imageId}
**Response:** `204 No Content` | `404 Not Found`

---

## 4. Pricing Rule Controller (`/api/properties/{propertyId}/pricing`)

### GET /api/properties/1/pricing
Dohvati cjenovnik nekretnine.

**Response:** `200 OK`
```json
{
    "id": 1,
    "propertyId": 1,
    "basePrice": 80.00,
    "weekendPrice": 100.00,
    "minStayDays": 1,
    "maxStayDays": 30,
    "longStayDiscountPct": 10,
    "longStayThreshold": 7,
    "createdAt": "2026-04-14T01:00:00"
}
```

---

### PUT /api/properties/1/pricing
Ažuriraj ili kreiraj cjenovnik.

```json
{
    "basePrice": 90.00,
    "weekendPrice": 120.00,
    "minStayDays": 2,
    "maxStayDays": 14,
    "longStayDiscountPct": 15,
    "longStayThreshold": 7
}
```
**Response:** `200 OK`

---

## 5. Calendar Block Controller (`/api/properties/{propertyId}/calendar-blocks`)

### GET /api/properties/1/calendar-blocks
**Response:** `200 OK`
```json
[
    { "id": 1, "propertyId": 1, "startDate": "2026-06-01", "endDate": "2026-06-15", "reason": "Renovacija", "createdBy": 1 }
]
```

---

### POST /api/properties/1/calendar-blocks
```json
{
    "startDate": "2026-07-01",
    "endDate": "2026-07-10",
    "reason": "Privatna upotreba",
    "createdBy": 1
}
```
**Response:** `201 Created`

---

### DELETE /api/properties/1/calendar-blocks/{blockId}
**Response:** `204 No Content`

---

## 6. Seasonal Rule Controller (`/api/properties/{propertyId}/seasonal-rules`)

### GET /api/properties/1/seasonal-rules
**Response:** `200 OK`
```json
[
    { "id": 1, "propertyId": 1, "name": "Ljetna sezona", "startDate": "2026-06-01", "endDate": "2026-08-31", "priceModifierPct": 30, "minNights": 3, "createdAt": "2026-04-14T01:00:00" }
]
```

---

### POST /api/properties/1/seasonal-rules
```json
{
    "name": "Zimska sezona",
    "startDate": "2026-12-15",
    "endDate": "2027-01-15",
    "priceModifierPct": 20,
    "minNights": 2
}
```
**Response:** `201 Created`

---

### DELETE /api/properties/1/seasonal-rules/{ruleId}
**Response:** `204 No Content`

---

## 7. Review Controller (`/api/reviews`)

### GET /api/reviews/{id}
**Response:** `200 OK` | `404 Not Found`

---

### GET /api/reviews/property/{propertyId}
Dohvati recenzije za nekretninu.

**Response:** `200 OK`

---

### GET /api/reviews/guest/{guestId}
Dohvati recenzije gosta.

**Response:** `200 OK`

---

### POST /api/reviews
```json
{
    "reservationId": 200,
    "guestId": 1,
    "propertyId": 1,
    "hostId": 1,
    "ratingCleanliness": 4.5,
    "ratingLocation": 4.0,
    "ratingCommunication": 5.0,
    "ratingValue": 4.5,
    "ratingAccuracy": 4.0,
    "comment": "Odličan smještaj, preporučujem!"
}
```
**Response:** `201 Created`
```json
{
    "id": 4,
    "reservationId": 200,
    "guestId": 1,
    "propertyId": 1,
    "hostId": 1,
    "ratingCleanliness": 4.5,
    "ratingLocation": 4.0,
    "ratingCommunication": 5.0,
    "ratingValue": 4.5,
    "ratingAccuracy": 4.0,
    "overallRating": 4.40,
    "comment": "Odličan smještaj, preporučujem!",
    "hostReply": null,
    "createdAt": "2026-04-14T01:45:00",
    "repliedAt": null
}
```

---

### PUT /api/reviews/{id}/reply
Dodaj odgovor domaćina.

```json
{
    "reply": "Hvala vam na lijepim riječima!"
}
```
**Response:** `200 OK`

---

## 8. Wishlist Controller (`/api/wishlists`)

### GET /api/wishlists/guest/{guestId}
Dohvati liste želja gosta.

**Response:** `200 OK`

---

### GET /api/wishlists/{id}
**Response:** `200 OK` | `404 Not Found`

---

### POST /api/wishlists
```json
{
    "guestId": 1,
    "name": "Sarajevo 2026"
}
```
**Response:** `201 Created`

---

### DELETE /api/wishlists/{id}
**Response:** `204 No Content`

---

### GET /api/wishlists/{wishlistId}/items
Dohvati stavke liste želja.

**Response:** `200 OK`
```json
[
    { "id": 1, "wishlistId": 1, "propertyId": 1, "addedAt": "2026-04-14T01:00:00" }
]
```

---

### POST /api/wishlists/1/items
```json
{
    "propertyId": 3
}
```
**Response:** `201 Created`

---

### DELETE /api/wishlists/1/items/{itemId}
**Response:** `204 No Content`

---

## Postman Testiranje

### Preduvjeti
1. Pokreni XAMPP (MySQL)
2. Kreiraj bazu: `CREATE DATABASE propertydb;`
3. Pokreni property-service (`mvn spring-boot:run` ili iz IDE)
4. DataLoader automatski popuni bazu sa test podacima

### Primjeri testiranja

**Uspješan request — Kreiraj nekretninu:**
- Metoda: `POST`
- URL: `http://localhost:8082/api/properties`
- Body → raw → JSON:
```json
{
    "hostId": 1,
    "name": "Vila Mostar",
    "description": "Prekrasna vila",
    "address": "Bulevar 10",
    "city": "Mostar",
    "country": "BiH",
    "maxGuests": 6
}
```
- Očekivani odgovor: `201 Created`

**Neuspješan request — Validacijska greška:**
- Metoda: `POST`
- URL: `http://localhost:8082/api/properties`
- Body → raw → JSON:
```json
{
    "description": "Samo opis"
}
```
- Očekivani odgovor: `400 Bad Request` sa porukama za `hostId`, `name`, `address`, `city`, `country`

**Neuspješan request — Nepostojeći resurs:**
- Metoda: `GET`
- URL: `http://localhost:8082/api/properties/999`
- Očekivani odgovor: `404 Not Found`

**Dohvati recenzije za nekretninu:**
- Metoda: `GET`
- URL: `http://localhost:8082/api/reviews/property/1`
- Očekivani odgovor: `200 OK` sa listom recenzija

**Dodaj stavku u listu želja:**
- Metoda: `POST`
- URL: `http://localhost:8082/api/wishlists/1/items`
- Body: `{ "propertyId": 2 }`
- Očekivani odgovor: `201 Created`
