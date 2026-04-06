@echo off
set PGPASSWORD=admin
"C:\Program Files\PostgreSQL\16\bin\psql.exe" -U postgres -d APP_usuchicamocha3 -c "SELECT v.placa, d.tipo_documento, d.fecha_vencimiento, d.activo FROM documentacion_y_elementos d JOIN vehiculos v ON d.id_vehiculo = v.id WHERE v.tipo_vehiculo = 'MOTOCICLETA' ORDER BY v.placa, d.tipo_documento LIMIT 30;"
