package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramacionRepository extends JpaRepository<ProgramacionEntity, Long> {

    /** Años con citas vigentes o con registros, del más reciente al más antiguo. */
    @org.springframework.data.jpa.repository.Query(value = """
        SELECT anio FROM (
            SELECT CAST(anio AS integer) AS anio FROM mant_programacion WHERE status
            UNION
            SELECT CAST(EXTRACT(YEAR FROM fecha) AS integer) FROM mant_ejecucion
        ) t ORDER BY anio DESC
        """, nativeQuery = true)
    List<Number> aniosConDatos();

    /** Citas que ve el móvil (GET /programacion): solo las del estado pedido (PUBLICADA). */
    List<ProgramacionEntity> findByEstacion_IdAndAnioAndMesAndActividad_Disciplina_CodigoAndStatusTrueAndEstado(
            Long estacionId, Integer anio, Integer mes, String disciplinaCodigo, String estado);


    @Query("""
        SELECT p.actividad.id, COUNT(p) FROM ProgramacionEntity p
        WHERE p.anio = :anio AND p.status = true AND p.estado = 'PUBLICADA'
        GROUP BY p.actividad.id
        """)
    List<Object[]> contarCitasPorActividad(@Param("anio") Integer anio);

    /**
     * Citas vigentes del año para la grilla del Cronograma (BORRADOR y PUBLICADA de estaciones
     * activas), con su disciplina y su primera ejecución. Columnas: id, mes, estacionId,
     * actividadId, disciplina, estado, pendienteRetiro, fechaPrimeraEjecucion, nEjecuciones.
     */
    @Query("""
        SELECT p.id, p.mes, est.id, a.id, d.codigo, p.estado, p.pendienteRetiro, MIN(e.fecha), COUNT(e.id)
        FROM ProgramacionEntity p
        JOIN p.estacion est
        JOIN p.actividad a
        JOIN a.disciplina d
        LEFT JOIN EjecucionEntity e ON e.programacion = p
        WHERE p.anio = :anio AND p.status = true AND p.estado <> 'RETIRADA' AND est.status = true
        GROUP BY p.id, p.mes, est.id, a.id, d.codigo, p.estado, p.pendienteRetiro
        ORDER BY est.id, p.mes, p.id
        """)
    List<Object[]> citasDelCronograma(@Param("anio") Integer anio);

    /** Citas vigentes (no RETIRADA) del año: para no duplicar al asignar o copiar. */
    List<ProgramacionEntity> findByAnioAndStatusTrueAndEstadoNot(Integer anio, String estado);

    List<ProgramacionEntity> findByAnioAndStatusTrueAndEstado(Integer anio, String estado);

    List<ProgramacionEntity> findByAnioAndStatusTrueAndPendienteRetiroTrue(Integer anio);

    boolean existsByAnioAndStatusTrueAndEstado(Integer anio, String estado);

    /**
     * Cambios pendientes de publicar del año: altas en BORRADOR de estaciones activas y
     * PUBLICADA pendientes de retiro (de cualquier estación: al desactivar una estación sus
     * citas futuras quedan para quitar), bloqueados para escritura: si dos publicaciones llegan a la vez, la
     * segunda espera y ya no los encuentra.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT p FROM ProgramacionEntity p
        WHERE p.anio = :anio AND p.status = true
          AND ((p.estado = 'BORRADOR' AND p.estacion.status = true) OR p.pendienteRetiro = true)
        ORDER BY p.id
        """)
    List<ProgramacionEntity> cambiosPendientesParaPublicar(@Param("anio") Integer anio);

    /** Lo mismo sin bloqueo, para el resumen del modal. */
    @Query("""
        SELECT p FROM ProgramacionEntity p
        WHERE p.anio = :anio AND p.status = true
          AND ((p.estado = 'BORRADOR' AND p.estacion.status = true) OR p.pendienteRetiro = true)
        ORDER BY p.id
        """)
    List<ProgramacionEntity> cambiosPendientes(@Param("anio") Integer anio);

    /** La cita bloqueada para escritura: quitar/restaurar esperan a una publicación en curso. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProgramacionEntity p WHERE p.id = :id")
    java.util.Optional<ProgramacionEntity> findByIdParaActualizar(@Param("id") Long id);

    List<ProgramacionEntity> findByPublicadaEn_IdAndStatusTrueAndEstado(Long publicacionId, String estado);

    List<ProgramacionEntity> findByRetiradaEn_IdAndStatusTrueAndEstado(Long publicacionId, String estado);

    boolean existsByAnioAndMesAndEstacion_IdAndActividad_IdAndStatusTrueAndEstadoNot(
            Integer anio, Integer mes, Long estacionId, Long actividadId, String estado);

    java.util.Optional<ProgramacionEntity> findFirstByAnioAndMesAndEstacion_IdAndActividad_IdAndStatusTrueAndEstadoNot(
            Integer anio, Integer mes, Long estacionId, Long actividadId, String estado);

    /** Citas vigentes (no RETIRADA) de una estación / actividad: para retirarlas al desactivarla. */
    List<ProgramacionEntity> findByEstacion_IdAndStatusTrueAndEstadoNot(Long estacionId, String estado);

    List<ProgramacionEntity> findByActividad_IdAndStatusTrueAndEstadoNot(Long actividadId, String estado);

    /** Actividades que alguna vez se programaron (su disciplina ya no se puede cambiar). */
    @Query("SELECT DISTINCT p.actividad.id FROM ProgramacionEntity p")
    List<Long> actividadesProgramadas();

    boolean existsByActividad_Id(Long actividadId);

    int countByAnioAndStatusTrueAndPendienteRetiroTrue(Integer anio);

    int countByActividad_IdAndAnioAndStatusTrueAndEstado(Long actividadId, Integer anio, String estado);
}
