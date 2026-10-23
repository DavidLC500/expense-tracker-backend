CREATE TABLE transactions (
  id UUID PRIMARY KEY,
  date DATE NOT NULL,
  concept VARCHAR(500) NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  category_id UUID REFERENCES categories(id),
  import_hash VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX idx_transactions_date ON transactions (date);
CREATE INDEX idx_transactions_category_date ON transactions (category_id, date);
