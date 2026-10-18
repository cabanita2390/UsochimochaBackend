package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.OrdenTrabajoEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajoEntity, Long>,
        JpaSpecificationExecutor<OrdenTrabajoEntity> {

    /** La orden bloqueada para escritura (registrar la visita que la ejecuta, verificarla). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OrdenTrabajoEntity o WHERE o.id = :id")
    Optional<OrdenTrabajoEntity> findByIdParaActualizar(@Param("id") Long id);

    /** La orden vigente (no anulada) de un hallazgo: a lo sumo una (D3). */
    @Query("""
        SELECT o FROM OrdenTrabajoEntity o
        WHERE o.ejecucionOrigen.id = :ejecucionId AND o.status = true AND o.estado <> 'ANULADA'
        """)
    Optional<OrdenTrabajoEntity> vigenteDeHallazgo(@Param("ejecucionId") Long ejecucionId);

    /** Órdenes vigentes de varios hallazgos (para no consultar una por una al listar ejecuciones). */
    @Query("""
        SELECT o FROM OrdenTrabajoEntity o
        WHERE o.ejecucionOrigen.id IN :ejecucionIds AND o.status = true AND o.estado <> 'ANULADA'
        """)
    List<OrdenTrabajoEntity> vigentesDeHallazgos(@Param("ejecucionIds") List<Long> ejecucionIds);

    /** Órdenes que el operario tiene por hacer (lista "Prioritarias" del móvil). */
    @Query("""
        SELECT o FROM OrdenTrabajoEntity o
        WHERE o.asignadoA.id = :usuarioId AND o.status = true AND o.estado = 'ASIGNADA'
        ORDER BY CASE WHEN o.prioridad = 'ALTA' THEN 0 ELSE 1 END, o.fechaLimite, o.id
        """)
    List<OrdenTrabajoEntity> asignadasA(@Param("usuarioId") Long usuarioId);
}
