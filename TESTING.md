# Testiranje BookingNWT Sistema

Ovaj dokument opisuje procedure za ručno testiranje i verifikaciju funkcionalnosti servisa.

## 1. Provjera Zdravlja Servisa (Health Check)
Možete provjeriti status bilo kojeg servisa putem Actuator-a:
```bash
curl http://localhost:8080/api/users/1
curl http://localhost:8080/api/properties/test
curl http://localhost:8080/api/reviews/property/1
```

## 2. Load Balancing Test (Property Service)
Da biste testirali raspodjelu zahtjeva između instanci `property-service`:

### Priprema
1. Provjerite da li su svi Docker kontejneri pokrenuti:
   `docker ps`
2. Osigurajte da `property-service` ima više instanci registrovanih na Eureki (port 8761):
   `http://localhost:8761/`

### Pokretanje testa
U korijenu projekta pokrenite PowerShell skriptu:
```powershell
.\property-service\test-load-balancing.ps1
```

### Očekivani rezultat
- Skripta ispisuje odgovor svakog od 100 zahtjeva.
- Trebali biste vidjeti rotaciju između `(Port: 8082)` i `(Port: 8083)`.

## 3. Pokretanje testova (Maven u Docker-u)
## 3. Testiranje Review Servisa
Za testiranje funkcionalnosti review servisa:

1. **Dohvatanje recenzija za nekretninu:**
   Pozovite endpoint za određenu nekretninu (npr. ID 1):
   ```bash
   curl http://localhost:8080/api/reviews/property/1
   ```
2. **Verifikacija:**
   Provjerite da li API vraća JSON listu recenzija. Ako je lista prazna, provjerite da li u bazi `reviewdb` postoje podaci za taj ID.

## 5. Pokretanje testova (Maven u Docker-u)
Ako nemate instaliran Maven lokalno, možete pokrenuti testove unutar Docker kontejnera koristeći postojeću sliku:

```bash
# Pokretanje svih testova u property-service
docker run --rm -v ${PWD}/property-service:/app -w /app maven:3.9-eclipse-temurin-21 ./mvnw test

# Pokretanje specifične klase
docker run --rm -v ${PWD}/property-service:/app -w /app maven:3.9-eclipse-temurin-21 ./mvnw test -Dtest=PropertyControllerTest
```

*Napomena: Ako koristite PowerShell, koristite `${PWD}` kao što je prikazano.*
