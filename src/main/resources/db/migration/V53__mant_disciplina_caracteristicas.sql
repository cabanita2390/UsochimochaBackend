-- Lo que hace cada disciplina pasa a la base: la web y el móvil lo leen de
-- GET /api/v1/substation/disciplinas en vez de tenerlo escrito en su código.
--   nombre         nombre visible ("Eléctrico").
--   sigla          letra de las celdas del cronograma ("E").
--   orden          orden en listas y filtros.
--   captura_movil  el móvil permite registrar visitas de la disciplina.
--   conjuntos      NO / OPCIONAL / OBLIGATORIO: paso "¿Qué se intervino?" con conjuntos.
--   usa_elementos  ese paso pide también elementos.
--   usa_equipos    la visita marca equipos (Electromecánico, todavía sin construir).
--   usa_controles  la visita lleva controles: alineación, sellos, rodamientos (ídem).
-- Electromecánico queda sin captura hasta que se construya su formulario.

ALTER TABLE mant_disciplina ADD COLUMN nombre VARCHAR(60);
ALTER TABLE mant_disciplina ADD COLUMN sigla VARCHAR(3);
ALTER TABLE mant_disciplina ADD COLUMN orden SMALLINT NOT NULL DEFAULT 0;
ALTER TABLE mant_disciplina ADD COLUMN captura_movil BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mant_disciplina ADD COLUMN conjuntos VARCHAR(12) NOT NULL DEFAULT 'NO'
    CHECK (conjuntos IN ('NO', 'OPCIONAL', 'OBLIGATORIO'));
ALTER TABLE mant_disciplina ADD COLUMN usa_elementos BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mant_disciplina ADD COLUMN usa_equipos BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE mant_disciplina ADD COLUMN usa_controles BOOLEAN NOT NULL DEFAULT FALSE;

UPDATE mant_disciplina SET nombre = 'Civil', sigla = 'C', orden = 1, captura_movil = TRUE
WHERE codigo = 'CIVIL';
UPDATE mant_disciplina SET nombre = 'Eléctrico', sigla = 'E', orden = 2, captura_movil = TRUE,
    conjuntos = 'OPCIONAL', usa_elementos = TRUE
WHERE codigo = 'ELECTRICO';
UPDATE mant_disciplina SET nombre = 'Electromecánico', sigla = 'M', orden = 3, captura_movil = FALSE,
    conjuntos = 'OBLIGATORIO', usa_elementos = TRUE
WHERE codigo = 'ELECTROMECANICO';
-- Cualquier otra que exista (no debería): su código como nombre, al final.
UPDATE mant_disciplina SET nombre = codigo, sigla = left(codigo, 1), orden = 99 WHERE nombre IS NULL;

ALTER TABLE mant_disciplina ALTER COLUMN nombre SET NOT NULL;
ALTER TABLE mant_disciplina ALTER COLUMN sigla SET NOT NULL;

-- Nombres de catálogo: "Bomba" y "bomba" (o "Papúas" y "Papuas") no pueden convivir. El servicio
-- ya lo valida al crear y editar; esto lo asegura también ante dos altas simultáneas. Si en una
-- base ya hay repetidos, el índice no se crea y queda un aviso: hay que unirlos a mano y crearlo.
CREATE OR REPLACE FUNCTION mant_clave_nombre(texto TEXT) RETURNS TEXT
    LANGUAGE sql IMMUTABLE PARALLEL SAFE AS
$$ SELECT translate(lower(regexp_replace(btrim(texto), '\s+', ' ', 'g')),
                    'áàäâãéèëêíìïîóòöôõúùüûñç', 'aaaaaeeeeiiiiooooouuuunc') $$;

DO $$
DECLARE
    t RECORD;
    repetidos INT;
BEGIN
    FOR t IN SELECT * FROM (VALUES
            ('mant_estacion',  'uq_mant_estacion_clave_nombre',  'mant_clave_nombre(nombre)'),
            ('mant_actividad', 'uq_mant_actividad_clave_nombre', 'disciplina_id, mant_clave_nombre(nombre)'),
            ('mant_conjunto',  'uq_mant_conjunto_clave_nombre',  'disciplina_id, mant_clave_nombre(nombre)'),
            ('mant_elemento',  'uq_mant_elemento_clave_nombre',  'disciplina_id, mant_clave_nombre(nombre)')
        ) AS v(tabla, indice, columnas)
    LOOP
        EXECUTE format('SELECT count(*) FROM (SELECT 1 FROM %I GROUP BY %s HAVING count(*) > 1) r',
                       t.tabla, t.columnas) INTO repetidos;
        IF repetidos = 0 THEN
            EXECUTE format('CREATE UNIQUE INDEX %I ON %I (%s)', t.indice, t.tabla, t.columnas);
        ELSE
            RAISE WARNING '%: % nombres repetidos sin distinguir mayúsculas ni tildes; no se crea %',
                t.tabla, repetidos, t.indice;
        END IF;
    END LOOP;
END $$;
