ALTER TABLE orders
  ADD COLUMN payment_method  VARCHAR(50),
  ADD COLUMN payment_status  VARCHAR(30) NOT NULL DEFAULT 'pending';
