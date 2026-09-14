-- Initial DDL Migration Script for Billing Bounded Context Schema

CREATE TABLE IF NOT EXISTS plans (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    price NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    billing_cycle VARCHAR(20) NOT NULL,
    max_vehicle_listings INT NOT NULL,
    max_simulations_per_month INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    stripe_price_id VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS subscriptions (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES plans(id),
    status VARCHAR(20) NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    auto_renew BOOLEAN NOT NULL DEFAULT TRUE,
    stripe_subscription_id VARCHAR(255),
    stripe_customer_id VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_subscription_user_id ON subscriptions(user_id);
CREATE INDEX IF NOT EXISTS idx_subscription_stripe_sub_id ON subscriptions(stripe_subscription_id);
CREATE INDEX IF NOT EXISTS idx_subscription_stripe_cus_id ON subscriptions(stripe_customer_id);

CREATE TABLE IF NOT EXISTS invoices (
    id BIGSERIAL PRIMARY KEY,
    subscription_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(20) NOT NULL,
    issued_at TIMESTAMP NOT NULL,
    due_date TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    stripe_payment_intent_id VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_invoice_user_id ON invoices(user_id);
CREATE INDEX IF NOT EXISTS idx_invoice_subscription_id ON invoices(subscription_id);
