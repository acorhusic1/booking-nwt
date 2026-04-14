# API Dokumentacija - Analytics Service

## Bazni URL
```
http://localhost:8088
```

---

## 1. Property Statistics API (`/api/statistics`)

### 1.1 Kreiranje statistike
- **Metoda:** POST
- **URL:** `http://localhost:8088/api/statistics`
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
    "propertyId": 1,
    "hostId": 2,
    "year": 2026,
    "month": 5,
    "totalReservations": 12,
    "totalRevenue": 2400.00,
    "averageRating": 4.75,
    "occupancyRate": 68.50,
    "viewCount": 320,
    "cancellationCount": 2
}
```
- **Response:** `201 Created`

### 1.2 Dohvatanje statistike po ID-u
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/statistics/1`
- **Response:** `200 OK`

### 1.3 Dohvatanje svih statistika
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/statistics`
- **Response:** `200 OK`

### 1.4 Dohvatanje statistika po property ID-u
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/statistics/property/1`
- **Response:** `200 OK`

### 1.5 Dohvatanje statistika po host ID-u
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/statistics/host/2`
- **Response:** `200 OK`

### 1.6 Dohvatanje statistika po hostu i periodu
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/statistics/host/2/period?year=2026&month=4`
- **Response:** `200 OK`

### 1.7 Brisanje statistike
- **Metoda:** DELETE
- **URL:** `http://localhost:8088/api/statistics/1`
- **Response:** `204 No Content`

---

## 2. Revenue Report API (`/api/reports`)

### 2.1 Kreiranje izvještaja o prihodima
- **Metoda:** POST
- **URL:** `http://localhost:8088/api/reports`
- **Headers:** `Content-Type: application/json`
- **Body:**
```json
{
    "hostId": 2,
    "year": 2026,
    "month": 5,
    "totalRevenue": 5200.00,
    "platformCommission": 780.00,
    "netRevenue": 4420.00,
    "totalReservations": 18,
    "totalCancellations": 3,
    "totalProperties": 3,
    "averageOccupancyRate": 62.50
}
```
- **Response:** `201 Created`

### 2.2 Dohvatanje izvještaja po ID-u
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/reports/1`
- **Response:** `200 OK`

### 2.3 Dohvatanje svih izvještaja
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/reports`
- **Response:** `200 OK`

### 2.4 Dohvatanje izvještaja po host ID-u
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/reports/host/2`
- **Response:** `200 OK`

### 2.5 Dohvatanje izvještaja po hostu i godini
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/reports/host/2/year/2026`
- **Response:** `200 OK`

### 2.6 Dohvatanje izvještaja po periodu
- **Metoda:** GET
- **URL:** `http://localhost:8088/api/reports/period?year=2026&month=4`
- **Response:** `200 OK`

### 2.7 Brisanje izvještaja
- **Metoda:** DELETE
- **URL:** `http://localhost:8088/api/reports/1`
- **Response:** `204 No Content`

---

## Testiranje u Postmanu

### Koraci:
1. Pokrenite XAMPP (MySQL)
2. Pokrenite analytics-service (`mvn spring-boot:run`)
3. Otvorite Postman
4. Kreirajte novu kolekciju "Analytics Service"
5. Testirajte svaki endpoint redom

### Validacija:
- Sva polja označena sa `@NotNull` su obavezna (propertyId, hostId, year, month, totalReservations, totalRevenue, viewCount, cancellationCount za statistike; hostId, year, month, totalRevenue, platformCommission, netRevenue, totalReservations, totalCancellations, totalProperties za izvještaje)
- Nepostojeći ID vraća `404 Not Found`
- Nedostajuća obavezna polja vraćaju `400 Bad Request`

### Test rezultati:
- **Ukupno testova:** 39

| Test klasa | Broj testova | Status |
|-----------|-------------|--------|
| AnalyticsServiceApplicationTests | 1 | ✅ PASS |
| PropertyStatisticsServiceTest | 9 | ✅ PASS |
| RevenueReportServiceTest | 9 | ✅ PASS |
| PropertyStatisticsControllerTest | 10 | ✅ PASS |
| RevenueReportControllerTest | 10 | ✅ PASS |
