package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface HallazgoSeguimientoRepository extends JpaRepository<HallazgoSeguimientoEntity, Long> {

    Optional<HallazgoSeguimientoEntity> findByEjecucion_Id(Long ejecucionId);

    /** Hallazgos ABIERTO o EN_PROCESO por estación (todos los años). Columnas: estacionId, cantidad. */
    @Query("""
        SELECT h.ejecucion.estacion.id, COUNT(h)
        FROM HallazgoSeguimientoEntity h
        WHERE h.status = true AND h.estado IN ('ABIERTO', 'EN_PROCESO')
          AND h.ejecucion.disciplina.codigo = :disciplina
        GROUP BY h.ejecucion.estacion.id
        """)
    List<Object[]> abiertosPorEstacion(@Param("disciplina") String disciplina);
}
