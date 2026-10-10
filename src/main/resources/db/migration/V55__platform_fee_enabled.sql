ALTER TABLE loyalty_settings
  ADD COLUMN IF NOT EXISTS platform_fee_enabled BOOLEAN NOT NULL DEFAULT true;
