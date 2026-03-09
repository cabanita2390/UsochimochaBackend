-- Ejecutar este comando en PostgreSQL para corregir el error de tipo de dato
-- El sistema guarda "Vigente", "Vencido", etc., por lo que la columna debe ser VARCHAR, no BOOLEAN.

ALTER TABLE insp_detalle_documentos 
ALTER COLUMN check_extintor TYPE VARCHAR(50) USING check_extintor::text;

-- O si prefieres borrarla y crearla de cero si no tienes datos importantes:
-- ALTER TABLE insp_detalle_documentos DROP COLUMN check_extintor;
-- ALTER TABLE insp_detalle_documentos ADD COLUMN check_extintor VARCHAR(50);
