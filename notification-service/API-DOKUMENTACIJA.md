# Notification Service - API Dokumentacija

## Bazni URL
```
http://localhost:8086
```

---

## 1. Notification API (`/api/notifications`)

### 1.1 Kreiranje notifikacije
- **POST** `/api/notifications`
- **Request Body:**
```json
{
    "userId": 1,
    "type": "BOOKING",
    "title": "Nova rezervacija",
    "content": "Imate novu rezervaciju za apartman Sunce",
    "relatedReservationId": 100
}
```
- **Response:** `201 Created`
```json
{
    "id": 1,
    "userId": 1,
    "type": "BOOKING",
    "title": "Nova rezervacija",
    "content": "Imate novu rezervaciju za apartman Sunce",
    "isRead": false,
    "relatedReservationId": 100,
    "createdAt": "2026-04-14T10:00:00",
    "readAt": null
}
```

### 1.2 Dohvat notifikacije po ID-u
- **GET** `/api/notifications/{id}`
- **Primjer:** `GET /api/notifications/1`
- **Response:** `200 OK`

### 1.3 Dohvat svih notifikacija
- **GET** `/api/notifications`
- **Response:** `200 OK` — lista svih notifikacija

### 1.4 Dohvat notifikacija po korisniku
- **GET** `/api/notifications/user/{userId}`
- **Primjer:** `GET /api/notifications/user/1`
- **Response:** `200 OK` — lista notifikacija korisnika (sortirano po createdAt DESC)

### 1.5 Označavanje notifikacije kao pročitane
- **PUT** `/api/notifications/{id}/read`
- **Primjer:** `PUT /api/notifications/1/read`
- **Response:** `200 OK`
```json
{
    "id": 1,
    "isRead": true,
    "readAt": "2026-04-14T10:30:00"
}
```

### 1.6 Brisanje notifikacije
- **DELETE** `/api/notifications/{id}`
- **Response:** `204 No Content`

---

## 2. Conversation API (`/api/conversations`)

### 2.1 Kreiranje konverzacije
- **POST** `/api/conversations`
- **Request Body:**
```json
{
    "guestId": 1,
    "hostId": 2,
    "propertyId": 10,
    "reservationId": 100
}
```
- **Response:** `201 Created`
```json
{
    "id": 1,
    "guestId": 1,
    "hostId": 2,
    "propertyId": 10,
    "reservationId": 100,
    "createdAt": "2026-04-14T10:00:00",
    "updatedAt": null
}
```

### 2.2 Dohvat konverzacije po ID-u
- **GET** `/api/conversations/{id}`
- **Primjer:** `GET /api/conversations/1`
- **Response:** `200 OK`

### 2.3 Dohvat svih konverzacija
- **GET** `/api/conversations`
- **Response:** `200 OK` — lista svih konverzacija

### 2.4 Dohvat konverzacija po gostu
- **GET** `/api/conversations/guest/{guestId}`
- **Primjer:** `GET /api/conversations/guest/1`
- **Response:** `200 OK`

### 2.5 Dohvat konverzacija po domaćinu
- **GET** `/api/conversations/host/{hostId}`
- **Primjer:** `GET /api/conversations/host/2`
- **Response:** `200 OK`

### 2.6 Brisanje konverzacije
- **DELETE** `/api/conversations/{id}`
- **Response:** `204 No Content`

---

## 3. Message API (`/api/messages`)

### 3.1 Slanje poruke
- **POST** `/api/messages`
- **Request Body:**
```json
{
    "conversationId": 1,
    "senderId": 1,
    "content": "Pozdrav, da li je apartman dostupan?"
}
```
- **Response:** `201 Created`
```json
{
    "id": 1,
    "conversationId": 1,
    "senderId": 1,
    "content": "Pozdrav, da li je apartman dostupan?",
    "isRead": false,
    "sentAt": "2026-04-14T10:00:00",
    "readAt": null
}
```

### 3.2 Dohvat poruke po ID-u
- **GET** `/api/messages/{id}`
- **Primjer:** `GET /api/messages/1`
- **Response:** `200 OK`

### 3.3 Dohvat poruka po konverzaciji
- **GET** `/api/messages/conversation/{conversationId}`
- **Primjer:** `GET /api/messages/conversation/1`
- **Response:** `200 OK` — lista poruka sortirano po sentAt ASC

### 3.4 Označavanje poruke kao pročitane
- **PUT** `/api/messages/{id}/read`
- **Primjer:** `PUT /api/messages/1/read`
- **Response:** `200 OK`
```json
{
    "id": 1,
    "isRead": true,
    "readAt": "2026-04-14T10:30:00"
}
```

### 3.5 Brisanje poruke
- **DELETE** `/api/messages/{id}`
- **Response:** `204 No Content`

---

## Testiranje u Postmanu

### Koraci:
1. Pokrenite XAMPP (MySQL)
2. Pokrenite notification-service (`mvn spring-boot:run` na portu 8086)
3. U Postmanu testirajte redoslijedom:

**Notifikacije:**
- POST `http://localhost:8086/api/notifications` (sa JSON tijelom)
- GET `http://localhost:8086/api/notifications`
- GET `http://localhost:8086/api/notifications/1`
- GET `http://localhost:8086/api/notifications/user/1`
- PUT `http://localhost:8086/api/notifications/1/read`
- DELETE `http://localhost:8086/api/notifications/1`

**Konverzacije:**
- POST `http://localhost:8086/api/conversations` (sa JSON tijelom)
- GET `http://localhost:8086/api/conversations`
- GET `http://localhost:8086/api/conversations/1`
- GET `http://localhost:8086/api/conversations/guest/1`
- GET `http://localhost:8086/api/conversations/host/2`
- DELETE `http://localhost:8086/api/conversations/1`

**Poruke:**
- POST `http://localhost:8086/api/messages` (sa JSON tijelom)
- GET `http://localhost:8086/api/messages/1`
- GET `http://localhost:8086/api/messages/conversation/1`
- PUT `http://localhost:8086/api/messages/1/read`
- DELETE `http://localhost:8086/api/messages/1`

### Error handling primjeri:
- GET `http://localhost:8086/api/notifications/999` → `404 Not Found`
- POST sa praznim tijelom → `400 Bad Request` sa validacijskim porukama

---

## Validacija
| Polje | Pravilo |
|-------|---------|
| `NotificationRequestDTO.userId` | @NotNull |
| `NotificationRequestDTO.type` | @NotBlank |
| `NotificationRequestDTO.title` | @NotBlank |
| `ConversationRequestDTO.guestId` | @NotNull |
| `ConversationRequestDTO.hostId` | @NotNull |
| `MessageRequestDTO.conversationId` | @NotNull |
| `MessageRequestDTO.senderId` | @NotNull |
| `MessageRequestDTO.content` | @NotBlank |

## Testovi
- **53 testa** (9 NotificationService + 8 ConversationService + 9 MessageService + 9 NotificationController + 9 ConversationController + 8 MessageController + 1 Application)
- Pokretanje: `mvn clean test`
