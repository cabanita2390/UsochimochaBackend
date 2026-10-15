--  Se usa como preselección de frecuencia en la Asignación masiva.
ALTER TABLE mant_estacion DROP CONSTRAINT mant_estacion_frecuencia_base_check;
ALTER TABLE mant_estacion ADD CONSTRAINT mant_estacion_frecuencia_base_check
    CHECK (frecuencia_base IN ('MENSUAL', 'BIMESTRAL', 'TRIMESTRAL', 'SEMESTRAL', 'ANUAL'));
