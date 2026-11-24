# Features

Current capabilities of the Supply Chain Management (SCM) system. This reflects what is **implemented in the codebase today** (`dev.dini.scms` backend + `frontend/`), not planned work — see [ROADMAP.md](ROADMAP.md) for what's next.

## Platform & Infrastructure

- **Spring Boot 4.2** REST API on **Java 25**, built with Gradle.
- **Spring Modulith 2.2** enforcing domain module boundaries (`ModularityTests`) and event-driven cross-module integration.
- **PostgreSQL 17** persistence via Spring Data JPA / Hibernate, with `compose.yaml` and `spring-boot-docker-compose` dev support.
- **MapStruct 1.6** for entity ↔ DTO mapping; DTOs are Java records.
- **Bean Validation (`@Valid`)** on request payloads.
- **Virtual threads** enabled (`spring.threads.virtual.enabled: true`).
- **OpenAPI / Swagger UI (`springdoc`)** exposed at `/swagger-ui.html`.
- **Centralized error handling** via `GlobalExceptionHandler` (`@ControllerAdvice`) mapping domain exceptions to HTTP statuses.
- **Auditing** on every entity through `BaseEntity` (`id`, `createdAt`, `updatedAt`).
- **CI/CD & Security**: GitHub Actions CI ([ci.yml](.github/workflows/ci.yml)) running backend `./gradlew build` against PostgreSQL 17 and frontend `pnpm lint` / build, plus CodeQL ([codeql.yml](.github/workflows/codeql.yml)) and Dependabot.
- **Testing**: Unit, integration, and modularity tests (`CustomerOrderServiceImplTest`, `InventoryServiceImplTest`, `OrderFulfillmentIntegrationTest`, `PurchaseOrderServiceImplTest`, `PurchaseOrderItemServiceImplTest`, `ShipmentServiceImplTest`, `DataSeederTest`, `ModularityTests`, `ScmsApplicationTests`).

## Backend Modules

### Product
- Create, update (partial `PATCH`), delete, and fetch products by ID.
- Paginated, sortable product summaries (`GET /products/summaries` via `Pageable` → `PageResponse`).
- Products carry a `unitPrice` used when pricing order items.

### Inventory
- Create, update, delete, and query inventory records, keyed by product.
- Tracks physical `quantity` and `quantityReserved`.
- Availability model: **available = quantity − quantityReserved** (`InventoryCalculationService`).
- Availability checks: single-item (`isInventoryAvailable`) and batch (`InventoryAvailabilityService.ensureSufficientInventory`).
- Reservation lifecycle (`ReservationService`): reserve, confirm, release, and batch-reserve (groups duplicate product IDs).
- Stock replenishment (`addStock`): auto-initializes inventory record if absent and increments physical stock.

### Order (Customer Orders)
- Create orders with line items; total cost calculated on save (`OrderCalculationService`).
- Item management (add / update / remove) — restricted to `PENDING` orders.
- Order state machine (`CustomerOrderStatus`): `PENDING → PROCESSING → CONFIRMED`, plus `CANCELLED`; `SHIPPED` / `DELIVERED` states exist.
- Inventory integration: reserves stock on order/item creation, confirms (deducts) on confirmation, releases on cancel/delete/remove.
- Validation of items and product pricing before an order is accepted (`CustomerOrderValidator`).
- **Event-driven fulfillment**: confirming an order publishes an event that automatically provisions a shipment.
- Fetch single order, fetch all orders.

### Procurement
- Create, update, retrieve, and delete purchase orders and purchase-order items linked to suppliers and products.
- Status lifecycle (`PurchaseOrderStatus`): `PENDING` → `APPROVED` → `SHIPPED` → `DELIVERED` → `PARTIALLY_RECEIVED` / `FULLY_RECEIVED`, plus `CANCELLED` and `RETURNED`.
- State transition validation preventing invalid changes (e.g. from cancelled or terminal states).
- **Goods Receiving Workflow (`POST /purchase-orders/{id}/receive`)**:
  - Receive line items partially or in full by item ID or product ID.
  - Automatically replenishes physical stock in `Inventory`.
  - Over-receiving protection (guards against receiving more than ordered).
  - Automatically transitions PO status to `PARTIALLY_RECEIVED` or `FULLY_RECEIVED`.
  - Prevents deletion or invalid reductions of purchase orders/items that have already received stock.

### Shipment
- Create, update, delete, and retrieve shipment records linked to customer orders.
- Event-driven shipment provisioning when customer orders are confirmed (`OrderFulfillmentIntegrationTest`).

### Supplier
- Create, retrieve, update, delete, and list suppliers.

### Payment
- Payment processing (`POST /payments/process`) and order payment lookup (`GET /payments/order/{orderId}`).

### Seed (`seed`) & Cross-cutting Utilities (`util`)
- `DataSeeder` isolated in the `seed` package for development data initialization.
- `BaseEntity` — shared ID + audit timestamps.
- `Address` / `AddressDTO` — reusable address value type.
- `PageResponse` — pagination envelope for list endpoints.
- `InventoryUtil` — inventory lookup/existence helpers.
- Domain exception hierarchy (`*NotFoundException`, `InsufficientInventoryException`, `*AlreadyExistsException`, `InvalidPurchaseOrderStateException`, `PurchaseOrderReceivingException`).

## Frontend (`frontend/`)

- **React 19 + TypeScript + Vite + Tailwind CSS v4** single-page application (`react-router-dom`).
- Views:
  - **Dashboard** (`/`) — `DashboardPage`
  - **Products** (`/products`) — `ProductsPage`
  - **Inventory** (`/inventory`) — `InventoryPage`
  - **Orders** (`/orders`) — `OrdersPage`
  - **Shipments** (`/shipments`) — `ShipmentsPage` (supports filtering by `orderId` query param from `OrdersPage`)
  - **Procurement** (`/procurement`) — `ProcurementPage`
  - **Suppliers** (`/suppliers`) — `SuppliersPage`
- Shared `Layout` and `StatusBadge` components, in-flight guards on row actions, and modal validation.
