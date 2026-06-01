# Booking-NWT

Distribuirana platforma za rezervaciju smještaja, izgrađena kroz Spring Boot microservise i React SPA. Saga choreography preko RabbitMQ koordinira rezervaciju → naplatu → potvrdu kalendara, sa kompenzacijama na svakom koraku.

## Tim

| Član | Uloga |
|---|---|
| Benjamin Hadžihasanović | Backend (reservation-service, payment-service), frontend Dashboard |
| Emir | Backend (property-service), Host Dashboard, kalendar |
| Kenan | Notification-service, Saga listeneri, Messages |
| Acor (Husein) | API Gateway, user-service, Auth, Admin Dashboard |

## Arhitektura

**Infrastruktura:** MySQL 8, RabbitMQ 3.12, Eureka, Config Server, Spring Cloud Gateway
**Business servisi:** user, property, reservation, payment, notification, analytics, system-events
**Frontend:** React 18 + Vite + Zustand + react-hook-form + zod + react-router

```
booking-app (SPA, port 3000)
        │
        ▼
api-gateway (port 8080)  ────►  eureka-server
        │
        ├──► user-service          (auth, JWT, verifikacije)
        ├──► property-service      (smjestaji, kalendar, recenzije, wishlist)
        ├──► reservation-service   (rezervacije, promo kodovi, problem-reports)
        ├──► payment-service       (wallet, Stripe checkout)
        ├──► notification-service  (notifikacije, poruke, Saga listener)
        ├──► analytics-service     (reporting po hostu/mjesecu)
        └──► system-events-service (audit log)

Saga (RabbitMQ exchange = booking.events):
  RESERVATION_CREATED  ─► payment.queue          (naplata)
                       ─► property.queue         (rezervisanje slota)
                       ─► notification.queue     (host notif: nova rezervacija)
  PAYMENT_COMPLETED    ─► reservation.queue      (status → CONFIRMED)
                       ─► notification.queue     (gost notif: potvrda)
  PAYMENT_FAILED       ─► reservation.queue      (status → CANCELLED)
                       ─► notification.queue     (gost notif: otkazano)
  RESERVATION_CANCELLED ─► payment.cancellations (refund wallet)
                        ─► property.cancellations (oslobadja kalendar)
                        ─► notification.queue    (host notif: otkazano)
```

## Pokretanje preko Docker Compose

**Preduvjeti:** Docker Desktop 4.x+ (Windows / macOS / Linux), ~6 GB slobodne RAM.

```bash
# 1. Klon repozitorija
git clone https://github.com/acorhusic1/booking-nwt.git
cd booking-nwt

# 2. Build + start svih 14 kontejnera (prvi put traje ~5 min)
docker compose up -d --build

# 3. Provjeri da je sve healthy
docker ps --format "table {{.Names}}\t{{.Status}}"

# 4. Otvori SPA
# http://localhost:3000
```

Servisi mapiraju na portove:
- **3000** — Frontend (booking-app)
- **8080** — API Gateway
- **8761** — Eureka
- **8888** — Config Server
- **3306** — MySQL
- **15672** — RabbitMQ management (admin/admin)

### Login podaci (demo)

| Uloga | Email | Šifra |
|---|---|---|
| GUEST | benjamin@gmail.com | password |
| HOST | emir@gmail.com | password |
| ADMIN | admin@booking.com | password |

### Zaustavljanje

```bash
docker compose down              # zadržava DB volume
docker compose down -v           # briše i DB volume (čist start)
```

### Restart pojedinacnog servisa nakon code change-a

```bash
# Java servis (treba mvn package + rebuild)
cd reservation-service && mvn -q -DskipTests package && cd ..
docker compose up -d --build reservation-service

# Frontend
cd booking-app && npm run build && cd ..
docker compose up -d --build booking-app
```

## Lokalni development (bez Dockera)

Backend servisi se mogu pokrenuti direktno iz IDE-a — svaki ima `application.yml` koji čita config sa `config-server:8888` (mora biti up).

```bash
cd booking-app
npm install
npm run dev         # SPA na http://localhost:3000 sa proxy → :8080
```

## Testovi

### Backend (JUnit 5 + Mockito + Testcontainers)

```bash
cd reservation-service && mvn test
# 109 testova
```

### Frontend (Vitest + React Testing Library)

Svi testovi su organizovani u [`booking-app/src/test/`](booking-app/src/test/) sa istom strukturom direktorija kao izvorni kod.

```bash
cd booking-app
npm test            # one-shot (CI)
npm run test:watch  # watch mode (dev)
npm run test:ui     # vizualni UI u browser-u
```

134+ testova kroz 29 fajlova pokriva: utils, store, sve API module, common komponente (Modal, Spinner, ErrorState, StarRating, ToastProvider), forme i modale.

## Funkcionalni zahtjevi (F1–F19)

| ID | Naziv | Status | Napomena |
|---|---|---|---|
| F1 | Napredna pretraga smještaja sa filtriranjem | ✅ | Grad, država, period, kapacitet, sortiranje. Backend + frontend filtriraju i po overlapping rezervacijama. Filter po amenities (WiFi, parking) — podatak u modelu postoji, UI eksponira samo period + kapacitet + grad/državu. |
| F2 | Kreiranje i upravljanje objektima | ✅ | Host kreira/edituje/briše objekat sa slikama, kapacitetom, lokacijom, opisom, sadržajima. Moderacija prije objave nije strikno enforcuje (`isActive=true` po default-u). Više smještajnih jedinica unutar jednog objekta — model 1 property = 1 jedinica. |
| F3 | Upravljanje kalendarom dostupnosti | ✅ | Vizualni kalendar, blokiranje datuma, prikaz gostovih rezervacija (BUG H fix). Min/max nights kroz sezonska pravila (F15). |
| F4 | Dinamičko određivanje cijena | ✅ | Base price, weekend price, long-stay discount, sezonski modifikator. Breakdown se prikazuje gostu prije potvrde. |
| F5 | Proces rezervacije sa provjerom dostupnosti | ✅ | Saga choreography sprjecava duple rezervacije. Statusi: CREATED → CONFIRMED → ACTIVE → COMPLETED (auto-scheduler). 202 Accepted response. |
| F6 | Politike otkazivanja | ✅ | 3 tier-a: pun refund (unutar `freeCancelDays`), djelimičan (`partialRefundPct%`), bez refunda (`noRefund=true`). Saga propagira `refundPercentage` u payment-service koji kalkuliše tačan iznos za wallet credit. |
| F7 | Ocjenjivanje i recenzije | ✅ | 5 kategorija (čistoća, lokacija, komunikacija, vrijednost, tačnost), tekstualni komentar, host reply, prosječna ocjena na detalju. Samo COMPLETED rezervacije mogu ostaviti recenziju. |
| F8 | Sistem poruka gost ↔ host | ✅ | Konverzacije vezane za property/rezervaciju, prikaz imena umjesto ID-a, notifikacije o novim porukama. |
| F9 | Sistem notifikacija | ✅ | Notifikacije za: novu rezervaciju (host), potvrdu/otkazivanje (gost + host), nova poruka, recenzija. Read/unread status, bell badge sa polling-om (15s). Podsjetnik na dolazak nije implementiran. |
| F10 | Upravljanje listama želja | ✅ | Više listi po gostu, dodavanje/uklanjanje, prikaz srca na property kartici. |
| F11 | Izvještaji i statistike za domaćine | ✅ | RevenueChart (bar chart po mjesecu), ukupni prihod, broj rezervacija, broj potvrđenih. Filter po godini. |
| F12 | Preporuke smještaja na osnovu historije | ❌ | Nije implementirano. |
| F13 | Promotivni kodovi | ✅ | Procenat/fiksni iznos, period važenja, min nights, max usage. Backend + frontend validacija pri primjeni. |
| F14 | Evidencija sistemskih događaja (audit log) | ⏭ Preskočeno | Profesor je naveo da se ovaj task preskače. `system-events-service` postoji kao kostur sa AuditLog entity-em ali nije aktivno integrisan. |
| F15 | Sezonska pravila i ograničenja | ✅ | Cijenovni modifikator (+30%, -10%, ...), min nights u sezoni. Automatska primjena pri kreiranju rezervacije. |
| F16 | Verifikacija identiteta domaćina | ✅ | Host upload broja dokumenta, admin approve/reject, badge "Verifikovan" na host profilu. |
| F17 | Prijava problema tokom boravka | ✅ | 6 kategorija, opis problema, host vidi i mijenja status (REPORTED → IN_PROGRESS → RESOLVED/CLOSED). Notifikacija domaćinu. |
| F18 | Interaktivna mapa | ✅ | OpenStreetMap kroz `react-leaflet`. Toggle "🗺 Mapa / 🔳 Lista" na `/properties`. Marker per property sa popup-om (naziv, cijena, link na detalje). Pomicanje + zoom radi nativno. Host pri dodavanju smještaja koristi LocationPicker — klik na mapu postavlja lat/lng. |
| F19 | Plaćanje i virtualni novčanik | ✅ | Wallet (Saga naplata pri rezervaciji, refund pri otkazu), Stripe Checkout za dopunu kartice. Pri PAYMENT_FAILED — kompenzacijska transakcija oslobađa kalendar. |

**Sažetak:** 17 ✅ kompletno · 1 ⏭ namjerno preskočeno (F14) · 1 ❌ nedostaje (F12 — preporuke iz historije).

## Struktura repozitorija

```
booking-nwt/
├── booking-app/             # React SPA
│   ├── src/
│   │   ├── api/             # axios klijenti
│   │   ├── components/      # Re-usable UI
│   │   ├── store/           # Zustand store-ovi (auth)
│   │   ├── utils/           # Pricing helper
│   │   └── test/            # Svi unit testovi (mirror src/)
│   └── package.json
├── user-service/            # JWT auth, profili, verifikacije
├── property-service/        # Smjestaji, kalendar, recenzije, wishlist
├── reservation-service/     # Rezervacije, promo, problem reports
├── payment-service/         # Wallet, Stripe
├── notification-service/    # Notifikacije + Saga listener
├── analytics-service/       # Reporting
├── system-events-service/   # Audit log
├── api-gateway/             # Spring Cloud Gateway
├── config-server/           # Spring Cloud Config
├── eureka-server/           # Service discovery
└── docker-compose.yml
```

## Tehnologije

**Backend**
- Java 17, Spring Boot 3.x, Spring Cloud
- JPA / Hibernate, MySQL 8
- Spring Security + JWT
- RabbitMQ (Saga choreography)
- Resilience4j (circuit breaker)
- OpenFeign (sync inter-service calls)

**Frontend**
- React 18 + Vite
- Zustand (state), react-hook-form + zod (forme)
- react-router 6, axios
- Vitest + React Testing Library

**DevOps**
- Docker Compose
- Multi-stage Dockerfile builds
- Healthchecks + `depends_on: condition: service_healthy`

## Sigurnost — token-based user resolution

JWT sadrži `uid` claim sa autentifikovanim user ID-em. `reservation-service` već koristi taj `uid` u `POST /api/reservations` da pregazi `guestId` iz body-a (sprjecava da klijent rezerviše u nečije ime).

Isti pattern (čitanje `request.getAttribute("authUserId")` u kontroleru) treba primijeniti i u:
- `payment-service` — wallet operacije
- `property-service` — reviews, wishlist, calendar blocks
- `notification-service` — kreiranje konverzacije

JwtAuthenticationFilter u svakom servisu treba postaviti atribut `authUserId` iz `uid` claim-a (vidi `reservation-service/JwtAuthenticationFilter.java` kao referencu).

## Asinhrona komunikacija

`POST /api/reservations` vraća **HTTP 202 Accepted** sa `status=CREATED`. To znači "rezervacija primljena, Saga obrađuje". Klijent ne dobija odmah konačan status — Saga (payment + property) ga izračuna asinhrono, a klijent saznaje rezultat kroz:

1. **Notifikaciju** — `notification-service` emituje `POTVRDA_REZERVACIJE` ili `OTKAZANA_REZERVACIJA` na završetku Saga-e
2. **Polling** — frontend Dashboard polluje status pendingId-a svakih 3s dok ne dobije konačan status (max 5×)

Ovaj pattern eliminiše long-blocking HTTP request — backend se vraća odmah, korisnik dobija push update.
