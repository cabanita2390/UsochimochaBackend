-- Reintentos del móvil sin duplicar inspecciones (prueba exigente de presión de llantas, 2026-10-28).

-- Vehículo y moto: el móvil ya guarda un UUID por inspección pendiente; ahora lo envía y el
-- servidor responde con la inspección ya registrada en vez de crear otra (con sus lecturas de
-- presión). NULL para las inspecciones viejas y para versiones del móvil que no lo envían.
ALTER TABLE inspeccion_pre_operativa ADD COLUMN uuid_cliente VARCHAR(36);
CREATE UNIQUE INDEX ux_inspeccion_pre_operativa_uuid_cliente ON inspeccion_pre_operativa(uuid_cliente);

-- Maquinaria: el UUID ya se revisaba antes de guardar, pero dos envíos simultáneos pasaban los dos
-- (la misma inspección quedó 6 veces). Las inspecciones históricas pueden tener UUID repetidos, así
-- que el índice solo cubre las que se registren desde ahora.
DO $$
BEGIN
    EXECUTE format('CREATE UNIQUE INDEX ux_inspections_uuid_nuevas ON inspections(uuid) WHERE id > %s',
                   (SELECT COALESCE(MAX(id), 0) FROM inspections));
END $$;
