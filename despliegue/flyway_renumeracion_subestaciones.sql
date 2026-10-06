-- Ajusta flyway_schema_history de una base que aplicó la numeración ANTERIOR de las
-- migraciones de Subestaciones (back-test, bases locales viejas) a la numeración actual.
--
--   Antes                                   Ahora
--   V41 seed_inicial_subestaciones_civil    (eliminada: sus datos se quedan en la base)
--   V42 ajuste_es_programada_seed_...       (eliminada: ídem)
--   V43 Catalogo_frecuencia_administracion  V41
--   V44 mant_hallazgo_seguimiento           V42
--   V45 mant_ejecucion_edicion_cambios      V43
--   V46 mant_programacion_borrador_...      V44
--   V47 mant_actividad_nombre_corto         V45
--
-- Los archivos renombrados tienen el mismo contenido, así que su checksum no cambia: solo
-- hay que mover version y script. Sin esto el backend nuevo no arranca (Flyway falla la
-- validación: V41 con otro checksum y V46/V47 aplicadas sin archivo).
--
-- Uso: psql -v ON_ERROR_STOP=1 -d <base> -f despliegue/flyway_renumeracion_subestaciones.sql
-- No hace nada si la base ya tiene la numeración nueva.

\set ON_ERROR_STOP on
BEGIN;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM flyway_schema_history
                   WHERE version = '41' AND script = 'V41__seed_inicial_subestaciones_civil.sql') THEN
        RAISE NOTICE 'La base ya tiene la numeración nueva: no hay nada que ajustar';
        RETURN;
    END IF;

    DELETE FROM flyway_schema_history
    WHERE script IN ('V41__seed_inicial_subestaciones_civil.sql',
                     'V42__ajuste_es_programada_seed_subestaciones.sql');

    UPDATE flyway_schema_history
    SET version = (version::int - 2)::text,
        script  = 'V' || (version::int - 2) || substring(script FROM position('__' IN script))
    WHERE version IN ('43', '44', '45', '46', '47');

    RAISE NOTICE 'flyway_schema_history ajustada a la numeración nueva';
END $$;

SELECT version, script, checksum FROM flyway_schema_history
WHERE version::int >= 40 ORDER BY installed_rank;

COMMIT;
