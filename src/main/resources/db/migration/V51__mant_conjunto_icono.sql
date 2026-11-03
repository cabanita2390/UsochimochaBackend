-- Ícono de cada conjunto en el móvil («¿Qué se intervino?»), elegido desde Configuración en la web.
-- Es un código fijo (el móvil y la web lo traducen a su dibujo); NULL = ícono genérico.
ALTER TABLE mant_conjunto ADD COLUMN icono VARCHAR(30);

UPDATE mant_conjunto c
SET icono = v.icono
FROM (VALUES
    ('Subestación', 'SUBESTACION'),
    ('Gabinetes de control', 'GABINETE'),
    ('Red de media tensión', 'RED_MT'),
    ('Generador VAR', 'GENERADOR'),
    ('Iluminación', 'ILUMINACION'),
    ('Motores', 'MOTOR'),
    ('Transformador', 'TRANSFORMADOR'),
    ('Postes', 'POSTE')
) AS v(nombre, icono)
WHERE c.nombre = v.nombre
  AND c.disciplina_id = (SELECT id FROM mant_disciplina WHERE codigo = 'ELECTRICO');
