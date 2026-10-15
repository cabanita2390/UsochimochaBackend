-- Reintroduce mant_evidencia.hash_sha256 (quitado en V36 por no tener uso real) con un
-- propósito concreto esta vez: detectar subidas duplicadas de la misma foto para la misma
-- ejecución. WorkManager reintenta la subida de evidencia ante cualquier fallo de red, y
-- SubstationController.agregarEvidencia no tenía ninguna protección de idempotencia — un
-- timeout justo después de que el servidor ya guardó el archivo (éxito ambiguo desde el
-- cliente) podía terminar creando una fila de evidencia repetida para la misma foto.
--
-- Nullable porque las filas ya existentes no tienen el hash calculado; SubstationService
-- siempre lo calcula y lo guarda para las filas nuevas. NULL no rompe la unicidad en
-- Postgres (cada NULL se trata como distinto), así que las filas viejas no chocan entre sí.
ALTER TABLE mant_evidencia ADD COLUMN hash_sha256 VARCHAR(64);

CREATE UNIQUE INDEX idx_mant_evidencia_ejecucion_hash ON mant_evidencia(ejecucion_id, hash_sha256);
