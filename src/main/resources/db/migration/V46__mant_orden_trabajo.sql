-- SUB-15: órdenes de trabajo de estación (OT-EB-00001) para corregir hallazgos.
-- Ciclo: ASIGNADA -> POR_VERIFICAR -> CERRADA, POR_VERIFICAR -> ASIGNADA (devuelta),
-- y ANULADA desde ASIGNADA o POR_VERIFICAR. No es una cita del cronograma: no cuenta en el
-- avance del año ni como imprevisto (ver DOCUMENTOS/CIVILWEB/ORDENES_TRABAJO_ESTACIONES.md).
CREATE TABLE mant_orden_trabajo (
    id                    BIGSERIAL     PRIMARY KEY,
    consecutivo           VARCHAR(20)   NOT NULL UNIQUE,
    ejecucion_origen_id   BIGINT        NOT NULL REFERENCES mant_ejecucion(id),
    estacion_id           BIGINT        NOT NULL REFERENCES mant_estacion(id),
    disciplina_id         BIGINT        NOT NULL REFERENCES mant_disciplina(id),
    actividad_id          BIGINT        NOT NULL REFERENCES mant_actividad(id),
    prioridad             VARCHAR(10)   NOT NULL CHECK (prioridad IN ('ALTA', 'MEDIA')),
    fecha_limite          DATE          NOT NULL,
    asignado_a            BIGINT        NOT NULL REFERENCES users(id),
    instrucciones         TEXT          NOT NULL,
    estado                VARCHAR(20)   NOT NULL DEFAULT 'ASIGNADA'
                          CHECK (estado IN ('ASIGNADA', 'POR_VERIFICAR', 'CERRADA', 'ANULADA')),
    ejecucion_cierre_id   BIGINT        REFERENCES mant_ejecucion(id),
    observacion_cierre    TEXT,
    verificado_por        BIGINT        REFERENCES users(id),
    verificado_en         TIMESTAMP,
    motivo_anulacion      TEXT,
    creado_por            BIGINT        NOT NULL REFERENCES users(id),
    creado_en             TIMESTAMP     NOT NULL DEFAULT now(),
    actualizado_en        TIMESTAMP     NOT NULL DEFAULT now(),
    status                BOOLEAN       NOT NULL DEFAULT TRUE,
    -- Una orden cerrada siempre tiene la visita que la corrigió y quién la verificó.
    CONSTRAINT mant_orden_cerrada_completa CHECK (
        estado <> 'CERRADA'
        OR (ejecucion_cierre_id IS NOT NULL AND observacion_cierre IS NOT NULL
            AND verificado_por IS NOT NULL AND verificado_en IS NOT NULL)
    ),
    CONSTRAINT mant_orden_anulada_con_motivo CHECK (estado <> 'ANULADA' OR motivo_anulacion IS NOT NULL)
);

-- Una orden vigente por hallazgo; si se anula, se le puede crear otra.
CREATE UNIQUE INDEX ux_mant_orden_vigente_por_hallazgo
    ON mant_orden_trabajo(ejecucion_origen_id) WHERE estado <> 'ANULADA' AND status;
CREATE INDEX idx_mant_orden_asignado ON mant_orden_trabajo(asignado_a, estado) WHERE status;
CREATE INDEX idx_mant_orden_estacion ON mant_orden_trabajo(estacion_id) WHERE status;

-- Línea de tiempo de la orden (también es su auditoría).
CREATE TABLE mant_orden_evento (
    id            BIGSERIAL    PRIMARY KEY,
    orden_id      BIGINT       NOT NULL REFERENCES mant_orden_trabajo(id),
    tipo          VARCHAR(30)  NOT NULL CHECK (tipo IN ('CREADA', 'EDITADA', 'EJECUTADA', 'DEVUELTA', 'CERRADA',
                                                        'ANULADA', 'COSTOS', 'EJECUCION_FUERA_DE_ESTADO')),
    comentario    TEXT,
    ejecucion_id  BIGINT       REFERENCES mant_ejecucion(id),
    usuario_id    BIGINT       REFERENCES users(id),
    creado_en     TIMESTAMP    NOT NULL DEFAULT now()
);
CREATE INDEX idx_mant_orden_evento_orden ON mant_orden_evento(orden_id);

-- Costos de la orden: mano de obra (propia o contratista) y materiales.
CREATE TABLE mant_orden_costo (
    id            BIGSERIAL      PRIMARY KEY,
    orden_id      BIGINT         NOT NULL REFERENCES mant_orden_trabajo(id),
    tipo          VARCHAR(10)    NOT NULL CHECK (tipo IN ('MANO_OBRA', 'MATERIAL')),
    descripcion   VARCHAR(200)   NOT NULL,
    proveedor     VARCHAR(200),
    cantidad      NUMERIC(12, 2) NOT NULL DEFAULT 1 CHECK (cantidad > 0),
    valor_total   NUMERIC(14, 2) NOT NULL CHECK (valor_total >= 0)
);
CREATE INDEX idx_mant_orden_costo_orden ON mant_orden_costo(orden_id);

-- La visita del operario que ejecuta una orden (no es imprevisto ni cita del cronograma).
ALTER TABLE mant_ejecucion ADD COLUMN orden_id BIGINT REFERENCES mant_orden_trabajo(id);
CREATE INDEX idx_mant_ejecucion_orden ON mant_ejecucion(orden_id) WHERE orden_id IS NOT NULL;

-- Consecutivo OT-EB-00001 con el mismo contador de las órdenes de maquinaria.
INSERT INTO order_counters (module_code, last_value) VALUES ('EB', 0) ON CONFLICT (module_code) DO NOTHING;
