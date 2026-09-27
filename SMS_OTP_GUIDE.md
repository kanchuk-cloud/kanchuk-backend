# SMS & OTP Integration Guide

## Overview

This project uses a **provider-agnostic SMS abstraction**. During development, all SMS is free — messages are logged instead of sent. Switching to a real provider (MSG91, Twilio, etc.) requires only a config change and one new class.

---

## Architecture

```
SmsProvider (interface)
    │
    ├── MockSmsProvider          ← active when sms.provider=mock (default)
    ├── Msg91SmsProvider         ← active when sms.provider=msg91  (you add this)
    └── TwilioSmsProvider        ← active when sms.provider=twilio (you add this)

SmsService
    ├── sendOtp(mobile, otp, expiryMinutes)              ← synchronous
    └── @Async sendOrderNotification(mobile, order, type) ← async (never blocks API)

OtpService
    ├── sendOtp(mobile, purpose)   ← generates, hashes, saves, dispatches
    └── verifyOtp(mobile, otp, purpose)
```

**Package locations:**

| Package | Contents |
|---|---|
| `in.kanchuk.sms` | `SmsProvider`, `SmsResponse`, `MockSmsProvider`, `SmsUtil`, `SmsProperties`, `OtpProperties` |
| `in.kanchuk.sms.template` | `OrderNotificationType` (enum), `SmsTemplates` (message strings) |
| `in.kanchuk.service` | `SmsService`, `OtpService` |
| `in.kanchuk.entity` | `OtpVerification`, `OtpPurpose` |
| `in.kanchuk.repository` | `OtpVerificationRepository` |
| `in.kanchuk.controller.pub` | `PublicOtpController` |
| `in.kanchuk.config` | `AsyncConfig` (thread pool) |

---

## Configuration

### Current (development — free, no real SMS)

```yaml
# application.yml
sms:
  provider: mock     # logs SMS instead of sending
  enabled: true

otp:
  length: 6
  expiry-minutes: 5
  max-attempts: 5
  resend-cooldown-seconds: 60
```

### Disable SMS completely

```yaml
sms:
  enabled: false
```

When disabled, all SMS calls are silently skipped. Order creation and OTP verification still work normally.

---

## OTP API Endpoints

Both endpoints are under `/api/v1/auth/**` which is already `permitAll` — no security config change needed.

### Send OTP

```
POST /api/v1/auth/send-otp
Content-Type: application/json

{
  "mobileNumber": "9876543210",
  "purpose": "LOGIN"          // optional — LOGIN | SIGNUP | PHONE_VERIFICATION | FORGOT_PASSWORD
}
```

**Success response:**
```json
{ "success": true, "message": "OTP sent successfully" }
```

**Error responses:**
```json
{ "success": false, "message": "Mobile number must be exactly 10 digits" }
{ "success": false, "message": "Please wait 45 second(s) before requesting a new OTP." }
```

**In dev mode** — check the application log for the OTP:
```
INFO [MOCK SMS] To: ******3210 | Message: Your OTP is 482913. It is valid for 5 minutes...
```

### Verify OTP

```
POST /api/v1/auth/verify-otp
Content-Type: application/json

{
  "mobileNumber": "9876543210",
  "otp": "482913",
  "purpose": "LOGIN"
}
```

**Success:**
```json
{ "success": true, "message": "OTP verified successfully" }
```

**Failure (invalid/expired/too many attempts):**
```json
{ "success": false, "message": "Invalid or expired OTP." }
{ "success": false, "message": "Too many failed attempts. Please request a new OTP." }
```

---

## OTP Security Details

| Property | Value | Config key |
|---|---|---|
| Length | 6 digits | `otp.length` |
| Expiry | 5 minutes | `otp.expiry-minutes` |
| Max wrong attempts | 5 | `otp.max-attempts` |
| Resend cooldown | 60 seconds | `otp.resend-cooldown-seconds` |
| Storage | BCrypt hash only | — |
| Old OTPs | Invalidated on new request | — |

- OTP is **never** returned in any API response.
- Mobile numbers are **always masked** in logs (`9876543210` → `******3210`).
- Database stores `otp_hash` (BCrypt), never the raw OTP.

---

## Order SMS Notifications

SMS fires automatically on these order lifecycle events:

| Event | Trigger location | Notification type |
|---|---|---|
| Order confirmed | `AdminOrderController.advance()` — placed→confirmed | `ORDER_CONFIRMED` |
| Order packed | `AdminOrderController.advance()` — confirmed→packed | `ORDER_PACKED` |
| Order shipped | `AdminOrderController.advance()` — packed→shipped | `ORDER_SHIPPED` |
| Out for delivery | `AdminOrderController.advance()` — shipped→out_for_delivery | `OUT_FOR_DELIVERY` |
| Order delivered | `AdminOrderController.advance()` — out_for_delivery→delivered | `ORDER_DELIVERED` |
| Order cancelled | `AdminOrderController.update()` — status set to cancelled | `ORDER_CANCELLED` |
| Return initiated | `AdminOrderController.initiateReturn()` | `RETURN_INITIATED` |
| Refund completed | `AdminReturnController.update()` — status set to refunded | `REFUND_COMPLETED` |

All order SMS runs **asynchronously** (`@Async("smsExecutor")`) — the order API always responds immediately regardless of SMS outcome.

### SMS templates (in `SmsTemplates.java`)

```
ORDER_CONFIRMED    → "Your order #KCH-2026-000001 has been confirmed. Thank you for shopping with us!"
ORDER_PACKED       → "Your order #KCH-2026-000001 has been packed and is ready to ship."
ORDER_SHIPPED      → "Your order #KCH-2026-000001 has been shipped and is on its way."
OUT_FOR_DELIVERY   → "Your order #KCH-2026-000001 is out for delivery today."
ORDER_DELIVERED    → "Your order #KCH-2026-000001 has been delivered. Thank you for shopping with us!"
ORDER_CANCELLED    → "Your order #KCH-2026-000001 has been cancelled. Contact us for assistance."
RETURN_INITIATED   → "Return for order #KCH-2026-000001 has been initiated. We will process it shortly."
REFUND_COMPLETED   → "Refund for order #KCH-2026-000001 has been processed and will reflect in 5-7 business days."
OTP                → "Your OTP is 482913. It is valid for 5 minutes. Do not share it with anyone."
```

---

## Switching to a Real SMS Provider

### Example: MSG91

**Step 1 — Add credentials to config:**

```yaml
# application.yml
sms:
  provider: msg91
  enabled: true

msg91:
  auth-key: ${MSG91_AUTH_KEY}
  sender-id: KOSHAA
```

**Step 2 — Create the provider class** (one new file, nothing else changes):

```java
// src/main/java/in/kanchuk/sms/Msg91SmsProvider.java
package in.kanchuk.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@ConditionalOnProperty(name = "sms.provider", havingValue = "msg91")
public class Msg91SmsProvider implements SmsProvider {

    private static final Logger log = LoggerFactory.getLogger(Msg91SmsProvider.class);

    @Value("${msg91.auth-key}")
    private String authKey;

    @Value("${msg91.sender-id}")
    private String senderId;

    private final RestClient restClient = RestClient.create();

    @Override
    public SmsResponse send(String mobileNumber, String message) {
        try {
            String url = "https://api.msg91.com/api/sendhttp.php"
                + "?authkey=" + authKey
                + "&mobiles=91" + mobileNumber
                + "&message=" + URLEncoder.encode(message, StandardCharsets.UTF_8)
                + "&sender=" + senderId
                + "&route=4";  // 4 = transactional route

            String response = restClient.get().uri(url).retrieve().body(String.class);
            log.info("MSG91 response for {}: {}", SmsUtil.mask(mobileNumber), response);
            return new SmsResponse(true, response);

        } catch (Exception e) {
            log.error("MSG91 send failed for {}: {}", SmsUtil.mask(mobileNumber), e.getMessage());
            return new SmsResponse(false, e.getMessage());
        }
    }
}
```

**Step 3 — Set environment variable in production:**

```bash
export MSG91_AUTH_KEY=your_key_here
```

That's it. `SmsService`, `OtpService`, `AdminOrderController`, and `AdminReturnController` are **untouched**.

### Example: Twilio

Same pattern — change `havingValue` to `"twilio"` and use Twilio's REST API or Java SDK inside `send()`.

---

## How Provider Selection Works

```
sms.provider=mock   →  MockSmsProvider  ✓   (all other providers skipped)
sms.provider=msg91  →  Msg91SmsProvider ✓   (MockSmsProvider skipped)
sms.provider=twilio →  TwilioSmsProvider ✓  (MockSmsProvider skipped)
```

Spring's `@ConditionalOnProperty` registers exactly one `SmsProvider` bean. `SmsService` receives it via constructor injection — no `if/else`, no factory, no switch.

---

## Database

Migration: `V24__otp_verification.sql`

```sql
CREATE TABLE otp_verification (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    mobile_number VARCHAR(15)  NOT NULL,
    otp_hash      VARCHAR(255) NOT NULL,     -- BCrypt hash, never plaintext
    purpose       VARCHAR(30)  NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    verified_at   TIMESTAMPTZ,              -- set when OTP is used (single-use)
    attempt_count INTEGER      NOT NULL DEFAULT 0,
    resend_after  TIMESTAMPTZ,              -- resend cooldown enforced here
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);
```

---

## Test Coverage

```
MockSmsProviderTest   —  6 tests  (send success, masking)
SmsServiceTest        —  9 tests  (enabled/disabled, null phone, provider failure)
OtpServiceTest        — 16 tests  (generate, send, verify, cooldown, max attempts, hashing)
SmsTemplatesTest      —  7 tests  (all types contain order number, OTP format)
OrderSmsTest          — 12 tests  (each advance transition, cancellation, return, SMS failure isolation)
─────────────────────────────────
Total                   50 tests  — all passing
```
