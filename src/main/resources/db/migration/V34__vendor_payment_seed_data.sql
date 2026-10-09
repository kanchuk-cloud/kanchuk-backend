-- ─────────────────────────────────────────────────────────────────────────────
-- V34: Vendor Payment seed data
-- Covers: pending, processing, paid, failed, on_hold statuses
-- Vendors: Rajesh Textiles, Surat Silk House, Lucknow Chikan Emporium
-- Some payments linked to existing POs
-- ─────────────────────────────────────────────────────────────────────────────

INSERT INTO vendor_payments (
    id, vendor_id, purchase_order_id,
    invoice_number, invoice_date,
    gross_amount, tds_percent, tds_amount, net_payable,
    commission_amount,
    status, payment_method,
    utr_number, payment_date,
    approved_at,
    failure_reason,
    created_at, updated_at
) VALUES

-- PAY1010: Rajesh Textiles — paid, linked to PO-2026-0003
('PAY1010',
 '5eed0000-0000-0000-0007-000000000001',
 '5eed0000-0000-0000-0021-000000000003',
 'INV-RT-2026-001', '2026-08-15',
 125000.00, 2.00, 2500.00, 122500.00, 0.00,
 'paid', 'NEFT',
 'NEFT2026081500123', '2026-08-20',
 '2026-08-17 10:30:00+05:30',
 NULL,
 now(), now()),

-- PAY1011: Surat Silk — paid, linked to PO-2026-0001
('PAY1011',
 '5eed0000-0000-0000-0007-000000000002',
 '5eed0000-0000-0000-0021-000000000001',
 'INV-SS-2026-041', '2026-08-22',
 88000.00, 2.00, 1760.00, 86240.00, 0.00,
 'paid', 'RTGS',
 'RTGS2026082200456', '2026-08-25',
 '2026-08-23 14:15:00+05:30',
 NULL,
 now(), now()),

-- PAY1012: Lucknow Chikan — processing, linked to PO-2026-0002
('PAY1012',
 '5eed0000-0000-0000-0007-000000000003',
 '5eed0000-0000-0000-0021-000000000002',
 'INV-LC-2026-017', '2026-09-01',
 210000.00, 2.00, 4200.00, 205800.00, 0.00,
 'processing', 'NEFT',
 NULL, NULL,
 '2026-09-03 09:00:00+05:30',
 NULL,
 now(), now()),

-- PAY1013: Rajesh Textiles — pending, no PO
('PAY1013',
 '5eed0000-0000-0000-0007-000000000001',
 NULL,
 'INV-RT-2026-002', '2026-09-10',
 54000.00, 2.00, 1080.00, 52920.00, 0.00,
 'pending', 'UPI',
 NULL, NULL,
 NULL,
 NULL,
 now(), now()),

-- PAY1014: Surat Silk — failed
('PAY1014',
 '5eed0000-0000-0000-0007-000000000002',
 NULL,
 'INV-SS-2026-042', '2026-09-05',
 67500.00, 2.00, 1350.00, 66150.00, 0.00,
 'failed', 'NEFT',
 NULL, NULL,
 '2026-09-06 11:00:00+05:30',
 'Bank account details mismatch — IFSC code invalid',
 now(), now()),

-- PAY1015: Lucknow Chikan — on_hold
('PAY1015',
 '5eed0000-0000-0000-0007-000000000003',
 NULL,
 'INV-LC-2026-018', '2026-09-15',
 145000.00, 2.00, 2900.00, 142100.00, 0.00,
 'on_hold', 'RTGS',
 NULL, NULL,
 '2026-09-16 16:30:00+05:30',
 NULL,
 now(), now()),

-- PAY1016: Rajesh Textiles — pending, high value
('PAY1016',
 '5eed0000-0000-0000-0007-000000000001',
 NULL,
 'INV-RT-2026-003', '2026-09-20',
 320000.00, 2.00, 6400.00, 313600.00, 0.00,
 'pending', 'RTGS',
 NULL, NULL,
 NULL,
 NULL,
 now(), now());

-- Advance the sequence past our inserted IDs
SELECT setval('vendor_payment_seq', 1016, true);
