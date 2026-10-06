-- Carga inicial de datos reales de Estaciones de Bombeo (disciplina Civil) para una base
-- que ya tiene todas las migraciones aplicadas pero ningún dato del módulo: producción.
--
-- Contenido (el mismo que tenían los seeds V41/V42, retirados de las migraciones porque
-- dependían de que existiera el usuario id=1):
--   23 estaciones, 9 actividades Civil, 65 citas del cronograma 2026 ya publicadas,
--   34 ejecuciones históricas (22 enlazadas a su cita) y el seguimiento ABIERTO de las que
--   reportaron hallazgos. Además crea la publicación "Carga inicial" del año, para que el
--   Cronograma muestre "Publicado a móvil · fecha" y no se pueda "deshacer" la carga.
--
-- Uso (una sola vez; aborta sin cambiar nada si ya hay estaciones cargadas):
--   psql -v ON_ERROR_STOP=1 -v usuario=<username que firma los 34 registros> \
--        -d <base> -f despliegue/carga_inicial_subestaciones.sql
--
-- Las 34 ejecuciones quedan a nombre de ese usuario (debe existir y estar activo).

\set ON_ERROR_STOP on
BEGIN;

SELECT set_config('carga.usuario', :'usuario', true);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM mant_estacion) THEN
        RAISE EXCEPTION 'Ya hay estaciones cargadas: este script es solo para una base sin datos de Subestaciones';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM users WHERE username = current_setting('carga.usuario') AND status) THEN
        RAISE EXCEPTION 'El usuario % no existe o está inactivo', current_setting('carga.usuario');
    END IF;
END $$;

-- 1) Estaciones (23)
INSERT INTO mant_estacion (nombre, tipo, frecuencia_base, status) VALUES
    ('Ayalas', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('CLAN', 'COMPLEMENTARIA', 'ANUAL', TRUE),
    ('Cuche', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Ayalas', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren chorrito', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Cuche', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Duitama', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Jardines', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Suescun', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Dren Tocogua', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Duitama', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Fuente Salinas', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Holanda', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('La Copa', 'COMPLEMENTARIA', 'ANUAL', TRUE),
    ('La Playa', 'COMPLEMENTARIA', 'ANUAL', TRUE),
    ('Las Vueltas', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Ministerio', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Monquira', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Pantano de Vargas', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('San Rafael', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Sede Administrativa', 'COMPLEMENTARIA', 'ANUAL', TRUE),
    ('Surba', 'BOMBEO', 'TRIMESTRAL', TRUE),
    ('Tibasosa', 'BOMBEO', 'TRIMESTRAL', TRUE);

-- 2) Actividades Civil (9, todas con captura móvil habilitada)
INSERT INTO mant_actividad (nombre, disciplina_id, captura_movil_habilitada, status) VALUES
    ('Pintura puertas/ventanas/barandas estaciones', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Pintura muros estaciones (segun estado)', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección y mantenimiento compuertas + limpieza pozos succión', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección infraestructura presa La Copa y La Playa', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección sede administrativa/ CLAN', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Corrección hallazgos sede administrativa/ CLAN', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección maquinaria amarilla', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE),
    ('Inspección vehículos y motocicletas', (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), TRUE, TRUE);

-- 3) Programación 2026 (65 citas Civil, desde CRONOGRAMA_TABLA_OK)
INSERT INTO mant_programacion (anio, mes, estacion_id, actividad_id, status) VALUES
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Holanda'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Fuente Salinas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Monquira'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 7, (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 7, (SELECT id FROM mant_estacion WHERE nombre = 'Fuente Salinas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'Holanda'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 9, (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 9, (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Monquira'), (SELECT id FROM mant_actividad WHERE nombre = 'Pintura muros estaciones (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 4, (SELECT id FROM mant_estacion WHERE nombre = 'Holanda'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 4, (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 4, (SELECT id FROM mant_estacion WHERE nombre = 'Fuente Salinas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 8, (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Monquira'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 1, (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 1, (SELECT id FROM mant_estacion WHERE nombre = 'Holanda'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 1, (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 1, (SELECT id FROM mant_estacion WHERE nombre = 'Fuente Salinas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 2, (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 3, (SELECT id FROM mant_estacion WHERE nombre = 'Monquira'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'Holanda'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 10, (SELECT id FROM mant_estacion WHERE nombre = 'Fuente Salinas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 11, (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 12, (SELECT id FROM mant_estacion WHERE nombre = 'Monquira'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 6, (SELECT id FROM mant_estacion WHERE nombre = 'La Copa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección infraestructura presa La Copa y La Playa' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 6, (SELECT id FROM mant_estacion WHERE nombre = 'La Playa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección infraestructura presa La Copa y La Playa' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 5, (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 7, (SELECT id FROM mant_estacion WHERE nombre = 'CLAN'), (SELECT id FROM mant_actividad WHERE nombre = 'Inspección sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 5, (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE),
    (2026, 7, (SELECT id FROM mant_estacion WHERE nombre = 'CLAN'), (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), TRUE);

-- 4) Ejecuciones históricas 2026 (34 registros reales, deduplicados)
INSERT INTO mant_ejecucion (fecha, mes_ejecucion, semana_ejecucion, usuario_id, estacion_id,
    disciplina_id, tipo_mantenimiento, tipo_actividad, actividad_id, programacion_id,
    es_programada, motivo_no_catalogado, resultado, observaciones, descripcion_libre, uuid_cliente) VALUES
    ('2026-01-22', 1, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Puertas talleres / bodegas', NULL, gen_random_uuid()),
    ('2026-01-24', 1, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Puerta baño / muros', NULL, gen_random_uuid()),
    ('2026-01-28', 1, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Rampa de ingreso', NULL, gen_random_uuid()),
    ('2026-02-18', 2, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'CLAN'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Techo / cerramiento anti-palomas', NULL, gen_random_uuid()),
    ('2026-02-21', 2, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Iluminación: Luminarias parqueadero', NULL, gen_random_uuid()),
    ('2026-03-18', 3, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Reja inferior dren', NULL, gen_random_uuid()),
    ('2026-03-22', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento de pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-03-23', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Pintura y mantenimiento de cerramiento de estación', NULL, gen_random_uuid()),
    ('2026-03-24', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento y pintura de cerramiento, puertas y ventanas', NULL, gen_random_uuid()),
    ('2026-03-25', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), (SELECT id FROM mant_programacion WHERE anio = 2026 AND mes = 3 AND estacion_id = (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa') AND actividad_id = (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'))), TRUE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento y pintura de cerramiento, puertas y ventanas', NULL, gen_random_uuid()),
    ('2026-03-26', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'San Rafael'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento de pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-03-27', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento y pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-03-28', 3, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Mantenimiento de pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-04-20', 4, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Tibasosa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Pintura al cerramiento, desyerbe, pintura puertas y ventanas y poda', NULL, gen_random_uuid()),
    ('2026-04-21', 4, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ministerio'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-04-23', 4, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Pintura puertas/ventanas/barandas estaciones' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Pintura al cerramiento, desyerbe, pintura puertas y ventanas y poda', NULL, gen_random_uuid()),
    ('2026-04-24', 4, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Surba'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Pintura al cerramiento y desyerbe zona colindante', NULL, gen_random_uuid()),
    ('2026-05-18', 5, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-19', 5, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'CLAN'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-20', 5, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Cuche'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-21', 5, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-22', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren chorrito'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-23', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Cuche'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-24', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Duitama'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-25', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Jardines'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-26', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Suescun'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-05-27', 5, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Dren Tocogua'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Tapas cárcamos / cámaras / alambre', NULL, gen_random_uuid()),
    ('2026-06-22', 6, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Sede Administrativa'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Corrección hallazgos sede administrativa/ CLAN' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Cuarto combustibles: Filtros y lubricantes', NULL, gen_random_uuid()),
    ('2026-06-24', 6, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Ventanas: Enrejado de ventanas', NULL, gen_random_uuid()),
    ('2026-07-15', 7, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento cerchas/pasos elevados/limpieza malezas (segun estado)' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Se realiza muro para elevar cerramiento', NULL, gen_random_uuid()),
    ('2026-08-13', 7, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Las Vueltas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Se realiza limpieza de pozo succión', NULL, gen_random_uuid()),
    ('2026-07-17', 7, 3, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Duitama'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Limpieza de pozo', NULL, gen_random_uuid()),
    ('2026-08-24', 8, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Pantano de Vargas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'PREVENTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CONFORME', 'Infraestructura: Limpieza pozo succión', NULL, gen_random_uuid()),
    ('2026-08-26', 8, 4, (SELECT id FROM users WHERE username = :'usuario'), (SELECT id FROM mant_estacion WHERE nombre = 'Ayalas'), (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL'), 'CORRECTIVO', 'MANTENIMIENTO', (SELECT id FROM mant_actividad WHERE nombre = 'Inspección y mantenimiento compuertas + limpieza pozos succión' AND disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'CIVIL')), NULL, FALSE, NULL, 'CON_HALLAZGOS', 'Infraestructura: Mantenimiento compuerta pozo succión', NULL, gen_random_uuid());

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

-- Carga inicial: publicación del año enlazada a sus citas (igual que V44 en una base con datos).
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

-- Todo hallazgo arranca ABIERTO (igual que V42 y que registrarEjecucion).
INSERT INTO mant_hallazgo_seguimiento (ejecucion_id, estado)
SELECT id, 'ABIERTO'
FROM mant_ejecucion
WHERE resultado <> 'CONFORME';

SELECT (SELECT count(*) FROM mant_estacion)     AS estaciones,
       (SELECT count(*) FROM mant_actividad)    AS actividades,
       (SELECT count(*) FROM mant_programacion) AS citas,
       (SELECT count(*) FROM mant_ejecucion)    AS ejecuciones,
       (SELECT count(*) FROM mant_publicacion WHERE inicial) AS cargas_iniciales;

COMMIT;
