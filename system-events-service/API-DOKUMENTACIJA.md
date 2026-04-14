# System Events Service - API Dokumentacija

## Bazni URL
```
http://localhost:8087
```

---

## Audit Log API (`/api/audit-logs`)

### 1. Kreiranje audit loga
- **POST** `/api/audit-logs`
- **Request Body:**
```json
{
    "userId": 2,
    "action": "CREATE",
    "entityType": "PROPERTY",
    "entityId": 10,
    "details": "Kreiran objekat: Apartman Baščaršija, Sarajevo",
    "ipAddress": "192.168.1.10"
}
```
- **Response:** `201 Created`
```json
{
    "id": 1,
    "userId": 2,
    "action": "CREATE",
    "entityType": "PROPERTY",
    "entityId": 10,
    "details": "Kreiran objekat: Apartman Baščaršija, Sarajevo",
    "ipAddress": "192.168.1.10",
    "createdAt": "2026-04-14T10:00:00"
}
```

### 2. Dohvat audit loga po ID-u
- **GET** `/api/audit-logs/{id}`
- **Primjer:** `GET /api/audit-logs/1`
- **Response:** `200 OK` | `404 Not Found`

### 3. Dohvat svih audit logova
- **GET** `/api/audit-logs`
- **Response:** `200 OK` — lista svih audit logova

### 4. Dohvat audit logova po korisniku
- **GET** `/api/audit-logs/user/{userId}`
- **Primjer:** `GET /api/audit-logs/user/2`
- **Response:** `200 OK` — sortirano po createdAt DESC

### 5. Dohvat audit logova po tipu entiteta
- **GET** `/api/audit-logs/entity-type/{entityType}`
- **Primjer:** `GET /api/audit-logs/entity-type/PROPERTY`
- **Tipovi entiteta:** `PROPERTY`, `RESERVATION`, `PAYMENT`, `REVIEW`, `IDENTITY_VERIFICATION`, `PRICING_RULE`
- **Response:** `200 OK` — sortirano po createdAt DESC

### 6. Dohvat audit logova po akciji
- **GET** `/api/audit-logs/action/{action}`
- **Primjer:** `GET /api/audit-logs/action/CREATE`
- **Akcije:** `CREATE`, `UPDATE`, `DELETE`
- **Response:** `200 OK` — sortirano po createdAt DESC

### 7. Brisanje audit loga
- **DELETE** `/api/audit-logs/{id}`
- **Response:** `204 No Content` | `404 Not Found`

---

## Testiranje u Postmanu

### Preduvjeti
1. Pokrenite XAMPP (MySQL)
2. Kreirajte bazu: `CREATE DATABASE systemeventsdb;`
3. Pokrenite system-events-service (`mvn spring-boot:run` na portu 8087)
4. DataLoader automatski popuni bazu sa 10 audit logova

### Primjeri testiranja

**Kreiranje audit loga:**
- Metoda: `POST`
- URL: `http://localhost:8087/api/audit-logs`
- Body → raw → JSON:
```json
{
    "userId": 4,
    "action": "CREATE",
    "entityType": "RESERVATION",
    "entityId": 5,
    "details": "Nova rezervacija za Vila Stari Most",
    "ipAddress": "192.168.1.20"
}
```
- Očekivani odgovor: `201 Created`

**Filtriranje po tipu entiteta:**
- `GET http://localhost:8087/api/audit-logs/entity-type/PROPERTY`
- Očekivani odgovor: `200 OK` sa listom PROPERTY logova

**Filtriranje po akciji:**
- `GET http://localhost:8087/api/audit-logs/action/UPDATE`
- Očekivani odgovor: `200 OK` sa listom UPDATE logova

**Nepostojeći resurs:**
- `GET http://localhost:8087/api/audit-logs/999` → `404 Not Found`

**Validacijska greška:**
- POST sa praznim tijelom → `400 Bad Request`

---

## Validacija
| Polje | Pravilo |
|-------|---------|
| `userId` | @NotNull |
| `action` | @NotBlank |
| `entityType` | @NotBlank |
| `entityId` | opciono |
| `details` | opciono |
| `ipAddress` | opciono |

## Testovi
- **20 testova** (9 AuditLogService + 10 AuditLogController + 1 Application)
- Pokretanje: `mvn clean test`
