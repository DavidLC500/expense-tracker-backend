-- Marca los movimientos cuya categoría ha corregido el usuario a mano: al reaplicar las reglas se
-- respetan. Los existentes quedan como categorizados por reglas (no hay forma de saber cuáles se
-- corrigieron antes de esta migración).
ALTER TABLE transactions ADD COLUMN manually_categorized BOOLEAN NOT NULL DEFAULT FALSE;
