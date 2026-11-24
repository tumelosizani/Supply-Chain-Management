# Roadmap

Planned and proposed work for the SCM system. Items are grouped by priority. See [FEATURES.md](FEATURES.md) for what already exists.

## P0 — Correctness fixes (do first)

These were known defects in current logic that could silently corrupt data. **All fixed.**

- [x] **Fix `releaseReservation` deducting physical stock.** `ReservationServiceImpl` now uses a `ReservationOp` enum: RESERVE/CONFIRM/RELEASE are distinct — release frees only `quantityReserved`, confirm deducts both, reserve holds against available.
- [x] **Fix inverted availability guard** in `CustomerOrderServiceImpl.addItemToOrder` / `updateOrderItem` — guards now negate `isInventoryAvailable` correctly.
- [x] **Correct `CustomerOrderNotFoundException` message** ("Purchase order not found…" → customer order).
- [x] **Null-safe `getAvailableQuantity`** — now treats null physical `quantity` (and `quantityReserved`) as zero.

## P1 — Quality & confidence

- [ ] **Test coverage** for the reserve/confirm/release triad and the order state machine (currently only a context-load test exists). These would have caught the P0 bugs.
- [ ] **CI pipeline** (`.github/workflows`) running `./gradlew build` on PRs — prerequisite for enabling required status checks in branch protection.
- [ ] Remove redundant double `repository.save(...)` calls and standardize the recalculate-and-save pattern across order mutations.
- [ ] Clean up `CustomerOrderStatus` (unused `NEW`, formatting) and the `order.service.calulation` package typo.
- [ ] Consistent, structured error responses (problem+json body instead of plain strings from `GlobalExceptionHandler`).

## P2 — Feature enhancements

Carried over and expanded from the README's "Future Enhancements":

- [x] **Procurement: goods receiving & inventory replenishment** — status transitions, goods receiving endpoint (`POST /purchase-orders/{id}/receive`), over-receiving validation, partial/full receipt tracking (`quantityReceived`), automatic physical stock increment in `Inventory`.
- [ ] Procurement: back-orders and return-to-vendor flows.
- [ ] Richer product attributes: variants, images, units of measure.
- [ ] Inventory: location tracking, transaction history / ledger, low-stock alerts.
- [ ] Shipment: carrier integration and shipment tracking.
- [ ] Complete the payment module: processing flow, refunds, status lifecycle.
- [ ] Order fulfillment: link confirmed orders → shipments automatically.

## P3 — Platform & operations

- [ ] Authentication & authorization (Spring Security).
- [ ] Environment-specific config and secrets management; move DB credentials/URL out of committed `application.yaml`.
- [ ] Database migrations (Flyway/Liquibase) instead of Hibernate `ddl-auto: update`.
- [ ] API pagination/filtering/sorting standardized across all list endpoints.
- [ ] Observability: metrics, health checks (Actuator), structured logging.
- [ ] Containerization (Dockerfile / compose) for the app + PostgreSQL.
