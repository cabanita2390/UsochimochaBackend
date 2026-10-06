-- SUB-07: modelo borrador/publicado del cronograma anual.
--
-- Lo que asigna el ADMIN en el Cronograma queda en BORRADOR y el móvil no lo ve hasta que
-- se publica. El móvil arma su Cronograma, Pendientes y Home con
-- GET /indicadores/cumplimiento, que sale de v_mant_cumplimiento: por eso el filtro
-- "solo publicado" vive en esa vista.

-- Cada publicación del cronograma de un año: "Publicado a móvil · fecha",
-- "Deshacer última publicación" (una a la vez) e historial.
-- inicial = TRUE marca la "Carga inicial" (las citas que ya existían al migrar): no tiene
-- usuario y nunca se puede deshacer.
CREATE TABLE mant_publicacion (
    id             BIGSERIAL  PRIMARY KEY,
    anio           SMALLINT   NOT NULL,
    usuario_id     BIGINT     REFERENCES users(id),
    publicado_en   TIMESTAMP  NOT NULL DEFAULT now(),
    altas          INT        NOT NULL,
    bajas          INT        NOT NULL,
    inicial        BOOLEAN    NOT NULL DEFAULT FALSE,
    revertida      BOOLEAN    NOT NULL DEFAULT FALSE,
    revertida_por  BIGINT     REFERENCES users(id),
    revertida_en   TIMESTAMP,
    CONSTRAINT mant_publicacion_usuario_salvo_inicial CHECK (inicial OR usuario_id IS NOT NULL),
    CONSTRAINT mant_publicacion_inicial_no_revertida CHECK (NOT (inicial AND revertida))
);
CREATE INDEX idx_mant_publicacion_anio ON mant_publicacion(anio, id DESC);

-- Estado de cada cita:
--   PUBLICADA -> la ven el móvil, el Dashboard y el Resumen.
--   BORRADOR  -> alta nueva sin publicar; solo la ve el Cronograma (azul "+").
--   RETIRADA  -> se quitó en una publicación; nadie la ve. Se conserva por historial y
--                porque puede haber ejecuciones que apunten a ella.
-- pendiente_retiro = TRUE sobre una PUBLICADA -> "Se quitará al publicar" (tachada); el
-- móvil la sigue viendo hasta que se publique.
ALTER TABLE mant_programacion
    ADD COLUMN estado VARCHAR(12) NOT NULL DEFAULT 'PUBLICADA'
        CHECK (estado IN ('BORRADOR', 'PUBLICADA', 'RETIRADA')),
    ADD COLUMN pendiente_retiro BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN publicada_en_id  BIGINT REFERENCES mant_publicacion(id),
    ADD COLUMN retirada_en_id   BIGINT REFERENCES mant_publicacion(id),
    ADD COLUMN creada_por       BIGINT REFERENCES users(id),
    ADD CONSTRAINT mant_programacion_retiro_solo_publicada
        CHECK (NOT pendiente_retiro OR estado = 'PUBLICADA');
-- Todas las citas actuales quedan PUBLICADA por el DEFAULT: el móvil no nota nada.

-- El UNIQUE original impediría volver a asignar una cita que se retiró o se descartó
-- (misma estación + actividad + mes + año). Se reemplaza por uno que solo cuenta las
-- citas vigentes.
ALTER TABLE mant_programacion
    DROP CONSTRAINT mant_programacion_anio_mes_actividad_id_estacion_id_key;
CREATE UNIQUE INDEX ux_mant_programacion_vigente
    ON mant_programacion (anio, mes, actividad_id, estacion_id)
    WHERE status = TRUE AND estado <> 'RETIRADA';

CREATE INDEX idx_mant_programacion_anio_estado ON mant_programacion(anio, estado) WHERE status;

-- Carga inicial: una publicación por año con citas vigentes, enlazada a esas citas.
INSERT INTO mant_publicacion (anio, usuario_id, publicado_en, altas, bajas, inicial)
SELECT anio, NULL, now(), COUNT(*), 0, TRUE
FROM mant_programacion
WHERE status = TRUE
GROUP BY anio;

UPDATE mant_programacion p
SET publicada_en_id = pub.id
FROM mant_publicacion pub
WHERE pub.inicial = TRUE
  AND pub.anio = p.anio
  AND p.status = TRUE;

-- ESTO ES LO QUE PROTEGE AL MÓVIL: solo lo publicado. Se agrega fecha_ejecucion al final
-- (primera ejecución de la cita) para el chip "Ejecutada dd/mm" del Cronograma.
CREATE OR REPLACE VIEW v_mant_cumplimiento AS
SELECT
    p.id                AS programacion_id,
    p.anio,
    p.mes,
    e.id                AS estacion_id,
    e.nombre            AS estacion_nombre,
    e.tipo              AS estacion_tipo,
    a.id                AS actividad_id,
    a.nombre            AS actividad_nombre,
    d.codigo            AS disciplina,
    COUNT(ej.id)        AS ejecutado,
    (COUNT(ej.id) > 0)  AS cumple,
    MIN(ej.fecha)       AS fecha_ejecucion
FROM mant_programacion p
JOIN mant_estacion e ON e.id = p.estacion_id
JOIN mant_actividad a ON a.id = p.actividad_id
JOIN mant_disciplina d ON d.id = a.disciplina_id
LEFT JOIN mant_ejecucion ej ON ej.programacion_id = p.id
WHERE p.status = TRUE
  AND p.estado = 'PUBLICADA'
GROUP BY p.id, p.anio, p.mes, e.id, e.nombre, e.tipo, a.id, a.nombre, d.codigo;
