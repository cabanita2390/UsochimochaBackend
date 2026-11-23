-- Cada conjunto es una caja con sus elementos adentro (ajuste de Eléctrico, 2026-11-23).
--   mant_elemento sigue siendo la lista de NOMBRES reutilizables ("Fusibles" una sola vez).
--   mant_conjunto_elemento dice qué elementos tiene cada conjunto.
--   mant_ejecucion_elemento guarda, por visita, cada elemento marcado con su conjunto.
-- Ver Documentos-estaciones/02-planes-desarrollo/electrico/ajuste-conjuntos-elementos.md.

CREATE TABLE mant_conjunto_elemento (
    id           BIGSERIAL PRIMARY KEY,
    conjunto_id  BIGINT    NOT NULL REFERENCES mant_conjunto(id),
    elemento_id  BIGINT    NOT NULL REFERENCES mant_elemento(id),
    -- Al marcarlo el operario escribe algo (ej. "Estado físico" del Transformador: cómo se encuentra).
    pide_detalle BOOLEAN   NOT NULL DEFAULT FALSE,
    orden        SMALLINT  NOT NULL DEFAULT 0,
    -- Lo agregó un operario con "Otro" desde el móvil: el administrador lo revisa en Configuración.
    provisional  BOOLEAN   NOT NULL DEFAULT FALSE,
    -- Quitar un elemento de un conjunto es status = FALSE, nunca borrar (las visitas lo siguen usando).
    status       BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_mant_conjunto_elemento UNIQUE (conjunto_id, elemento_id)
);
CREATE INDEX idx_mant_conjunto_elemento_elemento ON mant_conjunto_elemento(elemento_id);

-- Lo marcado en la visita: el mismo elemento puede ir en dos conjuntos ("Fusibles" del
-- Transformador y de la Subestación), así que la llave pasa a un id propio.
ALTER TABLE mant_ejecucion_elemento DROP CONSTRAINT mant_ejecucion_elemento_pkey;
ALTER TABLE mant_ejecucion_elemento ADD COLUMN id BIGSERIAL PRIMARY KEY;
-- NULL solo en las visitas anteriores a este ajuste ("sin conjunto").
ALTER TABLE mant_ejecucion_elemento ADD COLUMN conjunto_id BIGINT REFERENCES mant_conjunto(id);
ALTER TABLE mant_ejecucion_elemento ADD COLUMN detalle VARCHAR(500);
CREATE UNIQUE INDEX uq_mant_ejecucion_elemento
    ON mant_ejecucion_elemento (ejecucion_id, COALESCE(conjunto_id, 0), elemento_id);
CREATE INDEX idx_mant_ejecucion_elemento_ejecucion ON mant_ejecucion_elemento(ejecucion_id);

-- En Eléctrico el conjunto es obligatorio: sin él no se sabe dónde se trabajó.
UPDATE mant_disciplina SET conjuntos = 'OBLIGATORIO' WHERE codigo = 'ELECTRICO';
