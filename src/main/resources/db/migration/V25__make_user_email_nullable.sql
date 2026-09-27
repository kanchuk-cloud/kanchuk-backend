-- Email is optional for customers who register via mobile OTP
ALTER TABLE users ALTER COLUMN email DROP NOT NULL;
ALTER TABLE users ALTER COLUMN email DROP DEFAULT;
