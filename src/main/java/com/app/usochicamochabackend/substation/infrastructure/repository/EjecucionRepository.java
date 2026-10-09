package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EjecucionRepository extends JpaRepository<EjecucionEntity, Long>, JpaSpecificationExecutor<EjecucionEntity> {

    boolean existsByUuidCliente(UUID uuidCliente);

    Optional<EjecucionEntity> findByUuidCliente(UUID uuidCliente);

    Page<EjecucionEntity> findByProgramacion_Id(Long programacionId, Pageable pageable);

    Optional<EjecucionEntity> findFirstByProgramacion_IdOrderByIdDesc(Long programacionId);

    boolean existsByProgramacion_Id(Long programacionId);

    boolean existsByActividad_Id(Long actividadId);

    /** Actividades con al menos una ejecución (su disciplina ya no se puede cambiar). */
    @Query("SELECT DISTINCT e.actividad.id FROM EjecucionEntity e WHERE e.actividad IS NOT NULL")
    List<Long> actividadesEjecutadas();

    /**
     * Ejecuciones de la disciplina (null = todas) entre dos fechas, agrupadas por estación. Columnas:
     * estacionId, total, programadas, noProgramadas, mantenimiento, inspeccion, conHallazgos.
     */
    @Query("""
        SELECT e.estacion.id, COUNT(e),
               SUM(CASE WHEN e.esProgramada = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.esProgramada = false THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'MANTENIMIENTO' THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'INSPECCION' THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.resultado <> 'CONFORME' THEN 1 ELSE 0 END)
        FROM EjecucionEntity e
        WHERE (:disciplina IS NULL OR e.disciplina.codigo = :disciplina) AND e.fecha BETWEEN :desde AND :hasta
        GROUP BY e.estacion.id
        """)
    List<Object[]> contarPorEstacion(@Param("disciplina") String disciplina,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /** Igual que contarPorEstacion, agrupado por actividad (solo ejecuciones con actividad del catálogo). */
    @Query("""
        SELECT e.actividad.id, COUNT(e),
               SUM(CASE WHEN e.esProgramada = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.esProgramada = false THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'MANTENIMIENTO' THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'INSPECCION' THEN 1 ELSE 0 END)
        FROM EjecucionEntity e
        WHERE e.actividad IS NOT NULL AND (:disciplina IS NULL OR e.disciplina.codigo = :disciplina) AND e.fecha BETWEEN :desde AND :hasta
        GROUP BY e.actividad.id
        """)
    List<Object[]> contarPorActividad(@Param("disciplina") String disciplina,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /**
     * Observaciones más repetidas por tipo de actividad (sugerencias del móvil). Agrupa sin
     * distinguir mayúsculas ni espacios de los extremos y muestra la redacción más reciente.
     * Columnas: tipoActividad, texto. Hasta :limite por tipo, de más a menos usada. Deja fuera las
     * frases que no dicen nada ("No aplica", "Ninguna"…) y el tipo obsoleto NO_PROGRAMADO.
     */
    @Query(value = """
        SELECT tipo_actividad, texto FROM (
            SELECT e.tipo_actividad,
                   (array_agg(btrim(e.observaciones) ORDER BY e.fecha DESC, e.id DESC))[1] AS texto,
                   row_number() OVER (PARTITION BY e.tipo_actividad
                                      ORDER BY count(*) DESC, max(e.fecha) DESC) AS puesto
            FROM mant_ejecucion e JOIN mant_disciplina d ON d.id = e.disciplina_id
            WHERE (CAST(:disciplina AS text) IS NULL OR d.codigo = :disciplina)
              AND length(btrim(e.observaciones)) >= 4
              AND e.tipo_actividad <> 'NO_PROGRAMADO'
              AND lower(btrim(e.observaciones)) NOT IN
                  ('no aplica', 'ninguna', 'ninguno', 'sin observaciones', 'sin novedad', 'n/a')
            GROUP BY e.tipo_actividad, lower(btrim(e.observaciones))
        ) t
        WHERE puesto <= :limite
        ORDER BY tipo_actividad, puesto
        """, nativeQuery = true)
    List<Object[]> observacionesFrecuentes(@Param("disciplina") String disciplina, @Param("limite") int limite);

    /**
     * Intervenciones por actividad en una estación (histórico): ejecuciones con actividad del
     * catálogo. Columnas: actividadId, nombre, nombreCorto, intervenciones. De más a menos.
     */
    @Query("""
        SELECT a.id, a.nombre, a.nombreCorto, COUNT(e)
        FROM EjecucionEntity e JOIN e.actividad a
        WHERE e.estacion.id = :estacionId AND (:disciplina IS NULL OR e.disciplina.codigo = :disciplina)
        GROUP BY a.id, a.nombre, a.nombreCorto
        ORDER BY COUNT(e) DESC, a.nombre
        """)
    List<Object[]> criticidadPorEstacion(@Param("estacionId") Long estacionId, @Param("disciplina") String disciplina);
}
