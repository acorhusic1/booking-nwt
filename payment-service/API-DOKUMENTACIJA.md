# Payment Service - REST API Dokumentacija

## Bazni URL
```
http://localhost:8084/api          (direktno)
http://localhost:8080/api/payments (kroz API Gateway)
```

> **Sigurnost (Task 7):** svi endpointi zahtijevaju validan JWT u zaglavlju
> `Authorization: Bearer <token>`. Role potrebne za pojedinačni endpoint
> navedene su pored opisa. Greške:
> - `401 Unauthorized` — token nedostaje ili je nevalidan
> - `403 Forbidden` — token validan ali rola nije dovoljna

> **Sinhrona komunikacija (Task 5):** kada se status plaćanja promijeni na
> `COMPLETED` ili `FAILED`, payment-service preko OpenFeign-a poziva
> reservation-service da podesi status rezervacije (`CONFIRMED` / `CANCELLED`).
> Poziv je zaštićen Resilience4j circuit-breakerom (`reservation-service`) sa
> fallbackom — ako je downstream nedostupan, plaćanje i dalje uspijeva.

> **Service discovery / healthchecks (Task 4):** servis se registruje na
> Eureku (`http://eureka-server:8761`). Healthcheck dostupan na
> `GET /actuator/health`, stanje circuit-breakera na
> `GET /actuator/circuitbreakers`.

## Error Format
```json
{
    "timestamp": "2026-04-14T02:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Plaćanje sa ID 99 nije pronađeno"
}
```

### Validacijske greške (400):
```json
{
    "timestamp": "2026-04-14T02:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": {
        "amount": "Iznos je obavezan",
        "currency": "Valuta je obavezna"
    }
}
```

---

## 1. Payment Controller (`/api/payments`)

### POST /api/payments
Kreiraj novo plaćanje.

**Request Body:**
```json
{
    "reservationId": 10,
    "guestId": 4,
    "amount": 250.00,
    "currency": "BAM",
    "method": "WALLET"
}
```

**Response:** `201 Created`
```json
{
    "id": 5,
    "reservationId": 10,
    "guestId": 4,
    "amount": 250.00,
    "currency": "BAM",
    "status": "PENDING",
    "method": "WALLET",
    "processedAt": null,
    "relatedPaymentId": null
}
```

**Validacijska greška (prazno tijelo):** `400 Bad Request`

---

### GET /api/payments/{id}
Dohvati plaćanje po ID-u.

**Response:** `200 OK` | `404 Not Found`

---

### GET /api/payments
Dohvati sva plaćanja.

**Response:** `200 OK`

---

### GET /api/payments/reservation/{reservationId}
Dohvati plaćanja za rezervaciju.

**Response:** `200 OK`

---

### GET /api/payments/guest/{guestId}
Dohvati plaćanja po gostu.

**Response:** `200 OK`

---

### GET /api/payments/status/{status}
Dohvati plaćanja po statusu.

Statusi: `PENDING`, `COMPLETED`, `FAILED`, `REFUNDED`

**Response:** `200 OK`

---

### PUT /api/payments/{id}/status?status=COMPLETED
Ažuriraj status plaćanja.

**Response:** `200 OK`
```json
{
    "id": 5,
    "reservationId": 10,
    "guestId": 4,
    "amount": 250.00,
    "currency": "BAM",
    "status": "COMPLETED",
    "method": "WALLET",
    "processedAt": "2026-04-14T02:30:00",
    "relatedPaymentId": null
}
```

---

### POST /api/payments/{id}/refund
Refundiraj plaćanje. Samo plaćanja sa statusom `COMPLETED` mogu biti refundirana.

**Response:** `200 OK`
```json
{
    "id": 6,
    "reservationId": 10,
    "guestId": 4,
    "amount": 250.00,
    "currency": "BAM",
    "status": "REFUNDED",
    "method": "WALLET",
    "processedAt": "2026-04-14T02:35:00",
    "relatedPaymentId": 5
}
```

**Greška (plaćanje nije completed):** `400 Bad Request`
```json
{
    "message": "Samo završena plaćanja mogu biti refundirana"
}
```

---

### DELETE /api/payments/{id}
Obriši plaćanje. _Role: ADMIN_

**Response:** `204 No Content` | `404 Not Found`

---

### PATCH /api/payments/{id}  *(Task 4 — RFC 6902 JSON Patch)*
_Role: ADMIN_  ·  Content-Type: `application/json-patch+json`

```json
[
  { "op": "replace", "path": "/method", "value": "CARD" }
]
```

**Response:** `200 OK` | `400 Bad Request`

---

### GET /api/payments/guest/{guestId}/paged  *(Task 4 — Pageable)*
_Role: USER, ADMIN_

Query parametri: `?page=0&size=10&sort=processedAt,desc`

**Response:** `200 OK` — Spring `Page<PaymentResponseDTO>`

---

### GET /api/payments/guest/{guestId}/total-spent  *(Task 4 — custom @Query)*
_Role: USER, ADMIN_

Vraća sumu svih `COMPLETED` plaćanja za gosta.

**Response:** `200 OK`
```
"1234.56"
```

---

### GET /api/payments/status/{status}/min/{minAmount}  *(Task 4 — custom JPQL)*
_Role: ADMIN_

Plaćanja zadanog statusa sa iznosom ≥ `minAmount`, sortirana opadajuće.

**Response:** `200 OK` — lista plaćanja  ·  `400 Bad Request` ako je `minAmount < 0`

---

### GET /api/payments/status/{status}/count
_Role: ADMIN_

**Response:** `200 OK` — broj plaćanja datog statusa

---

### POST /api/payments/batch  *(Task 4 — saveAll)*
_Role: ADMIN_

**Request Body:** Lista `PaymentRequestDTO`

**Response:** `201 Created` — lista kreiranih plaćanja  ·  `400 Bad Request` ako je lista prazna

---

### GET /api/payments/{id}/details  *(Task 4 — @EntityGraph)*
_Role: USER, ADMIN_

Vraća plaćanje + `relatedPayment` + `walletTransactions` u jednom upitu (bez N+1).

**Response:** `200 OK` | `404 Not Found`

---

## 2. Wallet Controller (`/api/wallets`)

### POST /api/wallets
Kreiraj novi novčanik.

**Request Body:**
```json
{
    "userId": 15,
    "balance": 1000.00,
    "currency": "BAM"
}
```

**Response:** `201 Created`
```json
{
    "id": 6,
    "userId": 15,
    "balance": 1000.00,
    "currency": "BAM",
    "createdAt": "2026-04-14T02:30:00",
    "updatedAt": null
}
```

---

### GET /api/wallets/{id}
Dohvati novčanik po ID-u.

**Response:** `200 OK` | `404 Not Found`

---

### GET /api/wallets/user/{userId}
Dohvati novčanik po korisniku.

**Response:** `200 OK` | `404 Not Found`

---

### GET /api/wallets
Dohvati sve novčanike.

**Response:** `200 OK`

---

### POST /api/wallets/{id}/deposit?amount=200.00
Uplati sredstva na novčanik.

**Response:** `200 OK`
```json
{
    "id": 6,
    "userId": 15,
    "balance": 1200.00,
    "currency": "BAM",
    "createdAt": "2026-04-14T02:30:00",
    "updatedAt": "2026-04-14T02:35:00"
}
```

---

### POST /api/wallets/{id}/withdraw?amount=100.00
Isplati sredstva sa novčanika.

**Response:** `200 OK`

**Greška (nedovoljno sredstava):** `400 Bad Request`
```json
{
    "message": "Nedovoljno sredstava na novčaniku"
}
```

---

### DELETE /api/wallets/{id}
Obriši novčanik.

**Response:** `204 No Content` | `404 Not Found`

---

## 3. Wallet Transaction Controller (`/api/transactions`)

### GET /api/transactions/{id}
Dohvati transakciju po ID-u.

**Response:** `200 OK`
```json
{
    "id": 1,
    "walletId": 3,
    "amount": 500.00,
    "type": "DEPOSIT",
    "description": "Inicijalni deposit",
    "paymentId": null,
    "createdAt": "2026-04-14T02:00:00"
}
```

**Response:** `404 Not Found` (ako ne postoji)

---

### GET /api/transactions
Dohvati sve transakcije.

**Response:** `200 OK`

Tipovi transakcija: `DEPOSIT`, `WITHDRAWAL`, `PAYMENT`, `REFUND`, `PAYOUT`

---

### GET /api/transactions/wallet/{walletId}
Dohvati transakcije za novčanik.

**Response:** `200 OK`

---

### GET /api/transactions/payment/{paymentId}
Dohvati transakcije vezane za plaćanje.

**Response:** `200 OK`

---

## Postman Testiranje

### Preduvjeti
1. Pokreni XAMPP (MySQL)
2. Kreiraj bazu: `CREATE DATABASE paymentdb;`
3. Pokreni payment-service (`mvn spring-boot:run` ili iz IDE)
4. DataLoader automatski popuni bazu sa test podacima (5 novčanika, 4 plaćanja, 6 transakcija)

### Primjeri testiranja

**Uspješan request — Kreiraj plaćanje:**
- Metoda: `POST`
- URL: `http://localhost:8084/api/payments`
- Body → raw → JSON:
```json
{
    "reservationId": 10,
    "guestId": 4,
    "amount": 250.00,
    "currency": "BAM",
    "method": "WALLET"
}
```
- Očekivani odgovor: `201 Created`

**Neuspješan request — Validacijska greška:**
- Metoda: `POST`
- URL: `http://localhost:8084/api/payments`
- Body → raw → JSON:
```json
{
    "amount": -5
}
```
- Očekivani odgovor: `400 Bad Request` sa porukama za `reservationId`, `guestId`, `currency`, `method`, `amount`

**Neuspješan request — Nepostojeći resurs:**
- Metoda: `GET`
- URL: `http://localhost:8084/api/payments/999`
- Očekivani odgovor: `404 Not Found`

**Uplata na novčanik:**
- Metoda: `POST`
- URL: `http://localhost:8084/api/wallets/1/deposit?amount=200.00`
- Očekivani odgovor: `200 OK` sa ažuriranim balansom

**Refund plaćanja:**
- Metoda: `POST`
- URL: `http://localhost:8084/api/payments/1/refund`
- Očekivani odgovor: `200 OK` sa novim payment zapisom (status REFUNDED, relatedPaymentId=1)

**Dohvati transakcije za novčanik:**
- Metoda: `GET`
- URL: `http://localhost:8084/api/transactions/wallet/3`
- Očekivani odgovor: `200 OK` sa listom transakcija
