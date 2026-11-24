# Features

Current capabilities of the Supply Chain Management (SCM) system. This reflects what is **implemented in the codebase today** (`dev.dini.scms`), not planned work — see [ROADMAP.md](ROADMAP.md) for what's next.

## Platform

- Spring Boot 3.4 REST API on Java 21, built with Gradle.
- PostgreSQL persistence via Spring Data JPA / Hibernate (`ddl-auto: update`).
- MapStruct for entity ↔ DTO mapping; DTOs are Java records.
- Bean Validation (`@Valid`) on request payloads.
- Virtual threads enabled.
- OpenAPI / Swagger UI (`springdoc`) exposed at `/swagger-ui.html`.
- Centralized error handling via `GlobalExceptionHandler` (`@ControllerAdvice`) mapping domain exceptions to HTTP statuses.
- Auditing on every entity through `BaseEntity` (`id`, `createdAt`, `updatedAt`).

## Modules

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

### Supplier
- Create, retrieve, update, delete, and list suppliers.

### Payment
- Payment entities, DTOs, enums, and service layer (present in code; not documented in README).

## Cross-cutting utilities (`util`)
- `BaseEntity` — shared ID + audit timestamps.
- `Address` / `AddressDTO` — reusable address value type.
- `PageResponse` — pagination envelope for list endpoints.
- `InventoryUtil` — inventory lookup/existence helpers.
- Domain exception hierarchy (`*NotFoundException`, `InsufficientInventoryException`, `*AlreadyExistsException`, `InvalidPurchaseOrderStateException`, `PurchaseOrderReceivingException`).
