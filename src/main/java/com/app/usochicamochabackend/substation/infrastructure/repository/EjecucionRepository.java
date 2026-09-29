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

    /**
     * Ejecuciones de la disciplina entre dos fechas, agrupadas por estación. Columnas:
     * estacionId, total, programadas, noProgramadas, mantenimiento, inspeccion.
     */
    @Query("""
        SELECT e.estacion.id, COUNT(e),
               SUM(CASE WHEN e.esProgramada = true THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.esProgramada = false THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'MANTENIMIENTO' THEN 1 ELSE 0 END),
               SUM(CASE WHEN e.tipoActividad = 'INSPECCION' THEN 1 ELSE 0 END)
        FROM EjecucionEntity e
        WHERE e.disciplina.codigo = :disciplina AND e.fecha BETWEEN :desde AND :hasta
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
        WHERE e.actividad IS NOT NULL AND e.disciplina.codigo = :disciplina AND e.fecha BETWEEN :desde AND :hasta
        GROUP BY e.actividad.id
        """)
    List<Object[]> contarPorActividad(@Param("disciplina") String disciplina,
            @Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
