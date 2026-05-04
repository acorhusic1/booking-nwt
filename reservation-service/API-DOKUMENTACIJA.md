# Reservation Service - API Dokumentacija

**Base URL:** `http://localhost:8083` (kroz API Gateway: `http://localhost:8080/api/reservations/...`)

> **Sigurnost (Task 7):** svi endpointi zahtijevaju validan JWT u zaglavlju
> `Authorization: Bearer <token>`. Role potrebne za pojedinačni endpoint
> navedene su pored opisa. Greške:
> - `401 Unauthorized` — token nedostaje ili je nevalidan
> - `403 Forbidden` — token validan ali rola nije dovoljna

> **Service discovery / healthchecks (Task 4):** servis se registruje na
> Eureku (`http://eureka-server:8761`). Healthcheck dostupan na
> `GET /actuator/health`, stanje circuit-breakera na
> `GET /actuator/circuitbreakers`.

> **Sinhrona komunikacija (Task 5 — Reservation → Property):** kreiranje
> rezervacije (`POST /api/reservations` i `POST /api/reservations/batch`)
> sinhrono provjerava preko OpenFeigna i Eureke da li smještaj postoji,
> nije neaktivan, i nema preklapajući blok u kalendaru. Poziv je zaštićen
> Resilience4j circuit-breakerom (`property-service`). Politika je
> **fail-closed** — ako property-service nije dostupan, rezervacija se odbija
> sa **`409 Conflict`** (ne kreiramo nepotvrđeni booking jer bi to vodilo u
> dvostruke rezervacije).

---

## 1. Reservation Controller (`/api/reservations`)

### 1.1 Kreiranje rezervacije
- **POST** `/api/reservations`
- **Request Body:**
```json
{
    "guestId": 10,
    "hostId": 20,
    "propertyId": 30,
    "checkIn": "2026-08-01",
    "checkOut": "2026-08-05",
    "numGuests": 2,
    "totalPrice": 500.00,
    "cancellationPolicyId": 1,
    "promoCodeId": null
}
```
- **Response (201):**
```json
{
    "id": 1,
    "guestId": 10,
    "hostId": 20,
    "propertyId": 30,
    "checkIn": "2026-08-01",
    "checkOut": "2026-08-05",
    "numGuests": 2,
    "totalPrice": 500.00,
    "status": "CREATED",
    "cancellationPolicyId": 1,
    "promoCodeId": null,
    "createdAt": "2026-04-14T12:00:00",
    "updatedAt": null
}
```

### 1.2 Dohvat rezervacije po ID
- **GET** `/api/reservations/{id}`
- **Response (200):** Isti format kao gore

### 1.3 Dohvat svih rezervacija
- **GET** `/api/reservations`
- **Response (200):** Lista rezervacija

### 1.4 Dohvat rezervacija po gostu
- **GET** `/api/reservations/guest/{guestId}`
- **Response (200):** Lista rezervacija za datog gosta

### 1.5 Dohvat rezervacija po objektu
- **GET** `/api/reservations/property/{propertyId}`
- **Response (200):** Lista rezervacija za dati objekat

### 1.6 Dohvat rezervacija po hostu
- **GET** `/api/reservations/host/{hostId}`
- **Response (200):** Lista rezervacija za datog hosta

### 1.7 Promjena statusa rezervacije
- **PUT** `/api/reservations/{id}/status?status=CONFIRMED`
- **Mogući statusi:** `CREATED`, `CONFIRMED`, `ACTIVE`, `COMPLETED`, `CANCELLED`
- **Response (200):** Ažurirana rezervacija

### 1.8 Otkazivanje rezervacije
- **PUT** `/api/reservations/{id}/cancel`
- **Response (200):** Rezervacija sa statusom `CANCELLED`

### 1.9 Brisanje rezervacije
- **DELETE** `/api/reservations/{id}` — _Role: ADMIN_
- **Response (204):** No Content

### 1.10 PATCH (Task 4 — RFC 6902 JSON Patch)
- **PATCH** `/api/reservations/{id}` — _Role: USER, ADMIN, HOST_
- **Content-Type:** `application/json-patch+json`
- **Request Body (primjer — promjena broja gostiju i ukupne cijene):**
```json
[
  { "op": "replace", "path": "/numGuests", "value": 4 },
  { "op": "replace", "path": "/totalPrice", "value": 600.00 }
]
```
- **Response (200):** Ažurirana rezervacija
- **Response (400):** Neispravna PATCH operacija

### 1.11 Paginacija + sortiranje rezervacija po gostu (Task 4)
- **GET** `/api/reservations/guest/{guestId}/paged?page=0&size=10&sort=createdAt,desc`
- _Role: USER, ADMIN_
- **Response (200):** Spring `Page<ReservationResponseDTO>` sa `content`, `totalPages`, itd.

### 1.12 Custom JPQL — rezervacije po gostu u opsegu datuma (Task 4)
- **GET** `/api/reservations/guest/{guestId}/range?from=2026-06-01&to=2026-09-30`
- _Role: USER, ADMIN_
- **Response (200):** Lista rezervacija u datom opsegu

### 1.13 Custom @Query — prihod hosta (Task 4)
- **GET** `/api/reservations/host/{hostId}/revenue`
- _Role: HOST, ADMIN_
- **Response (200):** Suma `totalPrice` rezervacija sa statusom `CONFIRMED`/`ACTIVE`/`COMPLETED`

### 1.14 Batch unos (Task 4)
- **POST** `/api/reservations/batch` — _Role: ADMIN_
- **Request Body:** Lista `ReservationRequestDTO` objekata
- **Response (201):** Lista kreiranih rezervacija (`saveAll`)

### 1.15 EntityGraph — rezervacija sa svim detaljima (Task 4)
- **GET** `/api/reservations/{id}/details` — _Role: USER, ADMIN, HOST_
- **Response (200):** Rezervacija + `cancellationPolicy` + `promoCode` + `problemReports` u jednom upitu

---

## 2. CancellationPolicy Controller (`/api/cancellation-policies`)

### 2.1 Kreiranje politike otkazivanja
- **POST** `/api/cancellation-policies`
- **Request Body:**
```json
{
    "propertyId": 100,
    "name": "Fleksibilna",
    "freeCancelDays": 7,
    "partialRefundPct": 50,
    "noRefund": false
}
```
- **Response (201):**
```json
{
    "id": 1,
    "propertyId": 100,
    "name": "Fleksibilna",
    "freeCancelDays": 7,
    "partialRefundPct": 50,
    "noRefund": false,
    "createdAt": "2026-04-14T12:00:00"
}
```

### 2.2 Dohvat politike po ID
- **GET** `/api/cancellation-policies/{id}`
- **Response (200):** Politika otkazivanja

### 2.3 Dohvat svih politika
- **GET** `/api/cancellation-policies`
- **Response (200):** Lista politika

### 2.4 Dohvat politika po objektu
- **GET** `/api/cancellation-policies/property/{propertyId}`
- **Response (200):** Lista politika za dati objekat

### 2.5 Ažuriranje politike
- **PUT** `/api/cancellation-policies/{id}`
- **Request Body:** Isti format kao POST
- **Response (200):** Ažurirana politika

### 2.6 Brisanje politike
- **DELETE** `/api/cancellation-policies/{id}`
- **Response (204):** No Content

---

## 3. PromoCode Controller (`/api/promo-codes`)

### 3.1 Kreiranje promo koda
- **POST** `/api/promo-codes`
- **Request Body:**
```json
{
    "code": "SUMMER2025",
    "description": "Ljetnji popust",
    "discountType": "PERCENTAGE",
    "discountValue": 15.00,
    "minNights": 3,
    "validFrom": "2025-06-01",
    "validTo": "2025-09-30",
    "maxUses": 100,
    "createdBy": 1
}
```
- **Response (201):**
```json
{
    "id": 1,
    "code": "SUMMER2025",
    "description": "Ljetnji popust",
    "discountType": "PERCENTAGE",
    "discountValue": 15.00,
    "minNights": 3,
    "validFrom": "2025-06-01",
    "validTo": "2025-09-30",
    "maxUses": 100,
    "usageCount": 0,
    "createdBy": 1,
    "createdAt": "2026-04-14T12:00:00"
}
```
- **Tipovi popusta:** `PERCENTAGE`, `FIXED`

### 3.2 Dohvat promo koda po ID
- **GET** `/api/promo-codes/{id}`
- **Response (200):** Promo kod

### 3.3 Dohvat promo koda po kodu
- **GET** `/api/promo-codes/code/{code}`
- **Primjer:** `/api/promo-codes/code/SUMMER2025`
- **Response (200):** Promo kod

### 3.4 Dohvat svih promo kodova
- **GET** `/api/promo-codes`
- **Response (200):** Lista promo kodova

### 3.5 Ažuriranje promo koda
- **PUT** `/api/promo-codes/{id}`
- **Request Body:** Isti format kao POST
- **Response (200):** Ažurirani promo kod

### 3.6 Brisanje promo koda
- **DELETE** `/api/promo-codes/{id}`
- **Response (204):** No Content

---

## 4. ProblemReport Controller (`/api/problem-reports`)

### 4.1 Kreiranje prijave problema
- **POST** `/api/problem-reports`
- **Request Body:**
```json
{
    "reservationId": 1,
    "reporterId": 10,
    "category": "Čistoća",
    "description": "Soba nije bila čista pri dolasku"
}
```
- **Response (201):**
```json
{
    "id": 1,
    "reservationId": 1,
    "reporterId": 10,
    "category": "Čistoća",
    "description": "Soba nije bila čista pri dolasku",
    "status": "REPORTED",
    "reportedAt": "2026-04-14T12:00:00",
    "resolvedAt": null
}
```

### 4.2 Dohvat prijave po ID
- **GET** `/api/problem-reports/{id}`
- **Response (200):** Prijava problema

### 4.3 Dohvat svih prijava
- **GET** `/api/problem-reports`
- **Response (200):** Lista prijava

### 4.4 Dohvat prijava po rezervaciji
- **GET** `/api/problem-reports/reservation/{reservationId}`
- **Response (200):** Lista prijava za datu rezervaciju

### 4.5 Dohvat prijava po reporteru
- **GET** `/api/problem-reports/reporter/{reporterId}`
- **Response (200):** Lista prijava od datog reportera

### 4.6 Promjena statusa prijave
- **PUT** `/api/problem-reports/{id}/status?status=RESOLVED`
- **Mogući statusi:** `REPORTED`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`
- **Response (200):** Ažurirana prijava

### 4.7 Brisanje prijave
- **DELETE** `/api/problem-reports/{id}`
- **Response (204):** No Content

---

## Testiranje u Postmanu

### Koraci za pokretanje:
1. Pokrenuti XAMPP (MySQL)
2. Kreirati bazu `reservationdb` (ako ne postoji)
3. Pokrenuti `ReservationServiceApplication` (port 8083)

### Postman kolekcija — redosljed testiranja:

**Korak 1: Kreirati CancellationPolicy**
- `POST http://localhost:8083/api/cancellation-policies`
- Body (JSON):
```json
{
    "propertyId": 1,
    "name": "Fleksibilna",
    "freeCancelDays": 7,
    "partialRefundPct": 50,
    "noRefund": false
}
```

**Korak 2: Kreirati PromoCode**
- `POST http://localhost:8083/api/promo-codes`
- Body (JSON):
```json
{
    "code": "SUMMER2025",
    "description": "Ljetnji popust 15%",
    "discountType": "PERCENTAGE",
    "discountValue": 15.00,
    "minNights": 3,
    "validFrom": "2025-06-01",
    "validTo": "2025-09-30",
    "maxUses": 100,
    "createdBy": 1
}
```

**Korak 3: Kreirati Reservation**
- `POST http://localhost:8083/api/reservations`
- Body (JSON):
```json
{
    "guestId": 1,
    "hostId": 2,
    "propertyId": 1,
    "checkIn": "2026-08-01",
    "checkOut": "2026-08-05",
    "numGuests": 2,
    "totalPrice": 425.00,
    "cancellationPolicyId": 1,
    "promoCodeId": 1
}
```

**Korak 4: Testirati GET endpointe**
- `GET http://localhost:8083/api/reservations`
- `GET http://localhost:8083/api/reservations/1`
- `GET http://localhost:8083/api/reservations/guest/1`
- `GET http://localhost:8083/api/reservations/property/1`
- `GET http://localhost:8083/api/reservations/host/2`
- `GET http://localhost:8083/api/cancellation-policies`
- `GET http://localhost:8083/api/cancellation-policies/property/1`
- `GET http://localhost:8083/api/promo-codes`
- `GET http://localhost:8083/api/promo-codes/code/SUMMER2025`

**Korak 5: Ažurirati status rezervacije**
- `PUT http://localhost:8083/api/reservations/1/status?status=CONFIRMED`

**Korak 6: Otkazati rezervaciju**
- `PUT http://localhost:8083/api/reservations/1/cancel`

**Korak 7: Kreirati ProblemReport**
- `POST http://localhost:8083/api/problem-reports`
- Body (JSON):
```json
{
    "reservationId": 1,
    "reporterId": 1,
    "category": "Čistoća",
    "description": "Soba nije bila čista pri dolasku"
}
```

**Korak 8: Ažurirati status prijave**
- `PUT http://localhost:8083/api/problem-reports/1/status?status=IN_PROGRESS`
- `PUT http://localhost:8083/api/problem-reports/1/status?status=RESOLVED`

**Korak 9: Testirati brisanje**
- `DELETE http://localhost:8083/api/problem-reports/1`
- `DELETE http://localhost:8083/api/reservations/1`
- `DELETE http://localhost:8083/api/promo-codes/1`
- `DELETE http://localhost:8083/api/cancellation-policies/1`

### Napomena o validaciji:
- `checkIn` i `checkOut` moraju biti datumi u budućnosti
- `numGuests` minimalno 1
- `totalPrice` ne smije biti negativna
- `code` promo koda mora biti jedinstven
- `reservationId` mora postojati kad se kreira ProblemReport
