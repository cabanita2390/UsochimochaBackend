package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.EvidenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EvidenciaRepository extends JpaRepository<EvidenciaEntity, Long> {
    List<EvidenciaEntity> findByEjecucion_Id(Long ejecucionId);

    /** Detecta subidas duplicadas de la misma foto para la misma ejecución (ver V39). */
    Optional<EvidenciaEntity> findByEjecucion_IdAndHashSha256(Long ejecucionId, String hashSha256);
}
