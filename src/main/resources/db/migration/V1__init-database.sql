CREATE TABLE wallets (
    id CHAR(36) NOT NULL PRIMARY KEY,
    pix_key VARCHAR(100),
    balance DECIMAL(20, 2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_pix_key UNIQUE (pix_key)
);

CREATE TABLE transactions (
    id CHAR(36) NOT NULL PRIMARY KEY,
    end_to_end_id VARCHAR(100) NOT NULL,
    sender_wallet_id CHAR(36) NOT NULL,
    receiver_wallet_id CHAR(36) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_end_to_end UNIQUE (end_to_end_id),
    FOREIGN KEY (sender_wallet_id) REFERENCES wallets(id),
    FOREIGN KEY (receiver_wallet_id) REFERENCES wallets(id)
);

CREATE TABLE ledger_entries (
    id CHAR(36) NOT NULL PRIMARY KEY,
    wallet_id CHAR(36) NOT NULL,
    amount DECIMAL(20, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    end_to_end_id VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,

    FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE INDEX idx_ledger_wallet_date ON ledger_entries(wallet_id, created_at);

CREATE TABLE idempotency_keys (
    key_id VARCHAR(255) NOT NULL PRIMARY KEY,
    response_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);