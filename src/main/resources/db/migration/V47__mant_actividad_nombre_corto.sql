-- SUB-13: nombre corto de la actividad para los chips de la grilla del Cronograma
-- (celdas de ~104 px). Sin códigos de estación ni actividad (decisión P1/D3).
-- Si es NULL, la web recorta el nombre completo con "…".
ALTER TABLE mant_actividad ADD COLUMN nombre_corto VARCHAR(24);

-- Propuesta para las actividades Civil del seed V41; se editan desde Configuración.
UPDATE mant_actividad a
SET nombre_corto = v.corto
FROM (VALUES
    ('Pintura puertas/ventanas/barandas estaciones', 'Pintura puertas'),
    ('Pintura muros estaciones (segun estado)', 'Pintura muros'),
    ('Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)', 'Cerchas y pasos'),
    ('Inspección y mantenimiento compuertas + limpieza pozos succión', 'Compuertas'),
    ('Inspección infraestructura presa La Copa y La Playa', 'Presas'),
    ('Inspección sede administrativa/ CLAN', 'Insp. sede CLAN'),
    ('Corrección hallazgos sede administrativa/ CLAN', 'Corr. sede CLAN'),
    ('Inspección maquinaria amarilla', 'Maquinaria'),
    ('Inspección vehículos y motocicletas', 'Vehículos')
) AS v(nombre, corto)
WHERE a.nombre = v.nombre
  AND a.disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL');
