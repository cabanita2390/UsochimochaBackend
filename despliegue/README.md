# Despliegue de Estaciones de Bombeo (Subestaciones Civil)

Pasos de base de datos para llevar el módulo a `back-test` y a producción. El backend y la
web se despliegan juntos (la web usa los endpoints nuevos del cronograma).

## 0. Antes de empezar

- El commit que renumera las migraciones (seeds V41/V42 eliminados, V43–V47 → V41–V45)
  tiene que estar en la rama que se despliega.
- Sacar un respaldo: `pg_dump --create <base> > respaldo_antes_subestaciones.sql`.
- Mirar en qué numeración está la base:

  ```sql
  SELECT version, script FROM flyway_schema_history WHERE version::int >= 40 ORDER BY installed_rank;
  ```

## 1. Bases que ya aplicaron la numeración anterior (back-test, bases locales viejas)

Si aparece `V41__seed_inicial_subestaciones_civil.sql`, el backend nuevo **no arranca**:
Flyway falla la validación (V41 con otro checksum, V46/V47 sin archivo). Con el backend
detenido:

```bash
psql -v ON_ERROR_STOP=1 -d <base> -f despliegue/flyway_renumeracion_subestaciones.sql
```

Mueve las filas de la historia a la numeración nueva. Los archivos renombrados tienen
exactamente el mismo contenido, así que su checksum no cambia. Los datos que cargaron los
seeds se quedan. Si la base ya está en la numeración nueva, el script no hace nada.

## 2. Producción (sin nada del módulo todavía)

1. Desplegar el backend: Flyway aplica las migraciones pendientes (hasta V45) y crea las tablas del módulo vacías.
2. Cargar los datos reales con el usuario que firmará los 34 registros históricos:

   ```bash
   psql -v ON_ERROR_STOP=1 -v usuario=<username> -d <base> -f despliegue/carga_inicial_subestaciones.sql
   ```

   El script carga 23 estaciones, 9 actividades Civil, 65 citas 2026 publicadas, 34 ejecuciones
   históricas con sus hallazgos ABIERTOS y la publicación "Carga inicial" del año.
   Todo va en una transacción. Aborta sin cambiar nada si ya hay estaciones o si el usuario no
   existe.

   Sin este paso, el cronograma 2026 no se puede reconstruir desde la web porque enero a
   septiembre son meses cerrados.

## 3. Verificación rápida

- Web › Estaciones de Bombeo › Cronograma Anual: "Publicado a móvil · Carga inicial", 65 citas.
- Dashboard: 65 programadas.
- App móvil (con la URL de producción): Home muestra las citas del mes en curso.
