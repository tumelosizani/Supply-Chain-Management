# Supply Chain Management (SCM) System

A full-stack Supply Chain Management (SCM) system featuring a **Spring Boot 4.2 / Spring Modulith** REST API on **Java 25** and a **React + TypeScript + Tailwind CSS** web frontend. It manages products, inventory reservations, customer orders, procurement & goods receiving, event-driven shipment provisioning, suppliers, and payments.

> See [FEATURES.md](FEATURES.md) for detailed implemented capabilities and [ROADMAP.md](ROADMAP.md) for planned work.

## Tech Stack

### Backend
- **Java 25** & **Gradle** (via wrapper)
- **Spring Boot 4.2** (Web, Data JPA, Validation, Virtual Threads)
- **Spring Modulith 2.2** for modular domain boundaries and event-driven fulfillment
- **PostgreSQL 17** (`compose.yaml`, exposed on port `6000`, with `spring-boot-docker-compose` dev support)
- **MapStruct 1.6** & **Lombok**
- **Springdoc OpenAPI** (`/swagger-ui.html`)
- **JUnit 5** & **Spring Modulith Test**

### Frontend (`frontend/`)
- **React 19** & **TypeScript**
- **Vite** & **pnpm**
- **Tailwind CSS v4**
- **React Router** & **Lucide Icons**

## Modules

Backend domain modules under `dev.dini.scms`:

- **Product (`product`):** Product catalog CRUD, partial `PATCH` updates, unit pricing, and paginated/sortable summaries.
- **Inventory (`inventory`):** Physical stock (`quantity`) and reservation (`quantityReserved`) tracking, availability checks (`available = quantity - quantityReserved`), reserve/confirm/release lifecycle, and stock replenishment.
- **Order (`order`):** Customer order lifecycle (`PENDING` → `PROCESSING` → `CONFIRMED`, `CANCELLED`, `SHIPPED`, `DELIVERED`), line-item management in `PENDING`, pricing calculation, and inventory reservation integration.
- **Procurement (`procurement`):** Purchase orders linked to suppliers and products, status lifecycle validation, and goods receiving (`POST /purchase-orders/{id}/receive`) with partial/full receipt tracking and automatic inventory replenishment.
- **Shipment (`shipment`):** Shipment tracking linked to customer orders, including event-driven shipment provisioning when orders are confirmed.
- **Supplier (`supplier`):** Supplier directory CRUD and listing.
- **Payment (`payment`):** Payment processing (`POST /payments/process`) and order payment lookup (`GET /payments/order/{orderId}`).
- **Seed (`seed`) & Util (`util`):** Development data seeding, `BaseEntity` auditing, shared `AddressDTO` / `PageResponse`, and centralized `GlobalExceptionHandler`.

### Frontend Views (`frontend/src/pages`)
- **Dashboard** (`DashboardPage`)
- **Products** (`ProductsPage`)
- **Inventory** (`InventoryPage`)
- **Orders** (`OrdersPage`)
- **Procurement** (`ProcurementPage`)
- **Shipments** (`ShipmentsPage`, filterable by `orderId`)
- **Suppliers** (`SuppliersPage`)

## Getting Started

### Prerequisites
- **JDK 25**
- **Docker** (for PostgreSQL via `compose.yaml`, or a local PostgreSQL instance on `localhost:6000`)
- **Node.js** & **pnpm** (for the frontend)

### 1. Clone the Repository
```bash
git clone https://github.com/tumelosizani/Supply-Chain-Management.git
cd Supply-Chain-Management
```

### 2. Start PostgreSQL
Spring Boot's Docker Compose integration starts `postgres:17` automatically during `bootRun`, or you can start it manually:
```bash
docker compose up -d
```

### 3. Run the Backend
Using the Gradle wrapper:
```bash
./gradlew bootRun
```
The API will be available at `http://localhost:8080` and Swagger UI at `http://localhost:8080/swagger-ui.html`.

Run tests and full build:
```bash
./gradlew test
./gradlew build
```

### 4. Run the Frontend
```bash
cd frontend
pnpm install
pnpm dev
```
Additional frontend scripts:
```bash
pnpm build   # Type-check and build for production
pnpm lint    # Run oxlint
```

## Future Enhancements

Planned work is tracked in [ROADMAP.md](ROADMAP.md), including:
- Completing the payment lifecycle (refunds, status history, UI integration)
- Inventory transaction ledger & low-stock alerts
- Multi-warehouse / location tracking
- Procurement back-orders and return-to-vendor (RTV) flows
- Richer product attributes (SKUs, variants, units of measure, images)
- Authentication & role-based access control (Spring Security)
- Database migrations (Flyway/Liquibase) and RFC 9457 `ProblemDetail` error responses
