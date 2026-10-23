-- La propia BD impide dos categorías con el mismo nombre normalizado. Comprobarlo en el caso de uso
-- no bastaba: con 30 peticiones simultáneas para crear "Ocio" se crearon 30 filas.
--
-- Misma normalización que TextNormalizer: sin tildes, en mayúsculas, sin espacios en los extremos
-- y con los interiores colapsados. Un índice solo admite funciones IMMUTABLE y unaccent() es
-- STABLE (depende de su diccionario): se envuelve fijando el diccionario, el patrón habitual.
CREATE FUNCTION normalize_category_name(category_name text) RETURNS text
    LANGUAGE sql IMMUTABLE STRICT PARALLEL SAFE
    AS $$
        SELECT regexp_replace(
            upper(public.unaccent('public.unaccent'::regdictionary, btrim(category_name))),
            '\s+', ' ', 'g')
    $$;

CREATE UNIQUE INDEX uq_categories_normalized_name ON categories (normalize_category_name(name));
