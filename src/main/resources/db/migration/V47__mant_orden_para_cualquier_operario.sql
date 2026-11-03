-- Una orden de trabajo puede quedar "para cualquier operario": la ven todos en el móvil y queda a
-- nombre del primero que la ejecuta (asignado_a se llena en ese momento).
ALTER TABLE mant_orden_trabajo ALTER COLUMN asignado_a DROP NOT NULL;
