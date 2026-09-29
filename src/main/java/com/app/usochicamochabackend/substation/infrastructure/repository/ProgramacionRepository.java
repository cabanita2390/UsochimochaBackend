package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramacionRepository extends JpaRepository<ProgramacionEntity, Long> {

    /** Citas que ve el móvil (GET /programacion): solo las del estado pedido (PUBLICADA). */
    List<ProgramacionEntity> findByEstacion_IdAndAnioAndMesAndActividad_Disciplina_CodigoAndStatusTrueAndEstado(
            Long estacionId, Integer anio, Integer mes, String disciplinaCodigo, String estado);


    @Query("""
        SELECT p.actividad.id, COUNT(p) FROM ProgramacionEntity p
        WHERE p.anio = :anio AND p.status = true AND p.estado = 'PUBLICADA'
        GROUP BY p.actividad.id
        """)
    List<Object[]> contarCitasPorActividad(@Param("anio") Integer anio);

    int countByActividad_IdAndAnioAndStatusTrueAndEstado(Long actividadId, Integer anio, String estado);
}
