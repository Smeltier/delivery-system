DO $$ BEGIN
    CREATE TYPE account_role AS ENUM ('BASE_CLIENT', 'RESTAURANT_OWNER');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE restaurant_status AS ENUM ('OPEN', 'CLOSED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE menu_item_category AS ENUM ('DESSERT');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE order_status AS ENUM ('DRAFT', 'PAID', 'CONFIRMED', 'DELIVERED', 'CANCELLED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

DO $$ BEGIN
    CREATE TYPE payment_status AS ENUM ('PENDING', 'APPROVED', 'DECLINED', 'CANCELLED', 'REFUNDED');
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS addresses (
    id UUID PRIMARY KEY,
    street VARCHAR(255) NOT NULL,
    number VARCHAR(10) NOT NULL,
    complement VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    country VARCHAR(100) NOT NULL,
    zip_code VARCHAR(10) NOT NULL
);

CREATE TABLE IF NOT EXISTS accounts (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts_roles (
    account_id UUID NOT NULL REFERENCES accounts(id),
    role account_role NOT NULL,
    PRIMARY KEY (account_id, role)
);

CREATE TABLE IF NOT EXISTS clients (
    id UUID PRIMARY KEY REFERENCES accounts(id),
    cpf VARCHAR(11) UNIQUE,
    address_id UUID REFERENCES addresses(id)
);

CREATE TABLE IF NOT EXISTS restaurant_owners (
    id UUID PRIMARY KEY REFERENCES accounts(id),
    corporate_name VARCHAR(255),
    cnpj VARCHAR(14) UNIQUE
);

CREATE TABLE IF NOT EXISTS restaurants (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES restaurant_owners(id),
    name VARCHAR(255) NOT NULL,
    status restaurant_status NOT NULL DEFAULT 'CLOSED',
    open_hour TIME NOT NULL,
    close_hour TIME NOT NULL,
    currency VARCHAR(3),
    address_id UUID REFERENCES addresses(id),
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS menu_items (
    id UUID PRIMARY KEY,
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    category menu_item_category NOT NULL,
    unit_price_amount NUMERIC(10, 2) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS orders (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES clients(id),
    restaurant_id UUID NOT NULL REFERENCES restaurants(id),
    currency VARCHAR(3) NOT NULL,
    delivery_address_id UUID REFERENCES addresses(id),
    delivery_fee_amount NUMERIC(10, 2),
    delivery_fee_currency VARCHAR(3),
    status order_status NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMP NOT NULL,
    paid_at TIMESTAMP,
    confirmed_at TIMESTAMP,
    delivered_at TIMESTAMP,
    cancelled_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    menu_item_id UUID NOT NULL REFERENCES menu_items(id),
    menu_item_name VARCHAR(255) NOT NULL,
    menu_item_description TEXT NOT NULL,
    menu_item_category menu_item_category NOT NULL,
    unit_price_amount NUMERIC(10, 2) NOT NULL,
    unit_price_currency VARCHAR(3) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0)
);

CREATE TABLE IF NOT EXISTS payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    total_amount NUMERIC(10, 2) NOT NULL,
    total_currency VARCHAR(3) NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_payments (
    order_id UUID NOT NULL REFERENCES orders(id),
    payment_id UUID NOT NULL REFERENCES payments(id),
    PRIMARY KEY (order_id, payment_id)
);

CREATE INDEX idx_clients_address_id ON clients(address_id);
CREATE INDEX idx_restaurants_owner_id ON restaurants(owner_id);
CREATE INDEX idx_restaurants_address_id ON restaurants(address_id);
CREATE INDEX idx_menu_items_restaurant_id ON menu_items(restaurant_id);
CREATE INDEX idx_orders_client_id ON orders(client_id);
CREATE INDEX idx_orders_restaurant_id ON orders(restaurant_id);
CREATE INDEX idx_orders_delivery_address_id ON orders(delivery_address_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_menu_item_id ON order_items(menu_item_id);
CREATE INDEX idx_payments_order_id ON payments(order_id);

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_accounts_updated_at
BEFORE UPDATE ON accounts
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_restaurants_updated_at
BEFORE UPDATE ON restaurants
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_menu_items_updated_at
BEFORE UPDATE ON menu_items
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_orders_updated_at
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_payments_updated_at
BEFORE UPDATE ON payments
FOR EACH ROW EXECUTE FUNCTION set_updated_at();

