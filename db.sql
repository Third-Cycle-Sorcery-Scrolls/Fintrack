CREATE TYPE currency_type AS ENUM ('ETB', 'USD', 'EUR', 'GBP', 'JPY', 'CHF', 'CAD', 'AUD', 'CNY', 'INR', 'BRL'); -- extend as needed
CREATE TYPE transaction_type AS ENUM ('INCOME', 'EXPENSE');
CREATE TYPE frequency_type AS ENUM ('WEEKLY', 'MONTHLY', 'YEARLY');

CREATE TABLE profiles (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    default_currency currency_type NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE categories (
    id         SERIAL PRIMARY KEY,
    profile_id INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name       TEXT NOT NULL,
    CONSTRAINT unique_profile_category UNIQUE (profile_id, name)
);


CREATE TABLE tags (
    id         SERIAL PRIMARY KEY,
    profile_id INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    name       TEXT NOT NULL,
    CONSTRAINT unique_profile_tag UNIQUE (profile_id, name)
);


CREATE TABLE transactions (
    id          SERIAL PRIMARY KEY,
    profile_id  INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    date        DATE NOT NULL,
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    type        transaction_type NOT NULL,
    currency    currency_type NOT NULL,
    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE transaction_tags (
    transaction_id INTEGER NOT NULL REFERENCES transactions(id) ON DELETE CASCADE,
    tag_id         INTEGER NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (transaction_id, tag_id)
);


CREATE TABLE recurring_expenses (
    id          SERIAL PRIMARY KEY,
    profile_id  INTEGER NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    amount      NUMERIC(15,2) NOT NULL CHECK (amount > 0),
    currency    currency_type NOT NULL,
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    description TEXT,
    frequency   frequency_type NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE,    -- NULL means indefinite
    is_active   BOOLEAN NOT NULL DEFAULT true,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    CHECK (end_date IS NULL OR end_date >= start_date)
);