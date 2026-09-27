-- V24: OTP verification table for customer authentication flows
-- Stores hashed OTPs for login, signup, phone verification, and password reset.

CREATE TABLE otp_verification (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    mobile_number VARCHAR(15)  NOT NULL,
    otp_hash      VARCHAR(255) NOT NULL,
    purpose       VARCHAR(30)  NOT NULL,
    expires_at    TIMESTAMPTZ  NOT NULL,
    verified_at   TIMESTAMPTZ,
    attempt_count INTEGER      NOT NULL DEFAULT 0,
    resend_after  TIMESTAMPTZ,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_otp_mobile_purpose ON otp_verification(mobile_number, purpose);
CREATE INDEX idx_otp_expires_at     ON otp_verification(expires_at);
