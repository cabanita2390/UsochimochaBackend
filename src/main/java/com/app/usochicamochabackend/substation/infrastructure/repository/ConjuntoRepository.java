package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.ConjuntoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConjuntoRepository extends JpaRepository<ConjuntoEntity, Long> {
    List<ConjuntoEntity> findByDisciplina_CodigoAndStatusTrueOrderByNombreAsc(String disciplinaCodigo);

    List<ConjuntoEntity> findByStatusTrueOrderByNombreAsc();

    List<ConjuntoEntity> findByDisciplina_CodigoOrderByNombreAsc(String disciplinaCodigo);

    List<ConjuntoEntity> findAllByOrderByNombreAsc();

    /** Lo marcó alguna visita: el nombre se puede corregir, pero no cambia de disciplina. */
    @Query("SELECT COUNT(e) > 0 FROM EjecucionEntity e JOIN e.conjuntos x WHERE x.id = :id")
    boolean enUso(@Param("id") Long id);
}
