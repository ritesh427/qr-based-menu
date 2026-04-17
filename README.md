# QR-Based Restaurant Menu & Ordering System

Full-stack restaurant ordering platform with QR menu access, JWT-secured admin APIs, Redis caching, WebSocket order updates, Kafka event publishing, H2 for local development, PostgreSQL for Docker, and Docker-based local orchestration.

## Stack

- Backend: Java 17, Spring Boot, Spring Security, JPA/Hibernate, H2/PostgreSQL, Redis, Kafka, WebSockets
- Frontend: React, Vite, Tailwind CSS
- Edge: Spring Cloud Gateway
- Deployment: Docker Compose

## Features

- Public QR menu per table without login
- Category browsing, search, veg filter, cart, checkout, and order tracking
- Admin login with JWT
- CRUD-ready category and menu item management
- Availability toggling and dynamic pricing support
- Real-time order updates through WebSockets
- Kafka event publishing for order lifecycle events
- Redis-backed menu caching
- API rate limiting
- Multi-restaurant-ready data model

## Project Layout

- [backend](C:/Users/rites/Documents/Codex/2026-04-18-build-a-full-stack-qr-based/backend)
- [gateway](C:/Users/rites/Documents/Codex/2026-04-18-build-a-full-stack-qr-based/gateway)
- [frontend](C:/Users/rites/Documents/Codex/2026-04-18-build-a-full-stack-qr-based/frontend)

## Seeded Demo Data

- Restaurant: `Saffron Table`
- QR token example: `saffron-table-t1`
- Admin username: `admin`
- Admin password: `admin123`

## Main API Groups

- Public menu + ordering: `/api/public/**`
- Admin auth: `/api/admin/auth/login`
- Admin categories: `/api/admin/categories`
- Admin menu items + QR codes: `/api/admin/menu-items`
- Admin orders + status updates: `/api/admin/orders`

## Run With Docker

```bash
docker compose up --build
```

Open:

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`

## Run Backend Locally Without SQL

The backend now defaults to the `local` Spring profile, which uses an in-memory H2 database.

```bash
mvn spring-boot:run
```

Or if you are using the IntelliJ-bundled Maven found on this machine:

```bash
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run
```

Useful local URLs:

- Backend API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui.html`
- H2 Console: `http://localhost:8081/h2-console`

The Docker stack still uses PostgreSQL through the `docker` profile.

## Local Frontend Run

The frontend now talks directly to the backend on `http://localhost:8081` by default, so you do not need the gateway for basic local development.

```bash
npm.cmd install
npm.cmd run dev
```

Open:

- Frontend: `http://localhost:5173`

## Local Gateway Run

If you want the gateway locally too, run it with the `local` profile so it routes to `localhost` instead of Docker service names:

```bash
"C:\Program Files\JetBrains\IntelliJ IDEA Community Edition 2025.2.6.1\plugins\maven\lib\maven3\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.profiles=local
```

## Architecture Notes

- The backend follows layered structure: controller -> service -> repository
- DTOs isolate HTTP contracts from JPA entities
- `DiningTable` is used instead of SQL-reserved `Table`
- WebSocket broadcasts go to `/topic/orders/{restaurantId}` and `/topic/orders/table/{qrToken}`
- Kafka publishing is controlled with `app.kafka.enabled`
- Redis cache key `menuByQr` keeps QR menu fetches fast

## Recommended Next Enhancements

- Add payment provider integration with Razorpay or Stripe
- Split menu/order/auth into separate services behind the gateway
- Add ratings, reviews, and kitchen analytics
- Replace the simple in-memory WebSocket broker with a distributed broker for scale
