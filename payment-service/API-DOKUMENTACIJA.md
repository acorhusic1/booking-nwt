# Payment Service - REST API Dokumentacija

## Bazni URL
```
http://localhost:8084/api
```

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
Obriši plaćanje.

**Response:** `204 No Content` | `404 Not Found`

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
