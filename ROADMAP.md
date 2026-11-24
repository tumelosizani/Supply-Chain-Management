# Roadmap

Planned and proposed work for the SCM system. Items are grouped by priority. See [FEATURES.md](FEATURES.md) for what already exists.

## P0 — Correctness fixes (do first)

These were known defects in current logic that could silently corrupt data. **All fixed.**

- [x] **Fix `releaseReservation` deducting physical stock.** `ReservationServiceImpl` now uses a `ReservationOp` enum: RESERVE/CONFIRM/RELEASE are distinct — release frees only `quantityReserved`, confirm deducts both, reserve holds against available.
- [x] **Fix inverted availability guard** in `CustomerOrderServiceImpl.addItemToOrder` / `updateOrderItem` — guards now negate `isInventoryAvailable` correctly.
- [x] **Correct `CustomerOrderNotFoundException` message** ("Purchase order not found…" → customer order).
- [x] **Null-safe `getAvailableQuantity`** — now treats null physical `quantity` (and `quantityReserved`) as zero.

## P1 — Quality & confidence

- [x] **Test coverage** — unit, integration, and Spring Modulith boundary tests added across `order`, `inventory`, `procurement`, `shipment`, `seed`, and `ModularityTests`.
- [x] **CI pipeline** — GitHub Actions CI ([ci.yml](.github/workflows/ci.yml)) running `./gradlew build` against PostgreSQL 17 and frontend `pnpm lint` / build, plus CodeQL ([codeql.yml](.github/workflows/codeql.yml)).
- [ ] Remove redundant double `repository.save(...)` calls and standardize the recalculate-and-save pattern across order mutations.
- [ ] Clean up `CustomerOrderStatus` (unused `NEW`, formatting) and the `order.service.calulation` package typo.
- [ ] Consistent, structured error responses (`application/problem+json` `ProblemDetail` instead of plain strings from `GlobalExceptionHandler`).
- [ ] Expand frontend automated test coverage (e.g., Vitest / Testing Library component and flow tests).

## P2 — Feature enhancements

- [x] **Procurement: goods receiving & inventory replenishment** — status transitions, goods receiving endpoint (`POST /purchase-orders/{id}/receive`), over-receiving validation, partial/full receipt tracking (`quantityReceived`), automatic physical stock increment in `Inventory`.
- [x] **Order fulfillment: link confirmed orders → shipments automatically** — event-driven shipment provisioning on order confirmation (`OrderFulfillmentIntegrationTest`).
- [ ] **Complete the payment lifecycle & UI integration**: refunds, payment status history, event-driven order confirmation on payment, and a `PaymentsPage` / order payment flow in `frontend/`.
- [ ] **Inventory audit ledger & low-stock alerts**: immutable `InventoryTransaction` history, configurable `reorderLevel` thresholds, and dashboard low-stock alerts with one-click PO creation.
- [ ] **Procurement: back-orders and return-to-vendor (RTV) flows**.
- [ ] **Inventory: multi-warehouse / location tracking** and inter-warehouse stock transfers.
- [ ] **Richer product attributes**: SKUs/barcodes, variants, images, units of measure.
- [ ] **Shipment**: carrier integration and external tracking status updates.

## P3 — Platform & operations

- [ ] Authentication & authorization (Spring Security + JWT and frontend login/RBAC).
- [ ] Environment-specific config and secrets management (`dev`/`prod` profiles; move DB credentials/URL out of committed `application.yaml`).
- [ ] Database migrations (Flyway/Liquibase) instead of Hibernate `ddl-auto: create-drop`.
- [ ] API pagination/filtering/sorting standardized across all list endpoints.
- [ ] Observability: metrics, health checks (Spring Boot Actuator), structured logging.
- [x] **Database containerization** (`compose.yaml` for PostgreSQL 17 with `spring-boot-docker-compose` support).
- [ ] Full-stack containerization (Dockerfile for backend + frontend in `compose.yaml`).
