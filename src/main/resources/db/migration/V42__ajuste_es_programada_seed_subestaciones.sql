-- V42 -- Corrección de es_programada/programacion_id en el seed de V41.
--
-- V41 vinculaba una ejecución a una cita solo si (estación, actividad, MES) coincidían
-- exactos con mant_programacion. Al revisar el resultado (1/34 'programada'), se vio que
-- en la mayoría de los casos la estación SÍ tiene una cita real para esa actividad en 2026,
-- solo que en un mes distinto al que realmente se ejecutó -- no sabemos por qué (posible
-- adelanto/atraso de campo), y se acepta tal cual sin reconstruir el motivo (decisión del
-- usuario). Se decidió entonces relajar el criterio: si existe CUALQUIER cita 2026 para esa
-- (estación, actividad) sin importar el mes, se marca es_programada=TRUE y se enlaza a la
-- cita de mes más cercano al real. Las combinaciones (estación, actividad) que NO tienen
-- ninguna cita en todo el año (12 de las 34, ver nota abajo) se dejan como estaban --no hay
-- nada real con qué vincularlas.
--
-- Las 12 que quedan sin vincular son todas de 'Inspección y mantenimiento cerchas/pasos
-- elevados/limpieza malezas (segun estado)' en estaciones que el cronograma 2026 nunca
-- programa para esa actividad: CLAN (2), Dren Ayalas (2), Surba (2) y los 6 Dren restantes
-- (Dren chorrito/Cuche/Duitama/Jardines/Suescun/Tocogua, todas de mayo) -- trabajo real de
-- campo fuera del alcance de la programación oficial, no un error de captura.

-- 22 ejecuciones se re-vinculan a una cita real (mes más cercano):

UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 5 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-01-22'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 5 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-01-24'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 4 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-01-28'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 5 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-02-21'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-22'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-23'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 2 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Duitama') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-24'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Duitama')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 3 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-25'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-26'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 12 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-03-27'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 3 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-04-20'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 12 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-04-21'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 3 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Cuche') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-04-23'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Cuche')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-05-18'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 12 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Cuche') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-05-20'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Cuche')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 5 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-06-22'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-06-24'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 8 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-07-15'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 11 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-08-13'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 11 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Duitama') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-07-17'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Duitama')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 10 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-08-24'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));
UPDATE mant_ejecucion SET
    es_programada = TRUE,
    programacion_id = (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 11 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')))
WHERE fecha = '2026-08-26'
  AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas')
  AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'));

-- 12 ejecuciones quedan sin cita real que las respalde -- se dejan como en V41
-- (es_programada = FALSE, programacion_id = NULL). Listado para trazabilidad, sin efecto:
--   2026-02-18 | CLAN | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-03-18 | Dren Ayalas | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-03-28 | Surba | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-04-24 | Surba | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-19 | CLAN | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-21 | Dren Ayalas | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-22 | Dren chorrito | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-23 | Dren Cuche | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-24 | Dren Duitama | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-25 | Dren Jardines | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-26 | Dren Suescun | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
--   2026-05-27 | Dren Tocogua | Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)
