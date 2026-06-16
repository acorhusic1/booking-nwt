# Booking-NWT

Distribuirana platforma za rezervaciju smještaja, izgrađena kroz Spring Boot microservise i React SPA. Saga choreography preko RabbitMQ koordinira rezervaciju → naplatu → potvrdu kalendara, sa kompenzacijama na svakom koraku.

## Tim

| Član | Uloga |
|---|---|
| Benjamin Hadžihasanović | Backend (reservation-service, payment-service), frontend Dashboard |
| Emir | Backend (property-service), Host Dashboard, kalendar |
| Kenan | Notification-service, Saga listeneri, Messages |
| Ahmed Čorhusić | API Gateway, user-service, Auth, Admin Dashboard |

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
        ├──► property-service      (smjestaji, kalendar, cijene, recenzije, wishlist, mapa)
        ├──► reservation-service   (rezervacije, politike otkazivanja, promo kodovi, problem-reports)
        ├──► payment-service       (wallet, Stripe checkout, host payout)
        ├──► notification-service  (notifikacije, poruke, Saga listener)
        ├──► analytics-service     (reporting po hostu/mjesecu)
        └──► system-events-service (audit log — kostur, preskočeno po dogovoru)

Saga (RabbitMQ exchange = booking.events):
  RESERVATION_CREATED   ─► payment.queue           (naplata sa walleta)
                        ─► property.queue          (saga link / provjera)
                        ─► notification.queue      (host notif: nova rezervacija)
  PAYMENT_COMPLETED     ─► reservation.queue       (status → CONFIRMED)
                        ─► notification.queue      (gost notif: potvrda)
  PAYMENT_FAILED        ─► reservation.queue       (status → CANCELLED, kompenzacija)
                        ─► notification.queue      (gost + host notif: otkazano)
  RESERVATION_CANCELLED ─► payment.cancellations   (refund na wallet po politici otkazivanja)
                        ─► property.cancellations  (oslobađa kalendar)
                        ─► notification.queue      (host notif: otkazano)
  RESERVATION_COMPLETED ─► payment.completions     (ISPLATA HOSTU nakon boravka, −10% provizija)
  RESERVATION_REMINDER  ─► notification.queue      (podsjetnik dan prije check-ina)
  REVIEW_REQUEST        ─► notification.queue      (zahtjev za recenziju nakon boravka)
  PROBLEM_REPORTED      ─► notification.queue      (host notif: prijavljen problem)
```

> **Napomena o konzistentnosti:** `ReservationCreatedEvent` se objavljuje tek **nakon commita**
> transakcije (TransactionSynchronization afterCommit). Saga zna završiti za ~20 ms, pa bi event
> poslan unutar transakcije stigao do payment-servisa prije nego što je rezervacija uopšte upisana
> u bazu — `PAYMENT_COMPLETED` listener je ne bi našao i rezervacija bi zauvijek ostala `CREATED`.
> Listener dodatno ima retry (5 pokušaja) kao osigurač.

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

### Login podaci (demo seed korisnici)

| Uloga | Email | Šifra |
|---|---|---|
| ADMIN | admin@bookingnwt.com | password123 |
| HOST | emir.d@email.com | password123 |
| HOST | ahmed.c@email.com | password123 |
| GUEST | benjamin.h@email.com | password123 |
| GUEST | kenan.a@email.com | password123 |
| GUEST | marija.m@email.com | password123 |

### Zaustavljanje

```bash
docker compose down              # zadržava DB volume
docker compose down -v           # briše i DB volume (čist start + svjež seed)
```

### Restart pojedinačnog servisa nakon code change-a

```bash
# Java servis
docker compose build reservation-service
docker compose up -d reservation-service

# Frontend
docker compose build booking-app
docker compose up -d booking-app
```

## Lokalni development (bez Dockera)

Backend servisi se mogu pokrenuti direktno iz IDE-a — svaki ima `application.properties` koji opciono čita config sa `config-server:8888`.

```bash
cd booking-app
npm install
npm run dev         # SPA na http://localhost:3000 sa proxy → :8080
```

## Testovi

### Backend (JUnit 5 + Mockito + MockMvc)

```bash
cd reservation-service && mvnw clean test    # 109 testova
cd property-service    && mvnw clean test    #  69 testova
cd payment-service     && mvnw clean test    #  80 testova
cd notification-service&& mvnw clean test    #  63 testa
cd user-service        && mvnw clean test    #  69 testova*
```

\* `UserServiceApplicationTests` i `AuthControllerIntegrationTest` zahtijevaju RSA ključeve iz
`.env`-a (JwtProvider) pa van Docker okruženja padaju na context-loadu — unit/controller testovi prolaze.

> Napomena (Windows): ako testovi padnu sa `ClassNotFoundException` bez paketa u imenu klase,
> radi se o pokvarenom inkrementalnom buildu — pokrenuti `mvnw clean test`.

### Frontend (Vitest + React Testing Library)

Svi testovi su organizovani u [`booking-app/src/test/`](booking-app/src/test/) sa istom strukturom direktorija kao izvorni kod.

```bash
cd booking-app
npm test            # one-shot (CI) — 134 testa / 29 fajlova
npm run test:watch  # watch mode (dev)
npm run test:ui     # vizualni UI u browser-u
```

## Funkcionalni zahtjevi (F1–F19)

| ID | Naziv | Status | Napomena |
|---|---|---|---|
| F1 | Napredna pretraga sa filtriranjem | ✅ | Grad/država, period boravka (backend `/search` + provjera overlapa s rezervacijama), tip smještaja, raspon cijena, min. ocjena, sadržaji (WiFi/parking/klima/bazen), kapacitet. Sortiranje: cijena, ocjena, **udaljenost** (geolokacija browsera), ime, kapacitet. Filteri rade nad cijelim skupom (klijentska paginacija 12/str). |
| F2 | Kreiranje i upravljanje objektima | ✅ | Host kreira objekat sa slikama, tipom, cijenom (obavezna pri kreiranju), koordinatama (LocationPicker), sadržajima i kućnim pravilima — pravila i sadržaji se prikazuju gostu na detalju. **Moderacija enforsovana**: javna lista, pretraga i mapa prikazuju samo APPROVED objekte. Model: 1 property = 1 smještajna jedinica (pojednostavljenje dokumentovanog ERD-a). |
| F3 | Kalendar dostupnosti | ✅ | Vizualni kalendar za hosta (blokiranje) i gosta (zauzeti termini iz blokova + rezervacija). Min/max noćenja iz cjenovnika + sezonska pravila — enforsovano i na backendu. |
| F4 | Dinamičko određivanje cijena | ✅ | Base/weekend cijena, long-stay popust, sezonski modifikatori. **Server-side kalkulacija je autoritativna** — klijentska cijena se ignoriše (sprječava manipulaciju). Breakdown prikazan gostu prije potvrde; cijena/noć vidljiva na kartici, detalju i formi. |
| F5 | Proces rezervacije | ✅ | Sinhrona provjera dostupnosti + kapaciteta (circuit breaker, fail-closed), lokalni overlap check, Saga naplata. Statusi: CREATED → CONFIRMED (tek nakon **uspješne naplate**) → ACTIVE → COMPLETED (auto-scheduler). Event se objavljuje after-commit (vidi napomenu gore). Instant booking model. |
| F6 | Politike otkazivanja | ✅ | 3 tier-a: pun refund (van `freeCancelDays`), djelimičan (`partialRefundPct%`), bez refunda. Politika se **automatski vezuje za property** pri kreiranju. UI prikazuje očekivani povrat prije potvrde otkazivanja; refund ide async na wallet. |
| F7 | Recenzije | ✅ | 5 kategorija + komentar + host reply + prosjek. **Server-side validacija**: samo gost sa završenom rezervacijom, jedna recenzija po rezervaciji, host odgovara samo na recenzije svog objekta. |
| F8 | Poruke gost ↔ host | ✅ | Konverzacije vezane za property/rezervaciju, read-status, **notifikacija primaocu o novoj poruci**. |
| F9 | Notifikacije | ✅ | Nova rezervacija (host), potvrda/otkaz (gost+host), nova poruka, **podsjetnik dan prije check-ina**, **zahtjev za recenziju nakon boravka**, prijava problema (host). Read/unread, bell badge (polling 15s). |
| F10 | Liste želja | ✅ | Više listi po gostu, picker za izbor liste, srce na kartici. |
| F11 | Statistike za domaćine | ✅ | Stopa popunjenosti (noći isječene na granicu godine), prihod po mjesecu (chart), prosječna ocjena, najpopularniji objekti, **broj pregleda oglasa**, filter po godini/objektu. Admin ima "Pregled platforme" sa živim podacima (promet, provizija, rezervacije po statusu...). |
| F12 | Preporuke iz historije | ✅ | "Preporučeno za vas" na Dashboardu — rangiranje po gradovima iz prošlih rezervacija + budžetu po noći (content-based, priprema za ML iz dokumentacije). |
| F13 | Promotivni kodovi | ✅ | Procenat/fiksno, period važenja, min noćenja, max korištenja. **Validacija i obračun na backendu** (frontend validira radi UX-a); kreiranje ograničeno na HOST/ADMIN. |
| F14 | Audit log | ⏭ Preskočeno | Po dogovoru s profesorom. `system-events-service` postoji kao kostur (AuditLog CRUD + filteri), notifikacije ga koriste preko Feign-a. |
| F15 | Sezonska pravila | ✅ | Cjenovni modifikator po sezoni, min noćenja u sezoni — **automatski primijenjeno pri kreiranju svake rezervacije na backendu** (+ UI upozorenja). Sezonske politike otkazivanja izostavljene (dogovoreno kao nebitno za obim projekta). |
| F16 | Verifikacija domaćina | ✅ | Host šalje zahtjev, admin approve/reject, objava objekta blokirana bez APPROVED verifikacije (fail-closed Feign provjera). Badge "🛡️ Verifikovan domaćin" vidljiv gostima na stranici objekta (endpoint vraća samo `{verified}` — bez podataka o dokumentu). |
| F17 | Prijava problema | ✅ | Kategorija + opis, statusi REPORTED → IN_PROGRESS → RESOLVED/CLOSED, host i admin pregled, **notifikacija hostu sa rokom za odgovor**. |
| F18 | Interaktivna mapa | ✅ | OpenStreetMap (react-leaflet). **Markeri prikazuju cijenu** ("85 KM" pill), popup sa fotografijom, ocjenom, cijenom i linkom. **Dinamičko učitavanje po viewportu** — `GET /api/properties/in-bounds` na svako pomicanje/zoom. LocationPicker za hosta. |
| F19 | Plaćanje i wallet | ✅ | Wallet naplata kroz Sagu, Stripe Checkout dopuna, refund po politici otkazivanja. **Isplata hostu tek nakon završenog boravka** (RESERVATION_COMPLETED, −10% provizija platforme, idempotentno). Sve operacije upisuju `WalletTransaction` — kompletna historija novčanika. |

**Sažetak:** 18 ✅ kompletno · 1 ⏭ namjerno preskočeno (F14, po dogovoru s profesorom).

## Struktura repozitorija

```
booking-nwt/
├── booking-app/             # React SPA
│   ├── src/
│   │   ├── api/             # axios klijenti
│   │   ├── components/      # Re-usable UI
│   │   ├── store/           # Zustand store-ovi (auth)
│   │   ├── utils/           # pricing kalkulator, date helperi (lokalna zona!)
│   │   └── test/            # Svi unit testovi (mirror src/)
│   └── package.json
├── user-service/            # JWT auth, profili, verifikacije
├── property-service/        # Smjestaji, kalendar, cijene, sezone, recenzije, wishlist
├── reservation-service/     # Rezervacije, politike otkazivanja, promo, problem reports
├── payment-service/         # Wallet, Stripe, host payout
├── notification-service/    # Notifikacije, poruke + Saga listener
├── analytics-service/       # Reporting
├── system-events-service/   # Audit log (kostur)
├── api-gateway/             # Spring Cloud Gateway
├── config-server/           # Spring Cloud Config
├── eureka-server/           # Service discovery
└── docker-compose.yml
```

## Tehnologije

**Backend**
- Java 17/21, Spring Boot 3.x, Spring Cloud
- JPA / Hibernate, MySQL 8
- Spring Security + JWT (RSA, `uid` claim)
- RabbitMQ (Saga choreography, after-commit publishing)
- Resilience4j (circuit breaker, fail-closed provjere)
- OpenFeign (sync inter-service pozivi + JWT propagacija)

**Frontend**
- React 18 + Vite
- Zustand (state), react-hook-form + zod (forme)
- react-router 6, axios (interceptori: JWT + silent refresh)
- react-leaflet (mapa)
- Vitest + React Testing Library

**DevOps**
- Docker Compose
- Multi-stage Dockerfile builds
- Healthchecks + `depends_on: condition: service_healthy`

## Sigurnost

- JWT (RSA potpis) nosi `uid` claim; `JwtAuthenticationFilter` u servisima postavlja `authUserId` request atribut.
- **reservation-service**: `guestId` se uzima iz tokena (ne iz body-a); otkazivanje/čitanje/status rezervacije dozvoljeni samo učesnicima (gost/host te rezervacije) ili adminu; JSON Patch i brisanje samo ADMIN; promo CRUD samo HOST/ADMIN.
- **property-service**: `hostId` pri kreiranju objekta dolazi iz tokena; izmjena/brisanje objekta samo vlasnik ili admin; odgovor na recenziju samo host tog objekta; recenzije validirane kroz Feign poziv prema reservation-service (fail-closed).
- **Server-side cijena**: ukupna cijena rezervacije se računa na backendu (cjenovnik + sezone + promo) — klijentski iznos se ignoriše.
- Preostalo za dosljednost (poznato ograničenje): payment-service i notification-service još vjeruju ID-evima iz body-a za wallet/konverzacije.

## Asinhrona komunikacija

`POST /api/reservations` vraća **HTTP 202 Accepted** sa `status=CREATED` — "rezervacija primljena, Saga obrađuje". Konačan status (CONFIRMED/CANCELLED) se izračunava asinhrono:

1. **Notifikacija** — `notification-service` kreira `POTVRDA_REZERVACIJE` ili `OTKAZANA_REZERVACIJA` na završetku Sage
2. **Polling** — Dashboard polluje status `pendingId`-a svakih 1.5 s (max 10×) i prikazuje toast s ishodom

Ključni detalji ispravnosti:
- `ReservationCreatedEvent` se objavljuje **after-commit** — eliminisan race gdje Saga završi prije nego što je rezervacija vidljiva u bazi
- `CANCELLED` je terminalan u saga state-machine-u — zakašnjeli "success" event ne može pregaziti kompenzaciju
- Refund i payout listeneri su idempotentni (dupli eventi ne dupliraju novac)
  
Video demo - https://drive.google.com/file/d/1KoCaP5RUQi_DSXsOfX08GOcKbzFIplR2/view
