# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

A Spring Boot 3.4 / Java 21 Supply Chain Management REST API (`dev.dini.scms`, artifact `scms`). Manages products, inventory, customer orders, procurement, shipments, suppliers, and payments backed by PostgreSQL.

## Commands

Uses the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows).

```bash
./gradlew build          # compile + run tests
./gradlew bootRun        # run the app (serves on http://localhost:8080)
./gradlew test           # run all tests
./gradlew test --tests "dev.dini.scms.ScmsApplicationTests"   # run a single test class
./gradlew test --tests "*.SomeTest.someMethod"                # run a single test method
```

Swagger UI (springdoc-openapi) is available at `/swagger-ui.html` when running.

## Database

- Requires a PostgreSQL instance at `jdbc:postgresql://localhost:6000/postgres` (see `src/main/resources/application.yaml`).
- `ddl-auto: update` — Hibernate manages the schema; `spring.sql.init.mode: never` so `data.sql` is **not** auto-run.
- Virtual threads are enabled (`spring.threads.virtual.enabled: true`).
- `docs/erd.puml` / `docs/erd.png` hold the entity-relationship diagram.

## Architecture

### Module layout
Each business domain is a self-contained package under `dev.dini.scms.<module>` following the same layered structure: `controller/` → `service/` → `repository/` → `entity/`, with `dto/` (request/response) and `mapper/` (MapStruct). Modules: `product`, `inventory`, `order`, `procurement`, `shipment`, `supplier`, `payment`. The `util` package holds cross-cutting pieces: `BaseEntity`, `InventoryUtil`, shared DTOs (`AddressDTO`, `PageResponse`), and all exceptions.

Note: README omits the `payment` module — it exists in code.

### Conventions (follow these when adding code)
- **Entities** extend `util.BaseEntity`, which supplies `id` (IDENTITY), `createdAt` (`@CreationTimestamp`), and `updatedAt` (`@UpdateTimestamp`). Never map these three fields manually.
- **DTOs are Java records** — access fields as method calls (`dto.items()`, `item.productId()`), not getters.
- **MapStruct mappers** use `componentModel = SPRING`. `toEntity` mappings must `@Mapping(target=..., ignore=true)` for `id`/`createdAt`/`updatedAt` and relationship collections. Partial updates use `partialUpdate(dto, @MappingTarget entity)` with `NullValuePropertyMappingStrategy.IGNORE`.
- **Services** split into an interface + `*Impl`, use constructor injection via Lombok `@RequiredArgsConstructor`, and log with `@Slf4j`. Larger domains further decompose responsibilities into collaborators (e.g. order has `CustomerOrderValidator`, `OrderCalculationService`; inventory has `InventoryAvailabilityService`, `ReservationService`, `InventoryCalculationService`).
- **Controllers** are thin `@RestController`s that delegate to services, use `@Valid` on request bodies, and return `ResponseEntity`. `PATCH` is used for partial updates.
- **Exceptions**: throw the domain `*NotFoundException` / `Insufficient*` / `*AlreadyExists` types from `util.exception`. `GlobalExceptionHandler` (`@ControllerAdvice`) maps them to HTTP statuses — register any new domain exception there.

### Order → Inventory flow (the core cross-module logic)
Orders drive inventory through a reservation model, not direct deduction:
- **Inventory** tracks `quantity` (physical) and `quantityReserved`. *Available* = `quantity - quantityReserved` (`InventoryCalculationService.getAvailableQuantity`).
- **Order lifecycle** (`CustomerOrderStatus`): `PENDING` → `PROCESSING` → `CONFIRMED`, or `CANCELLED`. `SHIPPED`/`DELIVERED`/`NEW` also exist. Structural edits (items, address) are allowed **only in PENDING**.
- **Reservation transitions** (`ReservationService`): creating/adding items **reserves** (increments `quantityReserved`); `confirmOrder` **confirms** (decrements both `quantity` and `quantityReserved` — actual stock leaves); `cancelOrder`/delete/remove-item **releases**. Reservation adjustments on item updates are driven by the quantity delta.
- Reservation logic runs under `@Transactional`; batch reservation groups duplicate product IDs before reserving.

When modifying order or inventory logic, preserve these invariants — the reserve/confirm/release triad must stay balanced or stock counts drift.
