-- Catálogos requeridos por el JOIN en VehicleRepository
CREATE TABLE IF NOT EXISTS cat_marcas_modelos (
    id_marca SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS cat_tipos_vehiculo (
    id_tipo_vehiculo SERIAL PRIMARY KEY,
    nombre_tipo VARCHAR(100) NOT NULL
);

-- Tabla principal de vehículos
CREATE TABLE IF NOT EXISTS vehiculos (
    id_vehiculo SERIAL PRIMARY KEY,
    placa VARCHAR(20) UNIQUE NOT NULL,
    id_marca INT REFERENCES cat_marcas_modelos(id_marca),
    id_tipo_vehiculo INT REFERENCES cat_tipos_vehiculo(id_tipo_vehiculo),
    activo BOOLEAN DEFAULT true,
    kilometraje_actual INT DEFAULT 0
);

-- Datos de prueba
INSERT INTO cat_marcas_modelos (descripcion)
VALUES ('Toyota'), ('Chevrolet'), ('Renault'), ('Mazda')
ON CONFLICT DO NOTHING;

INSERT INTO cat_tipos_vehiculo (nombre_tipo)
VALUES ('Camioneta'), ('Automóvil'), ('Camión'), ('Moto')
ON CONFLICT DO NOTHING;

INSERT INTO vehiculos (placa, id_marca, id_tipo_vehiculo, activo, kilometraje_actual)
VALUES
  ('ABC-123', 1, 1, true, 50000),
  ('XYZ-456', 2, 2, true, 32000),
  ('DEF-789', 3, 1, true, 75000),
  ('GHI-000', 4, 2, false, 10000)
ON CONFLICT (placa) DO NOTHING;

SELECT 'vehiculos OK' as estado, COUNT(*) as total FROM vehiculos WHERE activo = true;
