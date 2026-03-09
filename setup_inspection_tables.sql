-- Cabecera de inspección (nombre exacto que usa Hibernate)
CREATE TABLE IF NOT EXISTS "Inspección_pre_operativa" (
    id_inspeccion         BIGSERIAL PRIMARY KEY,
    fecha_registro        TIMESTAMP,
    id_vehiculo           INT,
    responsable_inspeccion VARCHAR(100) NOT NULL,
    kilometraje_reportado  INT NOT NULL DEFAULT 0,
    aprobado_ruta         BOOLEAN,
    observaciones_finales TEXT
);

-- Detalle mecánico
CREATE TABLE IF NOT EXISTS insp_detalle_mecanico (
    id_mecanico        BIGSERIAL PRIMARY KEY,
    id_inspeccion      BIGINT,
    nivel_aceite       VARCHAR(50),
    nivel_refrigerante VARCHAR(50),
    nivel_frenos       VARCHAR(50),
    estado_llantas     VARCHAR(50),
    luces_general      VARCHAR(50),
    estado_visual      VARCHAR(50),
    limpieza_general   VARCHAR(50)
);

-- Detalle documentos
CREATE TABLE IF NOT EXISTS insp_detalle_documentos (
    id_documentos  BIGSERIAL PRIMARY KEY,
    id_inspeccion  BIGINT,
    check_soat     VARCHAR(50),
    check_tecno    VARCHAR(50),
    check_licencia VARCHAR(50)
);

-- Detalle elementos de seguridad
CREATE TABLE IF NOT EXISTS insp_detalle_elementos (
    id_elementos          BIGSERIAL PRIMARY KEY,
    id_inspeccion         BIGINT,
    tiene_botiquin        BOOLEAN,
    "tiene_señalizacion"  BOOLEAN,
    tiene_extintor        BOOLEAN,
    tiene_llanta_repuesto VARCHAR(10),
    tiene_gato_hidraulico VARCHAR(10)
);

-- Detalle salud conductor
CREATE TABLE IF NOT EXISTS insp_detalle_salud (
    id_salud                   BIGSERIAL PRIMARY KEY,
    id_inspeccion              BIGINT,
    salud_fisica               BOOLEAN,
    salud_mental               BOOLEAN,
    sobrio                     BOOLEAN,
    medicamentos               BOOLEAN,
    condicion_para_conducir    BOOLEAN,
    consciente_responsabilidad BOOLEAN
);

-- Documentación y elementos (fechas de vencimiento + fotos)
CREATE TABLE IF NOT EXISTS documentacion_y_elementos (
    id_documento    SERIAL PRIMARY KEY,
    id_vehiculo     INT,
    tipo_documento  VARCHAR(50),
    fecha_vencimiento DATE NOT NULL,
    imagen_url      VARCHAR(255),
    "estadoDatos"   VARCHAR(255),
    fecha_extintor  DATE,
    activo          BOOLEAN,
    "mesAño"        DATE
);

SELECT 'Tablas de inspección creadas correctamente' AS resultado;
