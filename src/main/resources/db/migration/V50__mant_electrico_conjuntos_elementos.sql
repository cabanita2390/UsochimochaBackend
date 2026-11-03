-- Disciplina Eléctrico.
-- El formulario eléctrico pide, además de lo que ya registra Civil, el "tipo de conjunto
-- intervenido" y los "elementos inspeccionados o intervenidos", ambos
-- de selección múltiple, opcionales y con "Otros". "No aplica" = no marcar nada.
-- Las listas son genéricas por disciplina (igual para todas las estaciones) y se administran
-- desde Configuración, como las estaciones y las actividades.

-- --- Conjuntos (mismo patrón que mant_elemento) ---------------------------

CREATE TABLE mant_conjunto (
    id            BIGSERIAL    PRIMARY KEY,
    nombre        VARCHAR(200) NOT NULL,
    disciplina_id BIGINT       NOT NULL REFERENCES mant_disciplina(id),
    status        BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT mant_conjunto_nombre_disciplina_id_key UNIQUE (nombre, disciplina_id)
);
CREATE INDEX idx_mant_conjunto_disciplina_id ON mant_conjunto(disciplina_id);

-- --- Qué se intervino en cada visita (las tablas que V34 dejó pendientes) --

CREATE TABLE mant_ejecucion_conjunto (
    ejecucion_id BIGINT NOT NULL REFERENCES mant_ejecucion(id),
    conjunto_id  BIGINT NOT NULL REFERENCES mant_conjunto(id),
    PRIMARY KEY (ejecucion_id, conjunto_id)
);
CREATE INDEX idx_mant_ejecucion_conjunto_conjunto ON mant_ejecucion_conjunto(conjunto_id);

CREATE TABLE mant_ejecucion_elemento (
    ejecucion_id BIGINT NOT NULL REFERENCES mant_ejecucion(id),
    elemento_id  BIGINT NOT NULL REFERENCES mant_elemento(id),
    PRIMARY KEY (ejecucion_id, elemento_id)
);
CREATE INDEX idx_mant_ejecucion_elemento_elemento ON mant_ejecucion_elemento(elemento_id);

-- Texto de la opción "Otros" del formulario; NULL si no se usó.
ALTER TABLE mant_ejecucion ADD COLUMN conjunto_otro VARCHAR(200);
ALTER TABLE mant_ejecucion ADD COLUMN elemento_otro VARCHAR(200);

-- --- Catálogos eléctricos iniciales (se editan después desde Configuración) ---
-- ON CONFLICT: si alguien ya creó alguno desde la web con el mismo nombre, no se duplica.

INSERT INTO mant_conjunto (nombre, disciplina_id)
SELECT v.nombre, d.id
FROM (VALUES
    ('Subestación'),
    ('Gabinetes de control'),
    ('Red de media tensión'),
    ('Generador VAR'),
    ('Iluminación'),
    ('Motores'),
    ('Transformador'),
    ('Postes')
) AS v(nombre)
CROSS JOIN mant_disciplina d
WHERE d.codigo = 'ELECTRICO'
ON CONFLICT (nombre, disciplina_id) DO NOTHING;

INSERT INTO mant_elemento (nombre, disciplina_id, provisional)
SELECT v.nombre, d.id, FALSE
FROM (VALUES
    ('Variador de velocidad'),
    ('Arrancador suave'),
    ('PLC / sistema de control'),
    ('Breaker'),
    ('Contactores'),
    ('Relés de protección'),
    ('Transformador'),
    ('Sistema de puesta a tierra'),
    ('Cableado y conexiones'),
    ('Sensores / instrumentación'),
    ('Gabinete eléctrico'),
    ('Motor eléctrico'),
    ('Generador VAR'),
    ('Red media tensión'),
    ('Arranque'),
    ('Subestación'),
    ('Iluminación'),
    ('Reconectador'),
    ('Postes')
) AS v(nombre)
CROSS JOIN mant_disciplina d
WHERE d.codigo = 'ELECTRICO'
ON CONFLICT (nombre, disciplina_id) DO NOTHING;

-- Actividades del cronograma anual eléctrico, habilitadas para el móvil.
INSERT INTO mant_actividad (nombre, disciplina_id, nombre_corto, captura_movil_habilitada)
SELECT v.nombre, d.id, v.corto, TRUE
FROM (VALUES
    ('Inspección eléctrica y electrónica general de estaciones', 'Insp. eléctrica general'),
    ('Limpieza cárcamos/retiro de polvo/ajuste de contactos (gabinetes, bornes)', 'Limpieza y contactos'),
    ('Limpieza subestaciones/verificar tierra/nivel aceite transformador', 'Subestación y trafo'),
    ('Inspección y arreglo de hallazgos línea MT', 'Línea MT')
) AS v(nombre, corto)
CROSS JOIN mant_disciplina d
WHERE d.codigo = 'ELECTRICO'
ON CONFLICT (nombre, disciplina_id) DO NOTHING;
