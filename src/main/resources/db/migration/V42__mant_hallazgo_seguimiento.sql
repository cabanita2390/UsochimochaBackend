-- SUB-03: seguimiento de una ejecución con resultado CON_HALLAZGOS o REQUIERE_INTERVENCION.
-- Ciclo: ABIERTO -> EN_PROCESO -> RESUELTO, o ABIERTO -> RESUELTO directo.
-- Si la ejecución se edita a CONFORME, el seguimiento pasa a
-- status = false (soft-delete); si vuelve a tener hallazgo, se reactiva en ABIERTO.
CREATE TABLE mant_hallazgo_seguimiento (
    id                        BIGSERIAL    PRIMARY KEY,
    ejecucion_id              BIGINT       NOT NULL UNIQUE REFERENCES mant_ejecucion(id),
    estado                    VARCHAR(20)  NOT NULL DEFAULT 'ABIERTO'
                              CHECK (estado IN ('ABIERTO', 'EN_PROCESO', 'RESUELTO')),
    resuelto_en_ejecucion_id  BIGINT       REFERENCES mant_ejecucion(id),
    resuelto_misma_visita     BOOLEAN      NOT NULL DEFAULT FALSE,
    observaciones_cierre      TEXT,
    cerrado_por               BIGINT       REFERENCES users(id),
    cerrado_en                TIMESTAMP,
    actualizado_por           BIGINT       REFERENCES users(id),
    actualizado_en            TIMESTAMP    NOT NULL DEFAULT now(),
    status                    BOOLEAN      NOT NULL DEFAULT TRUE,
    -- Un hallazgo resuelto siempre tiene observación y responsable.
    CONSTRAINT mant_hallazgo_resuelto_completo CHECK (
        estado <> 'RESUELTO'
        OR (observaciones_cierre IS NOT NULL AND cerrado_por IS NOT NULL AND cerrado_en IS NOT NULL)
    ),
    -- O se resolvió en la misma visita, o en otra ejecución, o no se indicó; nunca ambas.
    CONSTRAINT mant_hallazgo_resolucion_unica CHECK (
        NOT (resuelto_misma_visita AND resuelto_en_ejecucion_id IS NOT NULL)
    ),
    CONSTRAINT mant_hallazgo_no_autorreferencia CHECK (
        resuelto_en_ejecucion_id IS NULL OR resuelto_en_ejecucion_id <> ejecucion_id
    )
);

CREATE INDEX idx_mant_hallazgo_estado ON mant_hallazgo_seguimiento(estado) WHERE status;

-- D8: las ejecuciones con hallazgo que ya existen arrancan ABIERTO.
INSERT INTO mant_hallazgo_seguimiento (ejecucion_id, estado)
SELECT id, 'ABIERTO'
FROM mant_ejecucion
WHERE resultado <> 'CONFORME';
