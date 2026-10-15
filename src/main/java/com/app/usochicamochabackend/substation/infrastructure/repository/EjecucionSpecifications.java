package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.EjecucionEntity;
import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Filtros opcionales de GET /ejecuciones como Specification en vez de métodos derivados
 * (findByXAndYAndZ...): con 6 filtros optativos (estación, rango de fechas, esProgramada,
 * resultado, actividad, tipoMantenimiento, tipoActividad) la explosión combinatoria de
 * métodos derivados (2^6) ya no es manejable — este es el primer lugar de la app con
 * tantos filtros optativos combinables a la vez.
 */
public final class EjecucionSpecifications {

    private EjecucionSpecifications() {
    }

    public static Specification<EjecucionEntity> filtrar(
            Long estacionId, LocalDate desde, LocalDate hasta, Boolean esProgramada,
            List<String> resultado, Long actividadId, String tipoMantenimiento, String tipoActividad,
            List<String> seguimiento, String disciplina) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.between(root.get("fecha"), desde, hasta));
            if (estacionId != null) {
                predicados.add(cb.equal(root.get("estacion").get("id"), estacionId));
            }
            if (esProgramada != null) {
                predicados.add(cb.equal(root.get("esProgramada"), esProgramada));
            }
            if (resultado != null && !resultado.isEmpty()) {
                predicados.add(root.get("resultado").in(resultado));
            }
            if (actividadId != null) {
                predicados.add(cb.equal(root.get("actividad").get("id"), actividadId));
            }
            if (tipoMantenimiento != null) {
                predicados.add(cb.equal(root.get("tipoMantenimiento"), tipoMantenimiento));
            }
            if (disciplina != null && !disciplina.isBlank()) {
                predicados.add(cb.equal(root.get("disciplina").get("codigo"), disciplina));
            }
            if (tipoActividad != null) {
                predicados.add(cb.equal(root.get("tipoActividad"), tipoActividad));
            }
            if (seguimiento != null && !seguimiento.isEmpty()) {
                // EjecucionEntity no conoce su seguimiento (la FK está del lado del seguimiento):
                // EXISTS (seguimiento activo de esta ejecución con alguno de los estados pedidos).
                Subquery<Long> sq = query.subquery(Long.class);
                Root<HallazgoSeguimientoEntity> seg = sq.from(HallazgoSeguimientoEntity.class);
                sq.select(seg.get("id")).where(
                        cb.equal(seg.get("ejecucion"), root),
                        seg.get("estado").in(seguimiento),
                        cb.isTrue(seg.get("status")));
                predicados.add(cb.exists(sq));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
