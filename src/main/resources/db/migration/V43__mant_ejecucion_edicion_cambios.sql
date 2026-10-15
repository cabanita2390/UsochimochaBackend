-- Lista JSON de los campos que cambiaron en la edición:
--   [{"campo":"fecha","antes":"2026-03-10","despues":"2026-03-12"}, ...]
-- TEXT (no JSONB) para que H2 pueda crear la columna en los tests.
-- Las ediciones anteriores quedan en NULL -> la web muestra "campos no registrados".
ALTER TABLE mant_ejecucion_edicion ADD COLUMN cambios TEXT;
