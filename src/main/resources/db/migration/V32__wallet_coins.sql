-- Singleton config row for loyalty/wallet rules
CREATE TABLE loyalty_settings (
    id                    BIGSERIAL PRIMARY KEY,
    coins_earn_pct        NUMERIC(5,2)  NOT NULL DEFAULT 2.00,
    coins_redeem_max_pct  NUMERIC(5,2)  NOT NULL DEFAULT 10.00,
    coins_per_rupee       NUMERIC(5,2)  NOT NULL DEFAULT 1.00,
    coins_expiry_days     INT           NOT NULL DEFAULT 365,
    wallet_expiry_days    INT           NOT NULL DEFAULT 730,
    min_order_for_coins   NUMERIC(10,2) NOT NULL DEFAULT 0,
    coins_on_coin_payment BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

INSERT INTO loyalty_settings (id) VALUES (1);

-- Full audit ledger for every wallet/coin credit or debit
CREATE TABLE wallet_ledger (
    id                UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id           UUID          NOT NULL REFERENCES users(id),
    order_id          UUID          REFERENCES orders(id),
    type              VARCHAR(30)   NOT NULL,
    amount            NUMERIC(12,2) NOT NULL DEFAULT 0,
    balance_after     NUMERIC(12,2),
    coin_amount       INT           DEFAULT 0,
    coin_balance_after INT,
    expires_at        DATE,
    is_pending        BOOLEAN       NOT NULL DEFAULT FALSE,
    note              TEXT,
    created_at        TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX wallet_ledger_user_idx    ON wallet_ledger(user_id);
CREATE INDEX wallet_ledger_order_idx   ON wallet_ledger(order_id);
CREATE INDEX wallet_ledger_expires_idx ON wallet_ledger(expires_at) WHERE type = 'COIN_EARN';
