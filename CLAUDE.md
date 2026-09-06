# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean install

# Run (requires env vars or uses application.yml defaults pointing to Neon)
mvn spring-boot:run

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=DeliveryCheckTest

# Skip tests during build
mvn clean install -DskipTests
```

Environment variables (see `.env.example`): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION_MS`, `PORT`, `CORS_ORIGINS`. Defaults in `application.yml` point to a Neon PostgreSQL instance.

## Architecture

**Stack**: Spring Boot 3.5.3, Java 21, PostgreSQL (Neon serverless), Flyway, JWT (jjwt 0.12.6), Lombok.

### No Service Layer

There is intentionally no service layer. Controllers inject Spring Data repositories directly. The only exceptions are:

- `GenericAdminService` — abstract utility base extended by all controllers (both admin and public). Provides:
  - `pageRequest(page, limit)` — 1-indexed page, sorted by `createdAt DESC`
  - `buildMeta(Page<?> pg, int page, int limit)` — builds the `PageMeta` response object
  - `findOrThrow(repo, id, entityName)` — throws `EntityNotFoundException` (caught by `GlobalExceptionHandler`)
  - `applyPatch(entity, fields)` — reflection-based field setter for PUT endpoints that receive `Map<String, Object>`. Silently swallows any reflection error via `catch (Exception ignored)`.

- `AuthService` — a real `@Service` bean (the only one). Handles the admin login flow: look up `AdminUser` by email, BCrypt-verify the password, generate a JWT via `JwtUtil`.

**`applyPatch` limitation**: it coerces scalars (String, Boolean, Integer, Long, BigDecimal, UUID, OffsetDateTime, List) but cannot set `@ManyToOne` relations by ID. To update a relation (e.g. `category`), fetch the target entity from its repository first, then call the setter directly.

**Exception**: `PublicDeliveryController` does not extend `GenericAdminService` — it uses `@RequiredArgsConstructor` directly and performs its own logic without the base class helpers.

### Request Body Pattern

Write endpoints (POST, PUT, PATCH) typically accept raw `Map<String, Object>` rather than typed DTO classes. Only auth (`LoginRequest`) has a dedicated request DTO. This means no `@Valid` annotations on most write endpoints — validation is done manually inside the handler.

### Response Envelope

All responses use `ApiResponse<T>` — factory methods: `ApiResponse.ok(data)`, `ApiResponse.ok(data, meta)`, `ApiResponse.created(data)`, `ApiResponse.deleted()`, `ApiResponse.error(message)`.

### API Route Structure

```
POST /api/v1/auth/admin/login     — public, returns JWT
GET  /api/v1/public/**            — public (no auth)
     /uploads/**                  — public (static file serving)
/api/v1/admin/**                  — requires ROLE_ADMIN JWT
```

JWT is stateless (no sessions). `JwtAuthenticationFilter` validates `Authorization: Bearer <token>`, extracts subject (admin email) and `role` claim, and sets the `SecurityContext`. `JwtUtil` generates and validates tokens using HMAC-SHA with the configured secret.

### Entity Hierarchy

All entities extend one of two abstract base classes:
- `BaseEntity` — UUID PK (auto-generated via `GenerationType.UUID`), `createdAt`, `updatedAt`
- `SoftDeleteEntity extends BaseEntity` — adds `deletedAt`; call `.softDelete()` to mark deleted, never issue a DB DELETE

Two entities use composite keys with `@EmbeddedId`:
- `VariantOptionValue` — links `ProductVariant` ↔ `ProductOptionValue`
- `StockLocationZone` — links `StockLocation` ↔ `DeliveryZone` with a `priority` column

### Product Catalog Model

The catalog follows a Medusa-style hierarchy:

```
Product
  └── ProductVariant (SKU combinations, e.g. color+size)
        └── ProductListing (seller-specific price)
              └── InventoryLevel (quantity per StockLocation)
```

Products also have:
- `Category` / `SubCategory` / `Fabric` / `Designer` / `TaxCategory` — `@ManyToOne` lazy relations
- `ProductOptionType` / `ProductOptionValue` — define the option axes (e.g. "Color", "Size")
- `VariantOptionValue` — composite-key join table linking variants to their option values
- `ProductImage` — ordered images per product (also stores optional `variantId` for variant-specific images)
- `Facet` / `FacetValue` — filterable attributes linked via `product_facet_values`
- `SizeChart` — size guide entity, separate from the product hierarchy

**Computed fields on Product** (`@Formula`): `minPrice` and `compareAtPrice` are read-only SQL subqueries executed at fetch time. They are never persisted and cannot be set.

**JSONB fields on Product** (`@JdbcTypeCode(SqlTypes.JSON)`): `occasions`, `tags`, `careInstructions`, `colorFamilies` (all `List<String>`), and `attributes` (`Map<String, String>`) are stored as PostgreSQL `jsonb` columns. The `Hibernate6Module` in `JacksonConfig` handles serialization correctly.

**Soft-delete vs. `isActive`**: Products have both. `deletedAt IS NULL` filters hard-removed records (admin and public queries). `isActive = true` is a visibility toggle — public queries require both; admin queries only filter `deletedAt`.

### Delivery Routing Model

```
Pincode (PK: 6-digit string)
  └── DeliveryZone (standard/express charges, COD flag, active flag)
        └── StockLocationZone (composite key: location_id + zone_id, ordered by priority)
              └── StockLocation (fulfillment hub)
                    └── InventoryLevel (qty per listing per location)
```

`GET /api/v1/public/delivery/check?pincode=&sku=` resolves serviceability: validate the PIN format → look up `Pincode` → check `DeliveryZone.isActive` → if a SKU is given, walk zone hubs in priority order for available stock, falling back to any hub with stock globally.

### Repository Pattern

Repositories use `@EntityGraph` on specific query methods to eagerly load associations and avoid N+1 queries. The set of eagerly loaded associations varies by method — check the repository interface before assuming what relations are available on returned entities.

Spring Data JPA property-path traversal is used for cross-entity filters (e.g. `findByCategorySlugAndDeletedAtIsNull` filters by `category.slug`, not a `categorySlug` column on products).

### Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`. The schema uses `ddl-auto: validate` — Hibernate never modifies the schema; all changes go through versioned SQL migration files (`V{n}__description.sql`).

The test profile (`application-test.yml`) uses H2 in-memory with Flyway disabled and `ddl-auto: create-drop`.

### Security

- BCrypt password hashing
- `@EnableMethodSecurity` is active — method-level `@PreAuthorize` can be used
- CORS origins configured via `cors.allowed-origins` property (comma-separated)

### Jackson

`JacksonConfig` registers `Hibernate6Module` with `INITIALIZE_NULL_COLLECTIONS` — this prevents infinite recursion and serialization errors for lazy-loaded JPA proxies.

### Testing

Tests use `@ExtendWith(MockitoExtension.class)` — pure unit tests with mocked repositories, no Spring context loaded. The existing `DeliveryCheckTest` is the reference pattern: instantiate the controller via `@InjectMocks`, mock only the repositories it touches, and call the controller method directly.

### Media / File Storage

Files are stored locally during development and tracked in the `media_assets` table (added in `V5__media_assets.sql`).

**API endpoints** (all require `ROLE_ADMIN` JWT):
```
POST   /api/v1/admin/media/upload   multipart: file, category, subcategory → MediaAsset
GET    /api/v1/admin/media          ?category=&subcategory=&page=&limit=   → MediaAsset[]
DELETE /api/v1/admin/media/{id}                                            → deletes file + DB row
```

Uploaded files are served publicly at `GET /uploads/{category}/{subcategory}/{uuid}.ext` (no auth required so the B2C storefront can display them).

**Storage config** (`application.yml`):
```yaml
storage:
  type: local                           # change to "s3" when ready
  local:
    upload-dir: ./uploads               # relative to JAR working directory
    base-url: http://localhost:8080/uploads
```

**Switching to S3**: set `storage.type=s3` and create `S3StorageService implements StorageService` in `in.kanchuk.storage`. The `store()` method should upload to S3 and return the CDN URL; `delete()` should remove the S3 object. The controller, entity, and repository need no changes.

**Allowed types**: JPEG, PNG, WebP, GIF, SVG. Max file size: 10 MB (configured via `spring.servlet.multipart`).

**Path convention**: `{upload-dir}/{category}/{subcategory}/{uuid}.{ext}`

Categories in use: `catalogue` (products, categories, fabrics), `marketing` (banners, occasions, designers, offers), `users-sellers` (users, sellers), `commerce` (orders, returns), `tax-logistics`, `ai-stylist` (blouse-recommendations, suit-recommendations).
