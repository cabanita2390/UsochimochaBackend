package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.ProgramacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProgramacionRepository extends JpaRepository<ProgramacionEntity, Long> {

    List<ProgramacionEntity> findByEstacion_IdAndAnioAndMesAndActividad_Disciplina_CodigoAndStatusTrue(
            Long estacionId, Integer anio, Integer mes, String disciplinaCodigo);


    @Query("""
        SELECT p.actividad.id, COUNT(p) FROM ProgramacionEntity p
        WHERE p.anio = :anio AND p.status = true
        GROUP BY p.actividad.id
        """)
    List<Object[]> contarCitasPorActividad(@Param("anio") Integer anio);

    int countByActividad_IdAndAnioAndStatusTrue(Long actividadId, Integer anio);
}
