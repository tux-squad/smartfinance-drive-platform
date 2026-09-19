-- DDL Migration Script for New Bounded Contexts and Aggregates

-- Conversations & Messages (messaging)
CREATE TABLE IF NOT EXISTS conversations (
    id UUID PRIMARY KEY,
    buyer_user_id VARCHAR(255) NOT NULL,
    dealer_user_id VARCHAR(255) NOT NULL,
    vehicle_id UUID,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    last_message_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_conversations_buyer ON conversations(buyer_user_id);
CREATE INDEX IF NOT EXISTS idx_conversations_dealer ON conversations(dealer_user_id);

CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY,
    conversation_id UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    sender_user_id VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    read_by_recipient BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON messages(conversation_id);

-- AI Consultations (consultations)
CREATE TABLE IF NOT EXISTS ai_consultations (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    session_id VARCHAR(255) NOT NULL,
    user_query TEXT NOT NULL,
    ai_response TEXT NOT NULL,
    recommended_vehicle_ids TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_ai_consultations_user ON ai_consultations(user_id);

-- Credit Applications (financing)
CREATE TABLE IF NOT EXISTS credit_applications (
    id UUID PRIMARY KEY,
    buyer_user_id VARCHAR(255) NOT NULL,
    financial_entity_id UUID NOT NULL,
    vehicle_id UUID NOT NULL,
    simulation_id UUID,
    requested_amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    status VARCHAR(30) NOT NULL DEFAULT 'SUBMITTED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_credit_app_buyer ON credit_applications(buyer_user_id);
CREATE INDEX IF NOT EXISTS idx_credit_app_entity ON credit_applications(financial_entity_id);

-- Dealerships B2B (partners)
CREATE TABLE IF NOT EXISTS dealerships (
    id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    ruc VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(255),
    website VARCHAR(255),
    description TEXT,
    operating_hours VARCHAR(255),
    logo_url VARCHAR(1000),
    banner_url VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_dealerships_user ON dealerships(user_id);
CREATE INDEX IF NOT EXISTS idx_dealerships_ruc ON dealerships(ruc);

-- Sales Agents (iam)
CREATE TABLE IF NOT EXISTS sales_agents (
    id UUID PRIMARY KEY,
    dealer_user_id VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX IF NOT EXISTS idx_sales_agents_dealer ON sales_agents(dealer_user_id);

-- CRM Prospects, Notes & Test Drives (crm)
CREATE TABLE IF NOT EXISTS prospects (
    id UUID PRIMARY KEY,
    dealer_user_id VARCHAR(255) NOT NULL,
    buyer_user_id VARCHAR(255),
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    interested_vehicle_id UUID,
    status VARCHAR(30) NOT NULL DEFAULT 'NEW',
    sales_agent_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_prospects_dealer ON prospects(dealer_user_id);
CREATE INDEX IF NOT EXISTS idx_prospects_buyer ON prospects(buyer_user_id);

CREATE TABLE IF NOT EXISTS prospect_notes (
    id UUID PRIMARY KEY,
    prospect_id UUID NOT NULL REFERENCES prospects(id) ON DELETE CASCADE,
    author_user_id VARCHAR(255) NOT NULL,
    note_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_prospect_notes_prospect ON prospect_notes(prospect_id);

CREATE TABLE IF NOT EXISTS test_drives (
    id UUID PRIMARY KEY,
    buyer_user_id VARCHAR(255) NOT NULL,
    vehicle_id UUID NOT NULL,
    dealership_id UUID NOT NULL,
    scheduled_date_time TIMESTAMP NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_test_drives_buyer ON test_drives(buyer_user_id);
CREATE INDEX IF NOT EXISTS idx_test_drives_vehicle ON test_drives(vehicle_id);
