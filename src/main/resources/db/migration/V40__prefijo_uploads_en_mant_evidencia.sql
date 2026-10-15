-- EvidenciaStorageService guardaba ruta_archivo relativa a la raíz de uploads (ej.
-- "subestaciones/ejecuciones/35/xxx.jpg"), a diferencia de los demás módulos
-- (documentos de vehículo, facturas de combustible), que guardan la ruta pública
-- completa con el prefijo "/uploads/" incluido. Esa inconsistencia hacía que el
-- navegador/la app pidieran la imagen sin "/uploads/" y el backend respondiera 403.
--
-- El código ya se corrigió (ver EvidenciaStorageService.store()) para guardar el
-- prefijo desde ya en los archivos nuevos — esta migración solo corrige las filas
-- que ya existían con el formato viejo. Es segura de reejecutar: el WHERE excluye
-- las filas que ya tienen el prefijo, así que no lo duplica.

UPDATE mant_evidencia
SET ruta_archivo = '/uploads/' || ruta_archivo
WHERE ruta_archivo NOT LIKE '/uploads/%';
