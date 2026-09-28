package com.app.usochicamochabackend.substation.infrastructure.repository;

import com.app.usochicamochabackend.substation.infrastructure.entity.HallazgoSeguimientoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HallazgoSeguimientoRepository extends JpaRepository<HallazgoSeguimientoEntity, Long> {

    Optional<HallazgoSeguimientoEntity> findByEjecucion_Id(Long ejecucionId);
}
