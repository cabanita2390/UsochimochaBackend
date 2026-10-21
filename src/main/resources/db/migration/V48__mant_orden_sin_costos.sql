-- David, 2026-10-18: las órdenes de trabajo no llevan costos (mano de obra y materiales). V46 ya
-- se aplicó en bases locales, así que se quita aquí en vez de editarla.
DROP TABLE IF EXISTS mant_orden_costo;

ALTER TABLE mant_orden_evento DROP CONSTRAINT IF EXISTS mant_orden_evento_tipo_check;
DELETE FROM mant_orden_evento WHERE tipo = 'COSTOS';
ALTER TABLE mant_orden_evento ADD CONSTRAINT mant_orden_evento_tipo_check
    CHECK (tipo IN ('CREADA', 'EDITADA', 'EJECUTADA', 'DEVUELTA', 'CERRADA', 'ANULADA', 'EJECUCION_FUERA_DE_ESTADO'));
