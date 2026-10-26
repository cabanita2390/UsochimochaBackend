-- Presión de llantas en la inspección preoperativa (vehículos, motos y maquinaria).
-- Es un reporte del estado: una presión fuera de rango no bloquea la inspección ni genera
-- órdenes ni notificaciones; calibrar es tarea del operario. Unidad: PSI en todo el sistema.

-- Perfil reutilizable ("Moto 2 llantas", "Volqueta 10 llantas", "Retro 4x4"...): las
-- posiciones y su rango de presión se definen una vez y se asignan a muchos activos.
CREATE TABLE llanta_perfil (
    id              BIGSERIAL     PRIMARY KEY,
    nombre          VARCHAR(100)  NOT NULL,
    aplica_a        VARCHAR(15)   NOT NULL CHECK (aplica_a IN ('VEHICULO', 'MOTOCICLETA', 'MAQUINARIA')),
    descripcion     TEXT,
    status          BOOLEAN       NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP     NOT NULL DEFAULT now(),
    actualizado_en  TIMESTAMP     NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_llanta_perfil_nombre ON llanta_perfil(UPPER(nombre)) WHERE status;

CREATE TABLE llanta_perfil_posicion (
    id            BIGSERIAL     PRIMARY KEY,
    perfil_id     BIGINT        NOT NULL REFERENCES llanta_perfil(id),
    codigo        VARCHAR(30)   NOT NULL,
    etiqueta      VARCHAR(80)   NOT NULL,
    orden         INTEGER       NOT NULL,
    psi_min       NUMERIC(5,1)  NOT NULL,
    psi_objetivo  NUMERIC(5,1),
    psi_max       NUMERIC(5,1)  NOT NULL,
    status        BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT llanta_posicion_rango CHECK (psi_min > 0 AND psi_min <= psi_max),
    CONSTRAINT llanta_posicion_objetivo CHECK (psi_objetivo IS NULL OR psi_objetivo BETWEEN psi_min AND psi_max)
);
CREATE UNIQUE INDEX ux_llanta_posicion_codigo ON llanta_perfil_posicion(perfil_id, codigo) WHERE status;

-- Perfil asignado a cada activo (arco exclusivo, mismo patrón que asset_fuel_config).
-- Una fila por activo; quitar el perfil la desactiva (status = false).
CREATE TABLE asset_llanta_config (
    id          BIGSERIAL  PRIMARY KEY,
    vehicle_id  INTEGER    UNIQUE REFERENCES vehiculos(id_vehiculo),
    machine_id  BIGINT     UNIQUE REFERENCES machines(id),
    perfil_id   BIGINT     NOT NULL REFERENCES llanta_perfil(id),
    status      BOOLEAN    NOT NULL DEFAULT TRUE,
    CHECK (num_nonnulls(vehicle_id, machine_id) = 1)
);
CREATE INDEX idx_asset_llanta_config_perfil ON asset_llanta_config(perfil_id) WHERE status;

-- Lecturas de cada inspección. La etiqueta y el rango se copian al registrar: si después
-- cambia el perfil, el historial sigue mostrando contra qué se evaluó cada lectura.
CREATE TABLE insp_presion_llanta (
    id                      BIGSERIAL     PRIMARY KEY,
    id_inspeccion_vehiculo  BIGINT        REFERENCES inspeccion_pre_operativa(id_inspeccion),
    id_inspeccion_maquina   BIGINT        REFERENCES inspections(id),
    codigo_posicion         VARCHAR(30)   NOT NULL,
    etiqueta_posicion       VARCHAR(80)   NOT NULL,
    orden                   INTEGER,
    psi_medido              NUMERIC(5,1)  NOT NULL,
    psi_min                 NUMERIC(5,1),
    psi_max                 NUMERIC(5,1),
    resultado               VARCHAR(10)   NOT NULL CHECK (resultado IN ('OK', 'BAJA', 'ALTA', 'SIN_RANGO')),
    creado_en               TIMESTAMP     NOT NULL DEFAULT now(),
    CHECK (num_nonnulls(id_inspeccion_vehiculo, id_inspeccion_maquina) = 1)
);
CREATE INDEX idx_insp_presion_llanta_vehiculo ON insp_presion_llanta(id_inspeccion_vehiculo);
CREATE INDEX idx_insp_presion_llanta_maquina ON insp_presion_llanta(id_inspeccion_maquina);
