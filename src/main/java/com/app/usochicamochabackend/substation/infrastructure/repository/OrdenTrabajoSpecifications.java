package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenTrabajoEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Filtros opcionales de GET /ordenes (mismo patrón que EjecucionSpecifications). */
public final class OrdenTrabajoSpecifications {

    private OrdenTrabajoSpecifications() {
    }

    /**
     * @param vencidas true = solo las que siguen en curso con la fecha límite ya pasada
     * @param hoy      fecha de referencia para "vencidas"
     */
    public static Specification<OrdenTrabajoEntity> conFiltros(
            List<String> estados, Long estacionId, String prioridad, Boolean vencidas, String disciplina,
            Long asignadoAId, LocalDate hoy) {
        return (root, query, cb) -> {
            List<Predicate> predicados = new ArrayList<>();
            predicados.add(cb.isTrue(root.get("status")));
            if (estados != null && !estados.isEmpty()) {
                predicados.add(root.get("estado").in(estados));
            }
            if (estacionId != null) {
                predicados.add(cb.equal(root.get("estacion").get("id"), estacionId));
            }
            if (prioridad != null && !prioridad.isBlank()) {
                predicados.add(cb.equal(root.get("prioridad"), prioridad));
            }
            if (Boolean.TRUE.equals(vencidas)) {
                predicados.add(root.get("estado").in(OrdenTrabajoEntity.ASIGNADA, OrdenTrabajoEntity.POR_VERIFICAR));
                predicados.add(cb.lessThan(root.get("fechaLimite"), hoy));
            }
            if (disciplina != null && !disciplina.isBlank()) {
                predicados.add(cb.equal(root.get("disciplina").get("codigo"), disciplina));
            }
            if (asignadoAId != null) {
                predicados.add(cb.equal(root.get("asignadoA").get("id"), asignadoAId));
            }
            return cb.and(predicados.toArray(new Predicate[0]));
        };
    }
}
