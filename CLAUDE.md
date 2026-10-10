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

### Service Layer

Most controllers inject repositories directly with no service intermediary. The exceptions are:

- `GenericAdminService` — abstract utility base extended by **all** controller types (admin, public, and vendor — despite the name). Provides:
  - `pageRequest(page, limit)` — 1-indexed page, sorted by `createdAt DESC`
  - `buildMeta(Page<?> pg, int page, int limit)` — builds the `PageMeta` response object
  - `findOrThrow(repo, id, entityName)` — throws `EntityNotFoundException` (caught by `GlobalExceptionHandler`)
  - `applyPatch(entity, fields)` — reflection-based field setter for PUT endpoints that receive `Map<String, Object>`. Silently swallows any reflection error via `catch (Exception ignored)`.

- `AuthService` — handles both admin and vendor login flows via BCrypt + JWT.

- `OtpService` — full OTP lifecycle: generates a `SecureRandom` OTP, BCrypt-hashes it (never stored plaintext), enforces resend cooldowns and max attempt limits, and delegates dispatch to `SmsService`. Called by `PublicOtpController`.

- `SmsService` — delegates to `SmsProvider` (currently `MockSmsProvider`; swap to a real provider bean to go live). Manages order-status SMS notifications via `SmsTemplates`.

- `InventoryService` — atomic `adjustStock()`: updates `InventoryLevel.quantityOnHand` and writes a `StockMovement` record in the same transaction. Called by admin stock-adjustment flows.

- `VendorService` — vendor lifecycle: status transitions with KYC document gating, auto-KYC verification when all required documents are verified, payment CRUD with TDS/net-payable calculation, MSME 45-day due-date rules, and PO payment-status sync.

**`applyPatch` limitations**:
- Coerces scalars (String, Boolean, Integer, Long, BigDecimal, UUID, OffsetDateTime, List) but **cannot set `@ManyToOne` relations by ID** — fetch the target entity from its repository first, then call the setter directly.
- Lombok strips the `is` prefix from boolean field names (field `isActive` → setter `setActive`). `applyPatch` handles this transparently, but the map key must be `"isActive"` not `"active"` to match.

**Exception**: `PublicDeliveryController` does not extend `GenericAdminService` — it uses `@RequiredArgsConstructor` directly.

### Three User Systems

There are three distinct user entities, each with their own JWT:

| Entity | Login endpoint | Role in JWT | Route prefix |
|--------|---------------|-------------|--------------|
| `AdminUser` | `POST /api/v1/auth/admin/login` | `ROLE_ADMIN` | `/api/v1/admin/**` |
| `VendorUser` (linked to `Seller`) | `POST /api/v1/auth/vendor/login` | `ROLE_VENDOR` | `/api/v1/vendor/**` |
| `User` (B2C customer) | OTP-based via `/api/v1/auth/send-otp` + `verify-otp` | — | — |

Vendor JWTs embed the `sellerId` as an extra claim. `JwtAuthenticationFilter` stores it in `Authentication.getCredentials()` — vendor controllers retrieve it via `currentSellerId(auth)` for row-level isolation.

B2C customer auth uses a phone-based OTP flow (`OtpPurpose`: `LOGIN`, `SIGNUP`, `PHONE_VERIFICATION`, `FORGOT_PASSWORD`). The OTP is BCrypt-hashed on storage; `OtpService.verifyOtp()` uses `PasswordEncoder.matches()` to validate. After verification the caller is responsible for issuing a session token (not yet implemented).

### Request Body Pattern

Write endpoints (POST, PUT, PATCH) typically accept raw `Map<String, Object>` rather than typed DTO classes. Only auth endpoints (`LoginRequest`, `SendOtpRequest`, `VerifyOtpRequest`) have dedicated request DTOs. This means no `@Valid` annotations on most write endpoints — validation is done manually inside the handler.

### Response Envelope

All responses use `ApiResponse<T>` — factory methods: `ApiResponse.ok(data)`, `ApiResponse.ok(data, meta)`, `ApiResponse.created(data)`, `ApiResponse.deleted()`, `ApiResponse.error(message)`.

### API Route Structure

```
POST /api/v1/auth/admin/login     — public, returns admin JWT
POST /api/v1/auth/vendor/login    — public, returns vendor JWT (with sellerId claim)
POST /api/v1/auth/send-otp        — public, sends OTP to mobile
POST /api/v1/auth/verify-otp      — public, verifies OTP
     /api/v1/public/**            — public (no auth)
     /uploads/**                  — public (static file serving)
     /catalogue/**                — public (static file serving alias)
     /marketing/**                — public (static file serving alias)
     /api/v1/admin/**             — requires ROLE_ADMIN JWT
     /api/v1/vendor/**            — requires ROLE_VENDOR JWT
```

JWT is stateless (no sessions). `JwtAuthenticationFilter` validates `Authorization: Bearer <token>`, extracts subject and `role` claim, and sets the `SecurityContext`. `JwtUtil` generates and validates tokens using HMAC-SHA with the configured secret.

### Controller Package Layout

```
controller/
  AuthController.java           — /api/v1/auth/** (admin + vendor login)
  admin/Admin*Controller.java   — /api/v1/admin/** (ROLE_ADMIN)
  pub/Public*Controller.java    — /api/v1/public/** (no auth)
  vendor/Vendor*Controller.java — /api/v1/vendor/** (ROLE_VENDOR)
```

Vendor controllers enforce row-level isolation: every query filters by `sellerId` extracted from the JWT so a vendor can only see their own `PurchaseOrder`s and `GoodsReceipt`s.

### Entity Hierarchy

All entities extend one of two abstract base classes:
- `BaseEntity` — UUID PK (auto-generated via `GenerationType.UUID`), `createdAt`, `updatedAt`
- `SoftDeleteEntity extends BaseEntity` — adds `deletedAt`; call `.softDelete()` to mark deleted, never issue a DB DELETE

Two entities use composite keys with `@EmbeddedId`:
- `VariantOptionValue` — links `ProductVariant` ↔ `ProductOptionValue`
- `StockLocationZone` — links `StockLocation` ↔ `DeliveryZone` with a `priority` column

`Testimonial` extends `BaseEntity` (no soft-delete — hard-delete via `repo.deleteById`). Fields: `customerName`, `city`, `quote`, `rating` (SMALLINT 1–5), `productName`, `avatarUrl`, `isVerified`, `isActive`, `displayOrder`. Admin CRUD at `/api/v1/admin/testimonials`; public read at `/api/v1/public/testimonials` (active only, ordered by `displayOrder`, max 20).

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

`Designer` was added in V41/V42 migrations. Products have a nullable `@ManyToOne designer` — cannot be set via `applyPatch`; fetch from `DesignerRepository` first.

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

### GST Calculation

`GstCalculator.calculate(unitPrice, interState)` returns a `TaxBreakdown` record. Unit price is **GST-inclusive** (tax-exclusive value is back-calculated via divisor). Rate: ≤ ₹2500 → 5%, > ₹2500 → 18%. Intra-state → CGST + SGST split; inter-state → full IGST. State codes are resolved from `Pincode.stateCode` for the delivery address and `StockLocation.stateCode` for the fulfillment hub.

`PublicOrderController.placeOrder()` applies this logic per line item and aggregates `subtotalTaxable`, `totalCgst`, `totalSgst`, `totalIgst`, `totalTax` onto the `Order` entity.

### Repository Pattern

Repositories use `@EntityGraph` on specific query methods to eagerly load associations and avoid N+1 queries. The set of eagerly loaded associations varies by method — check the repository interface before assuming what relations are available on returned entities.

Spring Data JPA property-path traversal is used for cross-entity filters (e.g. `findByCategorySlugAndDeletedAtIsNull` filters by `category.slug`, not a `categorySlug` column on products).

### Wallet and coin reversal flows

`AdminOrderController` — when an order's status transitions to `cancelled`, `reverseCancelledOrder(order)` is called automatically. It reverses any `COIN_REDEEM` entry (creates `COIN_REVERSE`), reverses any `WALLET_DEBIT` entry (creates `WALLET_REVERSE`), cancels pending `COIN_EARN`, and restores inventory per the order's item snapshot SKUs.

`AdminReturnController` — when advancing a return from `received` → `resolved`, `processReturnRefund(ret)` is called. It reverses `COIN_REDEEM` → `COIN_REVERSE`, reverses `WALLET_DEBIT` → `WALLET_REVERSE`, cancels pending `COIN_EARN`, and credits `order.total` to the customer's wallet as `WALLET_CREDIT`. Sets `ret.refundAmount` and `ret.coinsRefunded` for display in both admin and B2C.

`PublicWalletController` — `GET /public/wallet/me?userId={id}` returns `{ balance, coinBalance, coinWorth, expiringCoins, expiringDate, ledger[] }`.

`CoinExpiryScheduler` — runs daily at 02:00 via `@Scheduled(cron = "0 0 2 * * *")`. Finds `COIN_EARN` entries whose expiry date has passed, deducts them from `User.loyaltyPoints`, and writes a `COIN_EXPIRE` ledger entry per user. `AsyncConfig` enables the Spring scheduling executor.

### Coupon System

`Coupon` entity stores code, type (`percent`/`flat`), value, min order amount, max discount (for percent coupons), usage limit, and active flag. Admin CRUD at `/api/v1/admin/coupons`. Public endpoints: `GET /api/v1/public/coupons` (active coupons list) and `POST /api/v1/public/coupons/validate` (returns discount amount for a given cart total). `PublicOrderController.placeOrder()` increments `usageCount` when a coupon code is present in the order body.

### Database Migrations

Flyway migrations live in `src/main/resources/db/migration/`. The schema uses `ddl-auto: validate` — Hibernate never modifies the schema; all changes go through versioned SQL migration files (`V{n}__description.sql`).

The test profile (`application-test.yml`) uses H2 in-memory with Flyway disabled and `ddl-auto: create-drop`.

**Idempotent migrations**: when a migration has been applied manually to the DB before the backend restarts, use `CREATE TABLE IF NOT EXISTS` and wrap seed inserts in a `DO $$ BEGIN IF (SELECT COUNT(*) FROM table) = 0 THEN INSERT ...; END IF; END $$;` block. This prevents Flyway from failing on an already-existing table. Latest migration: `V52__savings_edit_cards.sql`.

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
