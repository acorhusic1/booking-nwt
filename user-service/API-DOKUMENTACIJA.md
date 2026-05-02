# User Service - REST API Dokumentacija

## Bazni URL
```
http://localhost:8081/api
```

## Error Format (Uniformno upravljanje greškama)

Sve greške vraćaju uniforman JSON format:
```json
{
    "timestamp": "2026-04-14T01:00:00",
    "status": 404,
    "error": "Not Found",
    "message": "Korisnik sa ID 99 nije pronađen"
}
```

### Validacijske greške (400):
```json
{
    "timestamp": "2026-04-14T01:00:00",
    "status": 400,
    "error": "Bad Request",
    "message": {
        "email": "Email je obavezan",
        "password": "Lozinka mora imati najmanje 6 karaktera"
    }
}
```

---

## 1. User Controller (`/api/users`)

### GET /api/users
Dohvati sve korisnike.

**Response:** `200 OK`
```json
[
    {
        "id": 1,
        "email": "ivo.ivic@email.com",
        "firstName": "Ivo",
        "lastName": "Ivić",
        "phone": "+38761111111",
        "role": "GUEST",
        "isActive": true,
        "lastLogin": null,
        "createdAt": "2026-04-14T01:00:00",
        "updatedAt": null
    }
]
```

---

### GET /api/users/{id}
Dohvati korisnika po ID-u.

**Response:** `200 OK`
```json
{
    "id": 1,
    "email": "ivo.ivic@email.com",
    "firstName": "Ivo",
    "lastName": "Ivić",
    "phone": "+38761111111",
    "role": "GUEST",
    "isActive": true,
    "lastLogin": null,
    "createdAt": "2026-04-14T01:00:00",
    "updatedAt": null
}
```

**Error:** `404 Not Found` — Korisnik ne postoji.

---

### GET /api/users/email/{email}
Dohvati korisnika po email adresi.

**Response:** `200 OK` — Isti format kao GET /api/users/{id}

**Error:** `404 Not Found` — Korisnik sa datim emailom ne postoji.

---

### POST /api/users
Kreiraj novog korisnika.

**Request Body:**
```json
{
    "email": "novi@email.com",
    "password": "pass123",
    "firstName": "Novi",
    "lastName": "Korisnik",
    "phone": "+38762222222",
    "role": "GUEST"
}
```

**Validacija:**
| Polje     | Pravilo                              |
|-----------|--------------------------------------|
| email     | Obavezno, validan email format       |
| password  | Obavezno, min 6 karaktera           |
| firstName | Obavezno                             |
| lastName  | Obavezno                             |
| phone     | Opcionalno                           |
| role      | Obavezno (GUEST, HOST, ADMIN)        |

**Response:** `201 Created`

**Error:** `400 Bad Request` — Validacijska greška.

---

### PUT /api/users/{id}
Ažuriraj postojećeg korisnika.

**Request Body:** Isti format kao POST.

**Response:** `200 OK`

**Error:** `404 Not Found` | `400 Bad Request`

---

### DELETE /api/users/{id}
Obriši korisnika.

**Response:** `204 No Content`

**Error:** `404 Not Found` — Korisnik ne postoji.

---

## 2. User Preference Controller (`/api/users/{userId}/preferences`)

### GET /api/users/{userId}/preferences
Dohvati preferencije korisnika.

**Response:** `200 OK`
```json
{
    "id": 1,
    "userId": 1,
    "preferredLanguage": "bs",
    "propertyType": "APARTMENT",
    "minPrice": 50.00,
    "maxPrice": 200.00,
    "updatedAt": "2026-04-14T01:00:00"
}
```

**Error:** `404 Not Found` — Preferencije ne postoje.

---

### PUT /api/users/{userId}/preferences
Kreiraj ili ažuriraj preferencije (upsert).

**Request Body:**
```json
{
    "preferredLanguage": "bs",
    "propertyType": "APARTMENT",
    "minPrice": 50.00,
    "maxPrice": 200.00
}
```

**Validacija:**
| Polje             | Pravilo                    |
|-------------------|----------------------------|
| preferredLanguage | Opcionalno                 |
| propertyType      | Opcionalno                 |
| minPrice          | >= 0.0                     |
| maxPrice          | >= 0.0                     |

**Response:** `200 OK`

**Error:** `404 Not Found` (korisnik ne postoji) | `400 Bad Request`

---

### DELETE /api/users/{userId}/preferences
Obriši preferencije korisnika.

**Response:** `204 No Content`

**Error:** `404 Not Found`

---

## 3. Identity Verification Controller (`/api/users/{userId}/verifications`)

### GET /api/users/{userId}/verifications
Dohvati sve verifikacije korisnika.

**Response:** `200 OK`
```json
[
    {
        "id": 1,
        "userId": 1,
        "documentType": "LIČNA KARTA",
        "documentNumber": "123456789",
        "status": "PENDING",
        "submittedAt": "2026-04-14T01:00:00",
        "verifiedAt": null,
        "verifiedBy": null
    }
]
```

**Error:** `404 Not Found` — Korisnik ne postoji.

---

### GET /api/users/{userId}/verifications/{id}
Dohvati verifikaciju po ID-u.

**Response:** `200 OK`

**Error:** `404 Not Found`

---

### POST /api/users/{userId}/verifications
Kreiraj novu verifikaciju identiteta.

**Request Body:**
```json
{
    "documentType": "LIČNA KARTA",
    "documentNumber": "123456789"
}
```

**Validacija:**
| Polje          | Pravilo    |
|----------------|------------|
| documentType   | Obavezno   |
| documentNumber | Obavezno   |

**Response:** `201 Created` — Status se automatski postavlja na `PENDING`.

**Error:** `404 Not Found` (korisnik ne postoji) | `400 Bad Request`

---

## Arhitektura

```
controller/
├── UserController.java
├── UserPreferenceController.java
└── IdentityVerificationController.java
service/
├── UserService.java (interface)
├── UserPreferenceService.java (interface)
├── IdentityVerificationService.java (interface)
└── impl/
    ├── UserServiceImpl.java
    ├── UserPreferenceServiceImpl.java
    └── IdentityVerificationServiceImpl.java
dto/
├── UserRequest.java
├── UserResponse.java
├── UserPreferenceRequest.java
├── UserPreferenceResponse.java
├── IdentityVerificationRequest.java
└── IdentityVerificationResponse.java
mapper/
├── UserMapper.java (MapStruct)
├── UserPreferenceMapper.java (MapStruct)
└── IdentityVerificationMapper.java (MapStruct)
exception/
├── ResourceNotFoundException.java
└── GlobalExceptionHandler.java
```

## Testovi

- **Service testovi** (Mockito + AssertJ): `UserServiceImplTest`, `UserPreferenceServiceImplTest`, `IdentityVerificationServiceImplTest`
- **Controller testovi** (@WebMvcTest + MockMvc): `UserControllerTest`, `UserPreferenceControllerTest`, `IdentityVerificationControllerTest`
- **Ukupno: 40 testova** — svi prolaze ✅
