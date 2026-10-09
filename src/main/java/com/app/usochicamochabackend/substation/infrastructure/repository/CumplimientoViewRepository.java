package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.CumplimientoView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CumplimientoViewRepository extends JpaRepository<CumplimientoView, Long> {

    List<CumplimientoView> findByEstacionIdAndAnioAndDisciplinaOrderByMesAsc(Long estacionId, Integer anio, String disciplina);

    /** Todas las disciplinas: el móvil muestra todas (solo Civil se puede registrar por ahora). */
    List<CumplimientoView> findByEstacionIdAndAnioOrderByMesAsc(Long estacionId, Integer anio);

    /** Citas publicadas del año y la disciplina (Dashboard y Resumen por actividad). */
    List<CumplimientoView> findByAnioAndDisciplina(Integer anio, String disciplina);

    /** Citas publicadas del año de todas las disciplinas. */
    List<CumplimientoView> findByAnio(Integer anio);

    List<CumplimientoView> findByAnioAndMesAndDisciplinaOrderByEstacionNombreAsc(Integer anio, Integer mes, String disciplina);

    List<CumplimientoView> findByAnioAndMesOrderByEstacionNombreAsc(Integer anio, Integer mes);
}
