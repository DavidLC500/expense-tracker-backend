CREATE TABLE budgets (
  id UUID PRIMARY KEY,
  category_id UUID NOT NULL REFERENCES categories(id),
  month DATE NOT NULL,
  amount NUMERIC(14,2) NOT NULL,
  currency VARCHAR(3) NOT NULL,
  CONSTRAINT uq_budgets_category_month UNIQUE (category_id, month)
);
