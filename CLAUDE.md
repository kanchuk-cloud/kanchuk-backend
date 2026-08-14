# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean install

# Run (requires .env or env vars set)
mvn spring-boot:run

# Run tests
mvn test

# Run a single test class
mvn test -Dtest=KanchukBackendApplicationTests

# Skip tests during build
mvn clean install -DskipTests
```

Environment variables (see `.env.example`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `PORT`, `CORS_ORIGINS`. Defaults in `application.yml` point to a Neon PostgreSQL instance.

## Architecture

**Stack**: Spring Boot 3.5.3, Java 21, PostgreSQL (Neon serverless), Flyway, JWT (jjwt 0.12.6), Lombok.

### Entity Hierarchy

All entities extend one of two abstract base classes:
- `BaseEntity` — UUID PK (auto-generated), `createdAt`, `updatedAt`
- `SoftDeleteEntity extends BaseEntity` — adds `deletedAt`; call `.softDelete()` to mark deleted, never issue a DB DELETE

### Admin Controller Pattern

Admin controllers extend `GenericAdminService` directly — there is no separate service layer for standard CRUD. `GenericAdminService` provides:
- `pageRequest(page, limit)` — 1-indexed page, sorted by `createdAt DESC`
- `buildMeta(page, limit)` — wraps `PageMeta` for response envelopes
- `findOrThrow(repo, id, entityName)` — throws `EntityNotFoundException` (caught by `GlobalExceptionHandler`)
- `applyPatch(entity, fields)` — reflection-based field setter; PUT endpoints receive `Map<String, Object>` and delegate here

All responses use `ApiResponse<T>` — a standard envelope with `success`, `message`, `data`, and optional `meta`.

### API Route Structure

```
POST /api/v1/auth/admin/login     — public, returns JWT
GET  /api/v1/public/**            — public
/api/v1/admin/**                  — requires ROLE_ADMIN JWT
```

JWT is stateless (no sessions). The `JwtAuthenticationFilter` validates the `Authorization: Bearer <token>` header, extracts the subject (admin email) and role claim, and sets the `SecurityContext`.

### Product Catalog Model

The catalog follows a Medusa-style hierarchy:

```
Product
  └── ProductVariant (SKU combinations, e.g. color+size)
        └── ProductListing (seller-specific price)
              └── InventoryLevel (quantity per StockLocation)
```

Products also have:
- `ProductOptionType` / `ProductOptionValue` — define the option axes (e.g. "Color", "Size")
- `VariantOptionValue` — join table linking variants to their option values
- `ProductImage` — ordered images per product
- `Facet` / `FacetValue` — filterable attributes linked via `product_facet_values`

### Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`. The schema uses `ddl-auto: validate` — Hibernate never modifies the schema; all changes go through versioned SQL migration files (`V{n}__description.sql`).

The test profile (`application-test.yml`) uses H2 in-memory with Flyway disabled and `ddl-auto: create-drop`.

### Security

- BCrypt password hashing
- `@EnableMethodSecurity` is active — method-level `@PreAuthorize` can be used
- CORS origins configured via `cors.allowed-origins` property (comma-separated)

### Jackson

`JacksonConfig` registers `Hibernate6Module` with `INITIALIZE_NULL_COLLECTIONS` — this prevents infinite recursion and serialization errors for lazy-loaded JPA proxies.
