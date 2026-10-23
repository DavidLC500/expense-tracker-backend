CREATE TABLE rules (
  id UUID PRIMARY KEY,
  pattern VARCHAR(255) NOT NULL,
  category_id UUID NOT NULL REFERENCES categories(id),
  priority INTEGER NOT NULL
);
